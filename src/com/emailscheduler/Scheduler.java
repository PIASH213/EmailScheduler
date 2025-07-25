package com.emailscheduler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    public static void scheduleEmail(EmailSender emailSender, Schedule schedule) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime then = schedule.getTime().atZone(ZoneId.systemDefault());
        long delay = then.toInstant().toEpochMilli() - now.toInstant().toEpochMilli();

        System.out.println("⏰ Scheduling email for: " + then + "  (delay=" + delay + "ms)");

        if (delay < 0) {
            System.err.println("❌ Cannot schedule in the past.");
            return;
        }

        // Use a daemon timer so it won’t prevent JVM shutdown
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("▶️ TimerTask fired at " + ZonedDateTime.now(ZoneId.systemDefault()));
                try {
                    emailSender.sendEmail(
                            schedule.getRecipient(),
                            schedule.getSubject(),
                            schedule.getBody()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    timer.cancel();
                }
            }
        }, delay);
    }
}
