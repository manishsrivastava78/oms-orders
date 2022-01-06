# Base Alpine Linux based image with OpenJDK JRE only
FROM openjdk
# copy application JAR (with libraries inside)
COPY target/oms-order-1.0.jar /oms-order-1.0.jar
# specify default command
CMD ["/usr/bin/java", "-jar",  "/oms-order-1.0.jar"]
