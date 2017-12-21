package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;

/**
 * It is implementation of notification service. Runs in console mode by executing jar
 * with command line parameters args[0] as portNumber (value: '1-65535'), not
 * necessary args[1] as hostname or IP address (if blank, default value: '127.0.0.1').
 * Service opens ServerSocket at hostname:portNumber and waits JSONObject in format:
 * <p>
 * [external_id: (String), message: (String), time (LocalDateTime), notification_type (String)]
 * notification_type can be as one of patterns like:
 * <p>
 * 'url=http://...'
 * 'email=email@email.ru'
 * <p>
 * After receiving JSONObject service adds time task to queue and after time (JSONObject.time - time.now)
 * sends message to email or requests http GET from url depending on notification_type.
 * <p>
 * Maven goals:
 * <p>
 * 1. To start server - 'mvn clean package exec:java "-Dexec.args=8090 127.0.0.1"'
 * 2. To make executable - 'mvn clean compile assembly:single'
 *
 * @author Aletzal
 */
public class NotifyServer {
    private static final Logger log = LoggerFactory.getLogger(NotifyServer.class);

    public static int tasks = 0;
    private ServerSocket serverSocket;
    private String hostIP = "127.0.0.1";
    private int portNumber;

    /**
     * Opens ServerSocket
     *
     * @throws IOException
     */
    public void establish() throws IOException {
        serverSocket = new ServerSocket(portNumber, 50, Inet4Address.getByName(hostIP));
        log.info("==== Notification Service has been established on " + serverSocket.getLocalSocketAddress() + " ====");
        log.info("========================== Waiting for connection ===========================");
    }

    /**
     * Accepts incoming connection and runs new thread NotTask within ThreadGroup "NotificationServices"
     *
     * @throws IOException
     */
    public void accept() throws IOException {
        ThreadGroup tgroup = new ThreadGroup("NotificationServicesGroup");
        while (true) {
            Socket socket = serverSocket.accept();
            Runnable r = new NotifyTask(socket);
            new Thread(tgroup, r).start();
        }
    }

    /**
     * Starts Server
     *
     * @param args (args[0] - port number, args[1] - IP address), args[1] may be blank
     *             then will be used default value: IP address - 127.0.0.1
     * @throws IOException
     */
    public void start(String... args) throws IOException {
        if ((args.length != 0) && (args.length == 1) || (args.length == 2)) {
            portNumber = Integer.parseInt(args[0]);
            if (args.length == 2) hostIP = args[1];
            establish();
            accept();
        }
        log.error("Usage: java NotifyServer <port number> <IP address> (default IP address '127.0.0.1' if blank)");
        System.exit(1);
    }

    public static void main(String[] args) {
        try {
            new NotifyServer().start(args);
        } catch (BindException e) {
            log.error("Bad address/port, please use format 'aaa.bbb.ccc.ddd'. " + e.getLocalizedMessage());
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            System.exit(1);
        }
    }
}
