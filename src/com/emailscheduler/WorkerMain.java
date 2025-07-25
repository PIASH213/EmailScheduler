package com.emailscheduler;

import java.io.*;
import java.net.InetAddress;
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

        // 3) Load and reschedule emails
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

        // 4) Start health check server (blocking call)
        startHealthCheckServer();

        // 5) The JVM will stay alive while health server is running
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
        int port = getServerPort();
        try (ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("✅ Health check server running on 0.0.0.0:" + port);
            
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                    
                    // Read request (we only care about the first line for health checks)
                    String request = in.readLine();
                    if (request != null && request.startsWith("GET")) {
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: text/plain");
                        out.println("Connection: close");
                        out.println();
                        out.println("Email Scheduler Running");
                    }
                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to start health server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getServerPort() {
        String port = System.getenv("PORT");
        if (port != null && !port.isEmpty()) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT value: " + port + ", using default 10000");
            }
        }
        return 10000; // Render's default port
    }
}
