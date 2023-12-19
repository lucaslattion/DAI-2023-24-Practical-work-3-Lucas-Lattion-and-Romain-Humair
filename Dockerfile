# Start from the Java 17 Temurin image
FROM eclipse-temurin:17

# Set the working directory
WORKDIR /app

# Copy the jar file
COPY target/java-udp-programming-1.0-SNAPSHOT.jar /app/java-udp-programming-1.0-SNAPSHOT.jar

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "java-udp-programming-1.0-SNAPSHOT.jar"]

# Set the default command
CMD ["--help"]

EXPOSE 5050
EXPOSE 5051
