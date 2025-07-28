FROM amazoncorretto:21-alpine3.22

# Create a user to run the app (to not run as root)
RUN addgroup -S files_app && adduser -S files_app -G files_app
USER files_app:files_app

# Defines the home folder of the user as the default root folder for the app
ARG FILES_ROOT_PATH=/home/files_app
ENV FILES_ROOT_PATH=$FILES_ROOT_PATH

ARG JAR_FILE=build/libs/files-json-rpc-1.0.0.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]