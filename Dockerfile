# Use a lightweight Java 17 runtime image
FROM eclipse-temurin:17-jre

# Create an app directory
WORKDIR /app

# Copy your libraries (JavaMail, Gson) into /app/lib
COPY lib/*.jar ./lib/

# Copy your application JAR
COPY out/artifacts/EmailSchedulerGUI_jar/EmailSchedulerGUI.jar ./app.jar

# Copy data files
COPY schedules.json .
COPY email_logs.txt .

# Command to run with classpath support and Render's PORT
CMD ["java", "-cp", "app.jar:lib/*", "com.emailscheduler.WorkerMain"]
