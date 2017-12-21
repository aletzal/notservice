package com.example;

import org.json.JSONObject;

import java.io.*;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

/**
 * Simple implementation of TCP socket client.
 * Sends static JSONObject.
 * Made for test purposes only.
 * <p>
 * Maven goals:
 * <p>
 * 1. To start client - 'mvn clean package exec:java "-Dexec.args=8090 127.0.0.1"'
 * 2. To make executable - 'mvn clean compile assembly:single'
 */
public class NotifyClient {

    private String host;
    private int port;
    private Socket socket;
    private final String DEFAULT_HOST = "localhost";


    public void connect(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
        System.out.println("Client has been connected...");
    }

    public void sendJSON() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("external_id", "1");
        jsonObject.put("message", "New message 1");
        jsonObject.put("time", LocalDateTime.now().plusSeconds(5L));
        jsonObject.put("notification_type", "email=comexample@yandex.ru");

        OutputStream out = socket.getOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(out);
        o.writeObject(jsonObject.toString());
        out.flush();
        System.out.println("Sent to server: " + jsonObject.toString());

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("external_id", "2");
        jsonObject2.put("message", "New message 2");
        jsonObject2.put("time", LocalDateTime.now().plusSeconds(10L));
        jsonObject2.put("notification_type", "url=https://www.ya.ru");
        o.writeObject(jsonObject2.toString());
        out.flush();
        System.out.println("Sent to server: " + jsonObject2.toString());
    }

    public static void main(String[] args) {
        NotifyClient client = new NotifyClient();
        if (args.length != 2) {
            System.err.println("Usage: java NotifyClient <port number> <IP address> (port 1-65535)");
            System.exit(1);
        }
        client.port = Integer.parseInt(args[0]);
        client.host = args[1];
        try {

            client.connect(client.host, client.port);
            client.sendJSON();

        } catch (ConnectException e) {
            System.err.println(client.host + " connect refused");
            return;
        } catch (UnknownHostException e) {
            System.err.println(client.host + " Unknown host");
            client.host = client.DEFAULT_HOST;
            return;
        } catch (NoRouteToHostException e) {
            System.err.println(client.host + " Unreachable");
            return;

        } catch (IllegalArgumentException e) {
            System.err.println(client.host + " wrong port");
            return;
        } catch (IOException e) {
            System.err.println(client.host + ' ' + e.getMessage());
            System.err.println(e);
        } finally {
            try {
                client.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}