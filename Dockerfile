FROM clojure:lein AS BUILD_CONTAINER
RUN mkdir -p /usr/local/app
WORKDIR /opt/app
COPY . /opt/app
RUN lein uberjar

FROM adoptopenjdk:11-jdk-hotspot
MAINTAINER Jacob Vigeveno <jacob@sharkbaitextraordinaire.com>
COPY --from=BUILD_CONTAINER /opt/app/target/uberjar/whereis.jar /whereis/app.jar
EXPOSE 3000
CMD ["java", "-XX:+PrintFlagsFinal", "-XX:+UseContainerSupport", "-jar", "/whereis/app.jar"]
