package com.soyoung.battle.field.bootstrap;

import com.soyoung.battle.field.BattlefieldException;
import com.soyoung.battle.field.ThreadFactoryImpl;
import com.soyoung.battle.field.Version;
import com.soyoung.battle.field.common.Constants;
import com.soyoung.battle.field.common.logging.Loggers;
import com.soyoung.battle.field.common.setting.Settings;
import com.soyoung.battle.field.env.Environment;
import com.soyoung.battle.field.monitor.jvm.JvmInfo;
import com.soyoung.battle.field.monitor.os.OsProbe;
import com.soyoung.battle.field.monitor.process.ProcessProbe;
import com.soyoung.battle.field.node.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bootstrap {

    private static volatile Bootstrap INSTANCE;

    private volatile Node node;

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

        // force the class initializer for BootstrapInfo to run before
        // the security manager is installed
        BootstrapInfo.init();

        INSTANCE = new Bootstrap();

        INSTANCE.setup(true, environment);

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

        node.start();
        keepAliveThread.start();
    }

    static void stop() throws IOException {
        try {
            //TODO 关闭资源
            INSTANCE.stop2();
        } finally {
            INSTANCE.keepAliveLatch.countDown();
        }
    }

    private void stop2(){
        try {
            node.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setup(boolean addShutdownHook, Environment environment) throws BootstrapException {
        Settings settings = environment.settings();

        initializeNatives(
                environment.tmpFile(),
                BootstrapSettings.MEMORY_LOCK_SETTING.get(settings),
                BootstrapSettings.SYSTEM_CALL_FILTER_SETTING.get(settings),
                BootstrapSettings.CTRLHANDLER_SETTING.get(settings));

        // initialize probes before the security manager is installed
        initializeProbes();

        if (addShutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {

                        //TODO 关闭资源

                        LoggerContext context = (LoggerContext) LogManager.getContext(false);
                        Configurator.shutdown(context);
                    } catch (Exception ex) {
                        throw new BattlefieldException("failed to stop node", ex);
                    }
                }
            });
        }

        node = new Node(environment);
    }

    /** initialize native resources */
    public static void initializeNatives(Path tmpFile, boolean mlockAll, boolean systemCallFilter, boolean ctrlHandler) {
        final Logger logger = Loggers.getLogger(Bootstrap.class);

        // check if the user is running as root, and bail
        if (Natives.definitelyRunningAsRoot()) {
            throw new RuntimeException("can not run battlefield as root");
        }

        // enable system call filter
        if (systemCallFilter) {
            Natives.tryInstallSystemCallFilter(tmpFile);
        }

        // mlockall if requested
        if (mlockAll) {
            if (Constants.WINDOWS) {
                Natives.tryVirtualLock();
            } else {
                Natives.tryMlockall();
            }
        }

        // listener for windows close event
        if (ctrlHandler) {
            Natives.addConsoleCtrlHandler(new ConsoleCtrlHandler() {
                @Override
                public boolean handle(int code) {
                    if (CTRL_CLOSE_EVENT == code) {
                        logger.info("running graceful exit on windows");
                        try {
                            Bootstrap.stop();
                        } catch (IOException e) {
                            throw new BattlefieldException("failed to stop node", e);
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        // force remainder of JNA to be loaded (if available).
//        try {
//            JNAKernel32Library.getInstance();
//        } catch (Exception ignored) {
//            // we've already logged this.
//        }

//        Natives.trySetMaxNumberOfThreads();
//        Natives.trySetMaxSizeVirtualMemory();
//        Natives.trySetMaxFileSize();

        // init lucene random seed. it will use /dev/urandom where available:
        //StringHelper.randomId();
    }

    static void initializeProbes() {
        // Force probes to be loaded
        ProcessProbe.getInstance();
        OsProbe.getInstance();
        JvmInfo.jvmInfo();
    }
}
