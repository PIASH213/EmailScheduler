package com.emailscheduler;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleManager {
    private static final String SCHEDULE_FILE = "schedules.json";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    public static List<Schedule> loadSchedules() {
        try {
            File file = new File(SCHEDULE_FILE);
            if (!file.exists() || file.length() == 0) {
                Files.write(Paths.get(SCHEDULE_FILE), "[]".getBytes());
                return new ArrayList<>();
            }
            String json = new String(Files.readAllBytes(Paths.get(SCHEDULE_FILE)));
            return gson.fromJson(json, new TypeToken<List<Schedule>>(){}.getType());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static void saveSchedules(List<Schedule> schedules) {
        try (FileWriter writer = new FileWriter(SCHEDULE_FILE)) {
            gson.toJson(schedules, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save schedules: " + e.getMessage());
        }
    }
}
