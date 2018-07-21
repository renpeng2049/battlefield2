package com.soyoung.battle.field.http.netty4;

import com.soyoung.battle.field.ThreadFactoryImpl;
import com.soyoung.battle.field.common.Constants;
import com.soyoung.battle.field.common.PortsRange;
import com.soyoung.battle.field.common.Strings;
import com.soyoung.battle.field.common.component.*;
import com.soyoung.battle.field.common.network.NetworkAddress;
import com.soyoung.battle.field.common.setting.Settings;
import com.soyoung.battle.field.common.unit.ByteSizeUnit;
import com.soyoung.battle.field.common.unit.ByteSizeValue;
import com.soyoung.battle.field.common.unit.TimeValue;
import com.soyoung.battle.field.http.BindHttpException;
import com.soyoung.battle.field.http.HttpInfo;
import com.soyoung.battle.field.http.HttpServerTransport;
import com.soyoung.battle.field.http.HttpStats;
import com.soyoung.battle.field.http.netty4.cors.Netty4CorsConfig;
import com.soyoung.battle.field.http.netty4.cors.Netty4CorsConfigBuilder;
import com.soyoung.battle.field.http.netty4.cors.Netty4CorsHandler;
import com.soyoung.battle.field.rest.RestChannel;
import com.soyoung.battle.field.rest.RestRequest;
import com.soyoung.battle.field.rest.RestUtils;
import com.soyoung.battle.field.transport.BoundTransportAddress;
import com.soyoung.battle.field.transport.TransportAddress;
import com.soyoung.battle.field.transport.netty4.Netty4OpenChannelsHandler;
import com.soyoung.battle.field.transport.netty4.Netty4Utils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.soyoung.battle.field.http.HttpServerTransport.HTTP_SERVER_WORKER_THREAD_NAME_PREFIX;

public class Netty4HttpServerTransport extends AbstractLifecycleComponent implements HttpServerTransport {

    final Logger logger = LogManager.getLogger(Netty4HttpServerTransport.class);


    protected final ByteSizeValue maxContentLength;
    protected final ByteSizeValue maxInitialLineLength;
    protected final ByteSizeValue maxHeaderSize;
    protected final ByteSizeValue maxChunkSize;

    private final int readTimeoutMillis;
    protected final int maxCompositeBufferComponents;

    protected final boolean compression;
    protected final int compressionLevel;

    public static final String ANY_ORIGIN = "*";
    protected final boolean pipelining;
    protected final int pipeliningMaxEvents;

    private final Netty4CorsConfig corsConfig;
    protected final boolean resetCookies;

    protected volatile ServerBootstrap serverBootstrap;
    protected final int workerCount;
    protected final boolean detailedErrorsEnabled;

    private final HttpServerTransport.Dispatcher dispatcher;

    protected final RecvByteBufAllocator recvByteBufAllocator;
    protected volatile BoundTransportAddress boundAddress;

    protected final PortsRange port;
    protected final List<Channel> serverChannels = new ArrayList<>();

    // package private for testing
    Netty4OpenChannelsHandler serverOpenChannels;


    public Netty4HttpServerTransport(Settings settings, HttpServerTransport.Dispatcher dispatcher){

        super(settings);
        this.pipelining = true; //TODO
        this.detailedErrorsEnabled = true;
        corsConfig = buildCorsConfig();
        resetCookies = false; //TODO

        this.dispatcher = dispatcher;
        workerCount = 2;

        this.port = new PortsRange("9200-9300");

        this.pipeliningMaxEvents = 10000;

        ByteSizeValue maxContentLength = new ByteSizeValue(100, ByteSizeUnit.MB);
        this.maxInitialLineLength = new ByteSizeValue(4, ByteSizeUnit.KB);
        this.maxHeaderSize = new ByteSizeValue(8, ByteSizeUnit.KB);
        this.maxChunkSize = new ByteSizeValue(8, ByteSizeUnit.KB);

        readTimeoutMillis = Math.toIntExact(new TimeValue(0).millis());
        this.maxCompositeBufferComponents = -1;
        this.compression = true;
        this.compressionLevel = 3;

        ByteSizeValue receivePredictor = new ByteSizeValue(64, ByteSizeUnit.KB);
        recvByteBufAllocator = new FixedRecvByteBufAllocator(receivePredictor.bytesAsInt());

        // validate max content length
        if (maxContentLength.getBytes() > Integer.MAX_VALUE) {
            logger.warn("maxContentLength[{}] set to high value, resetting it to [100mb]", maxContentLength);
            maxContentLength = new ByteSizeValue(100, ByteSizeUnit.MB);
        }
        this.maxContentLength = maxContentLength;
    }

