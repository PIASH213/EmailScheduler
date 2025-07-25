FROM eclipse-temurin:17-jre

WORKDIR /app

COPY lib/*.jar ./lib/
COPY out/artifacts/EmailSchedulerGUI_jar/EmailSchedulerGUI.jar ./app.jar
COPY schedules.json .
COPY email_logs.txt .

# Expose port for Render
EXPOSE $PORT

CMD ["java", "-cp", "app.jar:lib/*", "com.emailscheduler.WorkerMain"]
