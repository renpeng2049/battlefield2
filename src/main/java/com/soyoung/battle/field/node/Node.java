package com.soyoung.battle.field.node;

import com.soyoung.battle.field.BattlefieldException;
import com.soyoung.battle.field.Build;
import com.soyoung.battle.field.Version;
import com.soyoung.battle.field.bootstrap.BootstrapCheck;
import com.soyoung.battle.field.bootstrap.BootstrapContext;
import com.soyoung.battle.field.cli.InternalSettingsPreparer;
import com.soyoung.battle.field.common.Constants;
import com.soyoung.battle.field.common.breaker.CircuitBreakerService;
import com.soyoung.battle.field.common.breaker.NoneCircuitBreakerService;
import com.soyoung.battle.field.common.component.Lifecycle;
import com.soyoung.battle.field.common.logging.Loggers;
import com.soyoung.battle.field.common.setting.Setting;
import com.soyoung.battle.field.common.setting.Settings;
import com.soyoung.battle.field.env.Environment;
import com.soyoung.battle.field.http.netty4.Netty4HttpServerTransport;
import com.soyoung.battle.field.monitor.jvm.JvmInfo;
import com.soyoung.battle.field.rest.RestController;
import com.soyoung.battle.field.rest.RestHandler;
import com.soyoung.battle.field.rest.action.BlankAction;
import com.soyoung.battle.field.rest.action.SelectAction;
import com.soyoung.battle.field.rest.action.SqlAction;
import com.soyoung.battle.field.store.ArrayStore;
import com.soyoung.battle.field.store.StoreSchemas;
import com.soyoung.battle.field.transport.BoundTransportAddress;
import com.soyoung.battle.field.usage.UsageService;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public class Node implements Closeable {



    public static final Setting<Boolean> WRITE_PORTS_FILE_SETTING =
            Setting.boolSetting("node.portsfile", false, Setting.Property.NodeScope);
    public static final Setting<Boolean> NODE_DATA_SETTING = Setting.boolSetting("node.data", true, Setting.Property.NodeScope);
    public static final Setting<Boolean> NODE_MASTER_SETTING =
            Setting.boolSetting("node.master", true, Setting.Property.NodeScope);
    public static final Setting<Boolean> NODE_INGEST_SETTING =
            Setting.boolSetting("node.ingest", true, Setting.Property.NodeScope);


    /**
     * controls whether the node is allowed to persist things like metadata to disk
     * Note that this does not control whether the node stores actual indices (see
     * {@link #NODE_DATA_SETTING}). However, if this is false, {@link #NODE_DATA_SETTING}
     * and {@link #NODE_MASTER_SETTING} must also be false.
     *
     */
    public static final Setting<Boolean> NODE_LOCAL_STORAGE_SETTING = Setting.boolSetting("node.local_storage", true, Setting.Property.NodeScope);
    public static final Setting<String> NODE_NAME_SETTING = Setting.simpleString("node.name", Setting.Property.NodeScope);
    public static final Setting.AffixSetting<String> NODE_ATTRIBUTES = Setting.prefixKeySetting("node.attr.", (key) ->
            new Setting<>(key, "", (value) -> {
                if (value.length() > 0
                        && (Character.isWhitespace(value.charAt(0)) || Character.isWhitespace(value.charAt(value.length() - 1)))) {
                    throw new IllegalArgumentException(key + " cannot have leading or trailing whitespace " +
                            "[" + value + "]");
                }
                return value;
            }, Setting.Property.NodeScope));
    public static final Setting<String> BREAKER_TYPE_KEY = new Setting<>("indices.breaker.type", "hierarchy", (s) -> {
        switch (s) {
            case "hierarchy":
            case "none":
                return s;
            default:
                throw new IllegalArgumentException("indices.breaker.type must be one of [hierarchy, none] but was: " + s);
        }
    }, Setting.Property.NodeScope);

    /**
     * Adds a default node name to the given setting, if it doesn't already exist
     * @return the given setting if node name is already set, or a new copy with a default node name set.
     */
    public static final Settings addNodeNameIfNeeded(Settings settings, final String nodeId) {
        if (NODE_NAME_SETTING.exists(settings)) {
            return settings;
        }
        return Settings.builder().put(settings).put(NODE_NAME_SETTING.getKey(), nodeId.substring(0, 7)).build();
    }

    private final Lifecycle lifecycle = new Lifecycle();
    private final Settings settings;
    private final Environment environment;
    private final ArrayStore arrayStore;
    private final StoreSchemas storeSchemas;

    /**
     * Constructs a node with the given settings.
     *
     * @param preparedSettings Base settings to configure the node with
     */
    public Node(Settings preparedSettings) {
        this(InternalSettingsPreparer.prepareEnvironment(preparedSettings));
    }

    public Node(Environment environment) {
        final List<Closeable> resourcesToClose = new ArrayList<>(); // register everything we need to release in the case of an error
        boolean success = false;
        {
            // use temp logger just to say we are starting. we can't use it later on because the node name might not be set
            Logger logger = Loggers.getLogger(Node.class, NODE_NAME_SETTING.get(environment.settings()));
            logger.info("initializing ...");
        }

        //this.settings = environment.settings();

        try {

            Logger logger = Loggers.getLogger(Node.class);

            final JvmInfo jvmInfo = JvmInfo.jvmInfo();
            logger.info(
                    "version[{}], pid[{}], build[{}/{}], OS[{}/{}/{}], JVM[{}/{}/{}/{}]",
                    Version.displayVersion(Version.CURRENT, Build.CURRENT.isSnapshot()),
                    jvmInfo.pid(),
                    Build.CURRENT.shortHash(),
                    Build.CURRENT.date(),
                    Constants.OS_NAME,
                    Constants.OS_VERSION,
                    Constants.OS_ARCH,
                    Constants.JVM_VENDOR,
                    Constants.JVM_NAME,
                    Constants.JAVA_VERSION,
                    Constants.JVM_VERSION);
            logger.info("JVM arguments {}", Arrays.toString(jvmInfo.getInputArguments()));
            warnIfPreRelease(Version.CURRENT, Build.CURRENT.isSnapshot(), logger);

            if (logger.isDebugEnabled()) {
                logger.debug("using config [{}], data [{}], logs [{}]",
                        environment.configFile(), environment.dataFile(), environment.logsFile());
            }

            this.environment = environment;
            this.settings = environment.settings();


            ArrayStore as = new ArrayStore(environment);
            as.load();
            this.arrayStore = as;

            StoreSchemas storeSchemas = new StoreSchemas();
            storeSchemas.load();
            this.storeSchemas = storeSchemas;

            //TODO ActionModule 初始化nettyTransport controller actions
            //暂不引入actionModule，直接初始化


        } catch (Exception ex) {
            ex.printStackTrace();
            throw new BattlefieldException("failed to bind service", ex);
        }
    }

    public Node start(){
        if (!lifecycle.moveToStarted()) {
            return this;
        }

        Logger logger = Loggers.getLogger(Node.class, NODE_NAME_SETTING.get(settings));
        logger.info("starting ...");



        final UsageService usageService = new UsageService(environment.settings());
        CircuitBreakerService circuitBreakerService = new NoneCircuitBreakerService();

        UnaryOperator<RestHandler> restWrapper = r -> r;

        final RestController restController = new RestController(restWrapper,circuitBreakerService,usageService);
        initRestHandlers(environment.settings(),restController);

        // 启动netty端口
        Netty4HttpServerTransport httpServerTransport = new Netty4HttpServerTransport(environment.settings(),restController);
        httpServerTransport.start();

        return this;
    }


    static void warnIfPreRelease(final Version version, final boolean isSnapshot, final Logger logger) {
        if (!version.isRelease() || isSnapshot) {
            logger.warn(
                    "version [{}] is a pre-release version of Elasticsearch and is not suitable for production",
                    Version.displayVersion(version, isSnapshot));
        }
    }



    @Override
    public void close() throws IOException {

        arrayStore.stop();
    }

    void initRestHandlers(Settings settings, RestController restController){
        new BlankAction(settings,restController);
        new SqlAction(settings,restController,arrayStore,storeSchemas);
        new SelectAction(settings,restController,arrayStore,storeSchemas);
    }


    /**
     * Hook for validating the node after network
     * services are started but before the cluster service is started
     * and before the network service starts accepting incoming network
     * requests.
     *
     * @param context               the bootstrap context for this node
     * @param boundTransportAddress the network addresses the node is
     *                              bound and publishing to
     */
    @SuppressWarnings("unused")
    protected void validateNodeBeforeAcceptingRequests(
            final BootstrapContext context,
            final BoundTransportAddress boundTransportAddress, List<BootstrapCheck> bootstrapChecks) throws Exception {
    }

}
