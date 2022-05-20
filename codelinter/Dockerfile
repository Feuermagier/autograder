FROM openjdk:17-slim

ARG jarfile
ARG executor
ARG agent

RUN useradd --uid 10000 -m student
COPY $executor /home/student/Executor.jar
COPY $agent /home/student/Agent.jar
COPY $jarfile /home/student/studentcode.jar
USER student
ENTRYPOINT ["java", "-jar", "/home/student/Executor.jar"]
