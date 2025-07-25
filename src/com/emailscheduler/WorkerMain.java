package com.emailscheduler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

        // 3) Load and reschedule emails - handle null case
        List<Schedule> allSchedules = loadSchedulesSafe();
        System.out.println("Loaded " + allSchedules.size() + " schedules");
        
        for (Schedule sch : allSchedules) {
            if (sch.getTime().isAfter(java.time.LocalDateTime.now())) {
                Scheduler.scheduleEmail(sender, sch);
                System.out.println("Rescheduled: " + sch.getRecipient() + " at " + sch.getTime());
            } else {
                System.out.println("Skipping past schedule: " + sch.getRecipient() + " at " + sch.getTime());
            }
        }

        // 4) Start health check server
        startHealthCheckServer();

        // 5) Keep the JVM alive
        Thread.currentThread().join();
    }

    private static List<Schedule> loadSchedulesSafe() {
        try {
            List<Schedule> schedules = ScheduleManager.loadSchedules();
            return schedules != null ? schedules : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error loading schedules: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void startHealthCheckServer() {
        new Thread(() -> {
            int port = getServerPort();
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Health check server running on port " + port);
                
                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                        
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: text/plain");
                        out.println("Connection: close");
                        out.println();
                        out.println("Email Scheduler Running");
                    } catch (IOException e) {
                        System.err.println("Client error: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to start health server: " + e.getMessage());
            }
        }).start();
    }

    private static int getServerPort() {
        String port = System.getenv("PORT");
        if (port != null && !port.isEmpty()) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT: " + port);
            }
        }
        return 8080;
    }
}
