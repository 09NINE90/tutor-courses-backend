FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY build/libs/tutor-courses-0.0.1-SNAPSHOT.jar tutor-courses.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/tutor-courses.jar"]

#серверный вариант
#FROM eclipse-temurin:21-jdk AS builder
#WORKDIR /app
#COPY . .
#RUN ./gradlew clean build -x test
#
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#COPY  --from=builder /app/build/libs/tutor-courses-0.0.1-SNAPSHOT.jar tutor-courses.jar
#EXPOSE 8082
#ENTRYPOINT ["java","-jar","/app/tutor-courses.jar"]
