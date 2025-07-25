FROM eclipse-temurin:17-jre

WORKDIR /app

COPY lib/*.jar ./lib/
COPY out/artifacts/EmailSchedulerGUI_jar/EmailSchedulerGUI.jar ./app.jar
COPY schedules.json .
COPY email_logs.txt .

# Create default files if missing
RUN if [ ! -f schedules.json ]; then echo "[]" > schedules.json; fi
RUN if [ ! -f email_logs.txt ]; then touch email_logs.txt; fi

# Expose Render's default port
EXPOSE 10000

CMD ["java", "-cp", "app.jar:lib/*", "com.emailscheduler.WorkerMain"]
