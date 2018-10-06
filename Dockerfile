FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/whereis.jar /whereis/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/whereis/app.jar"]
