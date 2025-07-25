FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy dependencies
COPY lib/*.jar ./lib/

# Copy application JAR
COPY out/artifacts/EmailSchedulerGUI_jar/EmailSchedulerGUI.jar ./app.jar

# Copy data files (ensure they exist)
COPY schedules.json .
COPY email_logs.txt .

# Set default empty files if they don't exist in repo
RUN if [ ! -f schedules.json ]; then echo "[]" > schedules.json; fi
RUN if [ ! -f email_logs.txt ]; then touch email_logs.txt; fi

# Expose port
EXPOSE $PORT

CMD ["java", "-cp", "app.jar:lib/*", "com.emailscheduler.WorkerMain"]
