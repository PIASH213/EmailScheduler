# Use a lightweight Java 17 runtime image
FROM eclipse-temurin:17-jre

# Create an app directory
WORKDIR /app

# Copy your libraries (JavaMail, Gson) into /app/lib
COPY lib/ /app/lib/

# Copy your “fat” JAR into /app/app.jar
COPY out/artifacts/EmailSchedulerGUI_jar/EmailSchedulerGUI.jar /app/app.jar

# Expose any ports if you had a web server (not needed here)
# EXPOSE 8080

# Let the container pick up your env‑vars at runtime
ENV EMAIL_USERNAME=$EMAIL_USERNAME
ENV EMAIL_PASSWORD=$EMAIL_PASSWORD

# Command to run your worker
ENTRYPOINT ["java", "-jar", "app.jar"]
