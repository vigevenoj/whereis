# whereis

generated using Luminus version "3.0.9"

This provides an API to get location-tracking data from an MQTT broker

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Configuration

You'll need to provide a config.edn with appropriate values:

{:dev false
 :port 3000
 :nrepl-port 7000
 :broker-url "tcp://host:port"
 :owntracks-topic "owntracks/#"
 :mqtt-username ""
 :mqtt-password ""
 }

 There is some dead code for loading up a keystore as a custom trust store but that does not work

## Running

To start a web server for the application, run:

    lein run

In a second terminal, run:
    lein figwheel

