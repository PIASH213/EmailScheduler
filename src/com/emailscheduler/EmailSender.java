package com.emailscheduler;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailSender {
    private final String username;
    private final String password;
    private final Properties mailProperties;
    private static final DateTimeFormatter LOG_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EmailSender(String username, String password) {
        this.username = username;
        this.password = password;
        this.mailProperties = configureMailProperties();
    }

    public void sendEmail(String to, String subject, String body) throws EmailException {
        Session session = createMailSession();
        Message message = createMessage(session, to, subject, body);

        try {
            Transport.send(message);
            logEmail(to, subject, true, null);
        } catch (MessagingException e) {
            String errorMsg = "Failed to send email: " + e.getMessage();
            logEmail(to, subject, false, errorMsg);
            throw new EmailException(errorMsg, e);
        }
    }

    private Session createMailSession() {
        Session session = Session.getInstance(mailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebug(true);  // <<< enable SMTP debug (prints protocol to console)
        return session;
    }

    private Message createMessage(Session session, String to, String subject, String body)
            throws EmailException {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);
            return message;
        } catch (MessagingException e) {
            throw new EmailException("Failed to create email message", e);
        }
    }

    private Properties configureMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        return props;
    }

    private void logEmail(String to, String subject, boolean success, String error) {
        String logEntry = String.format("[%s] %s | To: %s | Subject: %s | Status: %s%s%n",
                LocalDateTime.now().format(LOG_FORMATTER),
                username,
                to,
                subject,
                success ? "SUCCESS" : "FAILED",
                error != null ? " | Error: " + error : ""
        );

        // Print to console
        System.out.print(logEntry);

        // Append to log file
        try (FileWriter writer = new FileWriter("email_logs.txt", true)) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write to email log: " + e.getMessage());
        }
    }

    public static class EmailException extends Exception {
        public EmailException(String message) {
            super(message);
        }
        public EmailException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
