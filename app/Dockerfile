FROM openjdk:12-alpine
COPY mysql-connector-java-8.0.18.jar /
COPY src/ /usr/src/atea/
WORKDIR /usr/src/atea/

RUN javac Test.java

CMD ["java", "Test"]
