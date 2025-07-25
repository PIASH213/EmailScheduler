package com.emailscheduler;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class WorkerMain {
    public static void main(String[] args) throws Exception {
        // 1) Load credentials: ENV vars override config.properties
        Properties props = new Properties();
        String user = System.getenv("EMAIL_USERNAME");
        String pass = System.getenv("EMAIL_PASSWORD");
        if (user == null || pass == null) {
            try (InputStream in = WorkerMain.class
                    .getClassLoader().getResourceAsStream("config.properties")) {
                if (in == null) {
                    System.err.println("ERROR: No env vars and no config.properties found.");
                    return;
                }
                props.load(in);
                user = props.getProperty("email.username");
                pass = props.getProperty("email.password");
            }
        }

        // 2) Build EmailSender
        EmailSender sender = new EmailSender(user, pass);

        // 3) Reschedule all saved emails on startup
        List<Schedule> all = ScheduleManager.loadSchedules();
        for (Schedule sch : all) {
            Scheduler.scheduleEmail(sender, sch);
            System.out.println("Rescheduled: " + sch.getRecipient() + " at " + sch.getTime());
        }

        // 4) Keep the JVM alive
        Thread.currentThread().join();
    }
}