    public Netty4CorsConfig getCorsConfig() {
        return corsConfig;
    }

    static Netty4CorsConfig buildCorsConfig(){

        String origin = "";
        final Netty4CorsConfigBuilder builder;
        if (Strings.isNullOrEmpty(origin)) {
            builder = Netty4CorsConfigBuilder.forOrigins();
        } else if (origin.equals(ANY_ORIGIN)) {
            builder = Netty4CorsConfigBuilder.forAnyOrigin();
        } else {
            Pattern p = RestUtils.checkCorsSettingForRegex(origin);
            if (p == null) {
                builder = Netty4CorsConfigBuilder.forOrigins(RestUtils.corsSettingAsArray(origin));
            } else {
                builder = Netty4CorsConfigBuilder.forPattern(p);
            }
        }

        //允许认证
        builder.allowCredentials();

        return builder.build();
    }

    public boolean isPipelining() {
        return pipelining;
    }

    public boolean isResetCookies() {
        return resetCookies;
    }


    void dispatchRequest(final RestRequest request, final RestChannel channel) {
//        final ThreadContext threadContext = threadPool.getThreadContext();
//        try (ThreadContext.StoredContext ignore = threadContext.stashContext()) {
//            dispatcher.dispatchRequest(request, channel, threadContext);
//        }

        dispatcher.dispatchRequest(request, channel);

    }

    void dispatchBadRequest(final RestRequest request, final RestChannel channel, final Throwable cause) {
//        final ThreadContext threadContext = threadPool.getThreadContext();
//        try (ThreadContext.StoredContext ignore = threadContext.stashContext()) {
//            dispatcher.dispatchBadRequest(request, channel, threadContext, cause);
//        }
        dispatcher.dispatchBadRequest(request, channel, cause);
    }

