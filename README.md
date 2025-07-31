# Running the application

## Via Gradle

You can run the application on your machine by running the following:

```shell
./gradlew bootRun
```

## Via Docker

### 1. Build the application

You first need to build the application, using the following command:

```shell
./gradlew build
```

## 2. Build the Docker image

Once the jar file has been built, you'll need to build your Docker image (changing the image name as fits):

```shell
docker build -t julienarz/files-json-rpc .
```

By default, the Docker image tries to find the 1.0.0 release of the app (which the current version built). But you can override which JAR you want to use for building the image by using the `JAR_FILE` argument.

## 3. Run the Docker image in a container

The port 8080 should be exposed on the Docker container. In order to persist the data over multiple run of that container, we should also mount a volume.

Example:
```shell
docker run -p 8080:8080 --volume files-rpc-json:/home/files_app julienarz/files-json-rpc
```

## Calling an endpoint

The server runs on localhost, on the port 8080 (or the port exposed from the Docker container).

An example HTTP request to the server would look like:
```shell
curl --request POST \
  --url http://localhost:8080/rpc/files \
  --header 'Content-Type: application/json' \
  --data '{
  "jsonrpc": "2.0",
  "method": "createFile",
  "params": ["test.txt"],
  "id": 1
}'
```

# Helm chart

The helm chart provided allows to deploy the application in Kubernetes.

It currently contains some hardcoded values in `values.yaml`:
- it's pulling the image from my personal Docker Hub for simplicity
- it's defining a Persistent Volume that needs to have been configured in the values before deploying this chart
  - the persistent volume needs to be a nfs server
  - with the current value, it's accessed through my local Minikube's IP