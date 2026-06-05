FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY build/libs/tutor-courses-1.0.0.jar tutor-courses.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","-Duser.timezone=Asia/Yekaterinburg","/app/tutor-courses.jar"]