    @Override
    protected void doStart(){
        logger.info("netty 4 http server transport start");

        this.serverOpenChannels = new Netty4OpenChannelsHandler(logger);

        serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(new NioEventLoopGroup(workerCount, daemonThreadFactory(
                HTTP_SERVER_WORKER_THREAD_NAME_PREFIX)));
        serverBootstrap.channel(NioServerSocketChannel.class);

        serverBootstrap.childHandler(configureServerChannelHandler());

        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

        final ByteSizeValue tcpSendBufferSize = new ByteSizeValue(-1);
        if (tcpSendBufferSize.getBytes() > 0) {
            serverBootstrap.childOption(ChannelOption.SO_SNDBUF, Math.toIntExact(tcpSendBufferSize.getBytes()));
        }

        final ByteSizeValue tcpReceiveBufferSize = new ByteSizeValue(-1);
        if (tcpReceiveBufferSize.getBytes() > 0) {
            serverBootstrap.childOption(ChannelOption.SO_RCVBUF, Math.toIntExact(tcpReceiveBufferSize.getBytes()));
        }

        serverBootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, recvByteBufAllocator);
        serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, recvByteBufAllocator);

        final boolean reuseAddress = Constants.WINDOWS? false : true;
        serverBootstrap.option(ChannelOption.SO_REUSEADDR, reuseAddress);
        serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, reuseAddress);

        this.boundAddress = createBoundHttpAddress();
        if (logger.isInfoEnabled()) {
            logger.info("{}", boundAddress);
        }
    }

    private BoundTransportAddress createBoundHttpAddress() {
        // Bind and start to accept incoming connections.

        InetAddress local = null;
        try {
            local = InetAddress.getByAddress(new byte[]{0,0,0,0});
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        InetAddress hostAddresses[] = {local};

        List<TransportAddress> boundAddresses = new ArrayList<>(hostAddresses.length);
        for (InetAddress address : hostAddresses) {
            boundAddresses.add(bindAddress(address));
        }

        final InetAddress publishInetAddress = local;

        final int publishPort = 9200;
        final InetSocketAddress publishAddress = new InetSocketAddress(publishInetAddress, publishPort);
        return new BoundTransportAddress(boundAddresses.toArray(new TransportAddress[0]), new TransportAddress(publishAddress));
    }


    private TransportAddress bindAddress(final InetAddress hostAddress) {
        final AtomicReference<Exception> lastException = new AtomicReference<>();
        final AtomicReference<InetSocketAddress> boundSocket = new AtomicReference<>();
        boolean success = port.iterate(portNumber -> {
            try {
                synchronized (serverChannels) {
                    ChannelFuture future = serverBootstrap.bind(new InetSocketAddress(hostAddress, portNumber)).sync();
                    serverChannels.add(future.channel());
                    boundSocket.set((InetSocketAddress) future.channel().localAddress());
                }
            } catch (Exception e) {
                lastException.set(e);
                return false;
            }
            return true;
        });
        if (!success) {
            throw new BindHttpException("Failed to bind to [" + port.getPortRangeString() + "]", lastException.get());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Bound http to address {{}}", NetworkAddress.format(boundSocket.get()));
        }
        return new TransportAddress(boundSocket.get());
    }



    public static ThreadFactory daemonThreadFactory(String namePrefix) {
        return new ThreadFactoryImpl(namePrefix);
    }


    public ChannelHandler configureServerChannelHandler() {
        return new HttpChannelHandler(this, detailedErrorsEnabled);
    }

    protected static class HttpChannelHandler extends ChannelInitializer<Channel> {

        private final Netty4HttpServerTransport transport;
        private final Netty4HttpRequestHandler requestHandler;

        protected HttpChannelHandler(
                final Netty4HttpServerTransport transport,
                final boolean detailedErrorsEnabled) {
            this.transport = transport;
            this.requestHandler = new Netty4HttpRequestHandler(transport, detailedErrorsEnabled);
        }

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast("openChannels", transport.serverOpenChannels);
            ch.pipeline().addLast("read_timeout", new ReadTimeoutHandler(transport.readTimeoutMillis, TimeUnit.MILLISECONDS));
            final HttpRequestDecoder decoder = new HttpRequestDecoder(
                    Math.toIntExact(transport.maxInitialLineLength.getBytes()),
                    Math.toIntExact(transport.maxHeaderSize.getBytes()),
                    Math.toIntExact(transport.maxChunkSize.getBytes()));
            decoder.setCumulator(ByteToMessageDecoder.COMPOSITE_CUMULATOR);
            ch.pipeline().addLast("decoder", decoder);
            ch.pipeline().addLast("decoder_compress", new HttpContentDecompressor());
            ch.pipeline().addLast("encoder", new HttpResponseEncoder());
            final HttpObjectAggregator aggregator = new HttpObjectAggregator(Math.toIntExact(transport.maxContentLength.getBytes()));
            if (transport.maxCompositeBufferComponents != -1) {
                aggregator.setMaxCumulationBufferComponents(transport.maxCompositeBufferComponents);
            }
            ch.pipeline().addLast("aggregator", aggregator);
            if (transport.compression) {
                ch.pipeline().addLast("encoder_compress", new HttpContentCompressor(transport.compressionLevel));
            }

            boolean isCorsEnabled = true;
            if (isCorsEnabled) {
                ch.pipeline().addLast("cors", new Netty4CorsHandler(transport.getCorsConfig()));
            }
            if (transport.pipelining) {
                ch.pipeline().addLast("pipelining", new HttpPipeliningHandler(transport.logger, transport.pipeliningMaxEvents));
            }
            ch.pipeline().addLast("handler", requestHandler);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            Netty4Utils.maybeDie(cause);
            super.exceptionCaught(ctx, cause);
        }

    }

    @Override
    protected void doStop() {
        synchronized (serverChannels) {
            if (!serverChannels.isEmpty()) {
                try {
                    Netty4Utils.closeChannels(serverChannels);
                } catch (IOException e) {
                    logger.trace("exception while closing channels", e);
                }
                serverChannels.clear();
            }
        }

        if (serverOpenChannels != null) {
            serverOpenChannels.close();
            serverOpenChannels = null;
        }

        if (serverBootstrap != null) {
            serverBootstrap.config().group().shutdownGracefully(0, 5, TimeUnit.SECONDS).awaitUninterruptibly();
            serverBootstrap = null;
        }
    }

    @Override
    protected void doClose() throws IOException {

    }

    @Override
    public BoundTransportAddress boundAddress() {
        return this.boundAddress;
    }

    @Override
    public HttpInfo info() {
        BoundTransportAddress boundTransportAddress = boundAddress();
        if (boundTransportAddress == null) {
            return null;
        }
        return new HttpInfo(boundTransportAddress, maxContentLength.getBytes());
    }

    @Override
    public HttpStats stats() {
        Netty4OpenChannelsHandler channels = serverOpenChannels;
        return new HttpStats(channels == null ? 0 : channels.numberOfOpenChannels(), channels == null ? 0 : channels.totalChannels());

    }
}
