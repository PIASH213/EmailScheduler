package com.emailscheduler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Properties;

public class EmailSchedulerGUI {
    private final JFrame frame = new JFrame("Email Scheduler");
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Recipient","Subject","Time"}, 0
    );
    private final EmailSender sender;

    public EmailSchedulerGUI(EmailSender sender) {
        this.sender = sender;
        setup();
        load();
        frame.setVisible(true);
    }

    private void setup() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,400);

        JPanel in = new JPanel(new GridLayout(0,2,5,5));
        JTextField r = new JTextField(), s = new JTextField(), t = new JTextField();
        JTextArea b = new JTextArea(3,20);
        in.add(new JLabel("Recipient:")); in.add(r);
        in.add(new JLabel("Subject:"));   in.add(s);
        in.add(new JLabel("Body:"));      in.add(new JScrollPane(b));
        in.add(new JLabel("Time (yyyy-MM-dd hh:mm AM/PM):")); in.add(t);

        JButton sch=new JButton("Schedule"), ref=new JButton("Refresh");
        sch.addActionListener(e->onSchedule(r,s,b,t));
        ref.addActionListener(e->load());

        JTable table=new JTable(model);
        frame.add(in, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel btn=new JPanel(); btn.add(sch); btn.add(ref);
        frame.add(btn, BorderLayout.SOUTH);
    }

    private void onSchedule(JTextField r,JTextField s,JTextArea b,JTextField t){
        try {
            String ti = t.getText().trim().toUpperCase();
            if (!ti.matches("\\d{4}-\\d{2}-\\d{2} (0[1-9]|1[0-2]):\\d{2} [AP]M"))
                throw new IllegalArgumentException("Bad format");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
            LocalDateTime dt = LocalDateTime.parse(ti, fmt);
            if (dt.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("Must be future");

            Schedule sch = new Schedule();
            sch.setRecipient(r.getText().trim());
            sch.setSubject(s.getText().trim());
            sch.setBody(b.getText().trim());
            sch.setTime(dt);

            List<Schedule> list = ScheduleManager.loadSchedules();
            list.add(sch);
            ScheduleManager.saveSchedules(list);
            Scheduler.scheduleEmail(sender, sch);

            load();
            r.setText(""); s.setText(""); b.setText(""); t.setText("");
            JOptionPane.showMessageDialog(frame, "Scheduled!");

        } catch (DateTimeParseException|IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frame, "Error: "+ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: "+ex.getMessage());
        }
    }

    private void load() {
        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        for (Schedule sch : ScheduleManager.loadSchedules()) {
            model.addRow(new Object[]{
                    sch.getRecipient(), sch.getSubject(), sch.getTime().format(fmt)
            });
        }
    }

    public static void main(String[] args) {
        Properties props = new Properties();
        try (InputStream in = EmailSchedulerGUI.class
                .getClassLoader().getResourceAsStream("config.properties")) {
            props.load(in);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Cannot load config.properties");
            return;
        }
        EmailSender sender = new EmailSender(
                props.getProperty("email.username"),
                props.getProperty("email.password")
        );
        SwingUtilities.invokeLater(() -> new EmailSchedulerGUI(sender));
    }
}
