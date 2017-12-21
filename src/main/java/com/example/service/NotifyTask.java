package com.example.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Thread NotifyTask handles established socket connection from Client and
 * tries to receive JSONObject, validate it and add time task to queue.
 * Used TimerTask.
 * Time to execute task is limited between 50ms < time < 1 week
 */
public class NotifyTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(NotifyTask.class);
    /* Let's say 1 week */
    public static final long MAX_FUTURE_SCHEDULING_MILLIS = 7l * 24l * 60l * 60l * 1000l;
    public static final String ZONE_OFFSET = "UTC+3";

    private Socket socket;

    public NotifyTask(Socket socket) {
        this.socket = socket;
    }

    private String getHTML(String urlToRead) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    private void sendYAEmail(String to, String message) {
        String from = "comexample@yandex.ru";

        // Create properties and get session
        Properties props = new Properties();

        // Using Yandex mailbox for test purposes
        props.put("mail.smtp.host", "smtp.yandex.ru");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        // Set authorization
        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            // Set credentials for source mail
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("comexample", "Password");
            }
        });

        try {
            // Creating message
            Message msg = new MimeMessage(session);

            // Setting message attributes
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = {new InternetAddress(to)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject("NOTIFICATION from NotificationService");
            msg.setSentDate(new Date());

            // Setting message body
            msg.setText(message + "\n" +
                    "Here is line 2.");

            // Send email
            Transport.send(msg);
            log.info("==================== Email message sent ====================");
        } catch (MessagingException e) {
            System.err.println(e);
        }
    }

    @Override
    public void run() {
        log.info("Accepted connection №" + NotifyServer.tasks + " from " + socket.toString());

        try (InputStream inputStream = socket.getInputStream()) {
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            Object obj = null;
            while ((obj = ois.readObject()) != null) {
                JSONObject jsonObject = new JSONObject(obj.toString());
                log.info("=============================== Got input Object ===============================");
                log.info(jsonObject.toString());
                /* Increase number of successful requests */
                NotifyServer.tasks++;

                /* ******************************************************************************* */
                /* Here can be Validation of JSON object, sending reply/errors to client and so on */
                /* ******************************************************************************* */

                /* But now we will check only time field */
                DateTimeFormatter json2ldtFormatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDateTime jsonldt = LocalDateTime.parse(jsonObject.get("time").toString(), json2ldtFormatter);
                ZonedDateTime jsonzdt = jsonldt.atZone(ZoneId.of(ZONE_OFFSET));
                long jsontime = jsonzdt.toInstant().toEpochMilli();

                /* Checking allowed future time execution for the task */
                long eventTime = jsontime - System.currentTimeMillis();
                if (eventTime < 50 || eventTime > MAX_FUTURE_SCHEDULING_MILLIS)
                    throw new UnsupportedOperationException("Can't schedule for " + eventTime + "ms from now");

                Timer timer = new Timer("TaskTimer");

                final TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("===================== TIMER " + NotifyServer.tasks + " TASK STARTED =====================");

                        String notType = jsonObject.getString("notification_type");
                        final int idx = notType.indexOf("=");
                        final String key = idx > 0 ? notType.substring(0, idx) : notType;
                        final String dest = notType.substring(idx + 1, notType.length());
                        if (key.equals("email")) {
                            log.info("=============== SENDING EMAIL ===============");
//              Uncomment to real send Email
//                            sendYAEmail(dest, jsonObject.getString("message"));
                        } else if (key.equals("url")) {
                            System.out.println("=============== SENDING HTTP/GET ===============");
//              Uncomment to execute HTTP GET
//                            try {
//                                System.out.println(getHTML(dest));
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }
                };
                timer.schedule(timerTask, eventTime);
            }
        } catch (IOException e) {
//            log.error("========================== Socket InputStream Error ===========================");
            log.error(e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("========================== Error reading JSON object ==========================");
            log.error(e.getMessage());
        }
    }
}
