package com.emailscheduler;

import com.sun.net.httpserver.HttpServer;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

public class WorkerMain {
    public static void main(String[] args) throws Exception {
        // 1) Load credentials from ENV vars
        String user = System.getenv("EMAIL_USER");
        String pass = System.getenv("EMAIL_PASSWORD");
        
        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            System.err.println("ERROR: Missing email credentials in environment variables");
            return;
        }

        // 2) Build EmailSender
        EmailSender sender = new EmailSender(user, pass);
        System.out.println("Email sender initialized");

        // 3) Reschedule all saved emails
        List<Schedule> all = ScheduleManager.loadSchedules();
        System.out.println("Loaded " + all.size() + " schedules");
        for (Schedule sch : all) {
            Scheduler.scheduleEmail(sender, sch);
            System.out.println("Rescheduled: " + sch.getRecipient() + " at " + sch.getTime());
        }

        // 4) Start health check server
        startHealthCheckServer();

        // 5) Keep the JVM alive
        Thread.currentThread().join();
    }

    private static void startHealthCheckServer() {
        try {
            int port = getServerPort();
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", exchange -> {
                String response = "Email Scheduler Running";
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            server.setExecutor(null);
            server.start();
            System.out.println("Health check server running on port " + port);
        } catch (Exception e) {
            System.err.println("Failed to start health check server: " + e.getMessage());
        }
    }

    private static int getServerPort() {
        String port = System.getenv("PORT");
        if (port != null && !port.isEmpty()) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT value: " + port);
            }
        }
        return 8080; // Default port
    }
}
