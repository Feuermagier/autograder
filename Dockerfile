FROM maven:3-openjdk-18-slim AS build

WORKDIR /usr/src/app

# copy source code to the container
COPY . .

RUN mvn install
RUN mvn package

ENTRYPOINT ["/bin/sh", "-c"]
