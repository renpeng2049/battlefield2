package com.soyoung.battle.field.bootstrap;

import com.soyoung.battle.field.ThreadFactoryImpl;
import com.soyoung.battle.field.Version;
import com.soyoung.battle.field.http.netty4.Netty4HttpServerTransport;
import com.soyoung.battle.field.rest.RestController;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    static void init() throws Exception {
        INSTANCE = new Bootstrap();

        //TODO 启动netty端口
        final RestController restController = new RestController();

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
        System.out.println(">>>>start");
        keepAliveThread.start();
    }

    static void stop() throws IOException {

    }
}
