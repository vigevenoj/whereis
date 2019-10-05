# whereis

This provides an API to get location-tracking data from an MQTT broker

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed in order to build this project.

You will need an MQTT broker and some OwnTracks-compatible location data published to it in order to have any useful output. 

[1]: https://github.com/technomancy/leiningen

## Configuration

You'll need to provide a config.edn with appropriate values:

```
{:dev false
 :port 3000
 :nrepl-port 7000
 :broker-url "tcp://host:port"
 :owntracks-topic "owntracks/#"
 :mqtt-username ""
 :mqtt-password ""}
```
 
 If using TLS and the JVM's default trust store, something like this will work:
 ```
 {:dev false
 :port 3000
 :nrepl-port 7000
 :broker-url "ssl://host:port"
 :owntracks-topic "owntracks/#"
 :mqtt-username ""
 :mqtt-password ""}
```
 
 Using a broker with a certificate signed by an untrusted authority or a self-signed certificate:
 ```
  {:dev false
  :port 3000
  :nrepl-port 7000
  :broker-url "ssl://host:port"
  :broker-ca-cert-path "/path/to/certificate.crt"
  :owntracks-topic "owntracks/#"
  :mqtt-username ""
  :mqtt-password ""
  }
```

Alternatively, environment variables can be used to configure the application, eg,
```
WORKER_THREADS=4 IO_THREADS=4 IO__HTTP_MAX_CONNECTIONS=5 NREPL_PORT=7000 BROKER_URL=tcp://example.org:1883 OWNTRACKS_TOPIC=owntracks/# MQTT_USERNAME=username MQTT_PASSWORD=password java -Xmx60m -jar target/uberjar/whereis.jar
```

If you are using a custom certificate for your MQTT broker, or the certificate you need is not trusted by the default Java trust store, you can specify the path to the public certificate that signed your MQTT broker's certificate with the `broker-ca-cert-path` option in the config.edn, or with `BROKER_CA_CERT_PATH` environment variable. 
If this variable is unset, the application will use the default JVM trust store.
The broker CA certificate must be provided in PEM format.


## Running for local development

To start the application's web-server, run:

    lein run

In a second terminal, run:

    lein figwheel

## Packaging
To package the application

    lein uberjar
    
 ## Running in production
 Either add `-Dconfig=/path/to/config.edn` to the command you're starting the jvm with, or set all the environment variables and then `java -jar`.
 
 The API does not require authentication, so you may want to use something in front of this application to limit access.