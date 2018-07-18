package com.soyoung.battle.field.bootstrap;

import com.soyoung.battle.field.ThreadFactoryImpl;
import com.soyoung.battle.field.Version;
import com.soyoung.battle.field.common.breaker.*;
import com.soyoung.battle.field.common.logging.Loggers;
import com.soyoung.battle.field.common.util.set.Sets;
import com.soyoung.battle.field.env.Environment;
import com.soyoung.battle.field.http.netty4.Netty4HttpServerTransport;
import com.soyoung.battle.field.rest.RestController;
import com.soyoung.battle.field.rest.RestHandler;
import com.soyoung.battle.field.store.ArrayStore;
import com.soyoung.battle.field.usage.UsageService;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bootstrap {

    private static volatile Bootstrap INSTANCE;

    private final CountDownLatch keepAliveLatch = new CountDownLatch(1);
    private final Thread keepAliveThread;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl(
            "BrokerControllerScheduledThread"));

    /** creates a new instance */
    Bootstrap() {
        keepAliveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    keepAliveLatch.await();
                } catch (InterruptedException e) {
                    // bail out
                }
            }
        }, "battlefield[keepAlive/" + Version.CURRENT + "]");
        keepAliveThread.setDaemon(false);
        // keep this thread alive (non daemon thread) until we shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println(">>>>>>shutdown hook");
                keepAliveLatch.countDown();
            }
        });
    }


    /**
     * This method is invoked by {@link Battlefield#main(String[])} to startup battlefield.
     */
    static void init(Environment environment) throws Exception {
        INSTANCE = new Bootstrap();

        //load 存储组件
//        ArrayStore arrayStore = new ArrayStore(environment);
//        arrayStore.load();

        final UsageService usageService = new UsageService(environment.settings());
        CircuitBreakerService circuitBreakerService = new CircuitBreakerService(environment.settings()) {
            @Override
            public void registerBreaker(BreakerSettings breakerSettings) {

            }

            @Override
            public CircuitBreaker getBreaker(String name) {
                return null;
            }

            @Override
            public AllCircuitBreakerStats stats() {
                return null;
            }

            @Override
            public CircuitBreakerStats stats(String name) {
                return null;
            }
        };

        UnaryOperator<RestHandler> restWrapper = null;

        final RestController restController = new RestController(restWrapper,circuitBreakerService,usageService);

        // 启动netty端口
        Netty4HttpServerTransport httpServerTransport = new Netty4HttpServerTransport(restController);
        httpServerTransport.start();

        INSTANCE.start();
    }

    private void scheduledSout(){

        System.out.println("start scheduledSout");
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    System.out.println("wow wow");
                } catch (Throwable e) {
                    System.out.println("wow wow error");
                }
            }
        }, 1000 * 10, 1000 * 30, TimeUnit.MILLISECONDS);
    }


    private void start() throws Exception {
        keepAliveThread.start();
    }

    static void stop() throws IOException {

    }
}
