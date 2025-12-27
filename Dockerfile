FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/tutor-courses-0.0.1-SNAPSHOT.jar tutor-courses.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/tutor-courses.jar"]
