(ns whereis.owntracks.core
  (:require
    [clojure.tools.logging :as log]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [cheshire.core :refer :all]
    [whereis.config :refer [env]]
    [clojurewerkz.machine-head.client :as mh]
    [mount.core :refer [defstate]]
    [clojure.tools.logging :as log])
  (:import (java.io InputStream)
           (java.security KeyStore)
           (javax.net.ssl SSLContext)
           (javax.net.ssl TrustManagerFactory)))

; It would make sense to use a library like less-awful-ssl
; https://github.com/aphyr/less-awful-ssl or use their implementation
; like https://github.com/aphyr/less-awful-ssl/blob/master/src/less/awful/ssl.clj
; in order to do this right and handle ssl/tls connections in a sane manner
; Otherwise, maybe it makes sense to ignore TLS for development, and set the
; right environment variables to specify a keystore to use as a truststore in production
(defn get-socketfactory-from-keystore-inputstream
  ""
  [^InputStream inputstream]
  (let [^KeyStore trust-store (KeyStore/getInstance (KeyStore/getDefaultType))
        truststore-password (env :trustStorePassword)
        ^TrustManagerFactory trust-factory (TrustManagerFactory/getInstance (TrustManagerFactory/getDefaultAlgorithm))]
    (.init trust-factory trust-store)
    (let [trust-managers (.getTrustManagers trust-factory)
          ^SSLContext ssl-context (SSLContext/getInstance "TLSv1.2")]
      (.load trust-store inputstream (.toCharArray truststore-password))
      (.init ssl-context nil trust-managers nil)
      (SSLContext/setDefault ssl-context)
      (.getSocketFactory (SSLContext/getDefault)))))

; use http://www.luminusweb.net/docs/components.md as a reference:
; here we can call (mh/generate-id)
; and define a connection (mh/connect (-> config :mqtt :url) id)
; and subscribe via (mh/subscribe conn {(-> config :mqtt :topic) 0}
; and define a function to call to handle mqtt messages on our topics

(def locations
  (atom {}))

(defn username-from-topic
  "Given an MQTT topic named owntracks/username/device, parse the username out of the topic"
  [topic]
  (first (rest (str/split topic #"/"))))

(defn device-from-topic
  "Given an MQTT topic named owntracks/username/device, parse the device out of the topic"
  [topic]
  (last (str/split topic #"/")))

(defn have-location-for?
  "Return true if we have recorded a location for the provided username, else false"
  [username]
  (contains? @locations (keyword username)))

(defn update-latest-location
  "Update the latest location for a user."
  ; turn username into key, associate new-location with that, like
  ; {:user1 {user-1-location-data} :user2 {user-2-location-data} }
  [username new-location]
  (swap! locations assoc (keyword username) new-location))

(defn get-latest-location [username]
  "Return the latest location for [username]"
  (let [key (keyword username)]
    ; these keys are selected to match the schema in whereis.routes.services
    (select-keys (-> @locations key) [:device :lat :lon :tst])))

(defn handle-owntracks-update
      "Handle a single location update"
      [^String topic meta ^bytes payload]
  (do
    (log/warn (str topic " " (String. payload "UTF-8"))))
    ; what i want to do here is to split the topic from owntracks/username/device
    ; so that i can put the "device" part of the topic into the map of data that
    ; will be saved for that user (so owntracks/jacob/iphone and owntracks/jacob/spot
    ; end up being the location data for :jacob and i can tell which device most-recently
    ; sent an update
    (update-latest-location (username-from-topic topic)
                            (assoc (parse-string (String. payload "UTF-8") true)
                              (keyword "device")
                              (device-from-topic topic))))


(defstate mqtt
          :start (let [broker-url (env :broker-url)
                       topic (env :owntracks-topic)
                       mqtt (mh/connect broker-url {:client-id "whereis-test"
                                                    :username (env :mqtt-username)
                                                    :password (env :mqtt-password)
                                                    ;:socket-factory (.getSocketFactory (SSLContext/getDefault))
                                                    ;:socket-factory (get-socketfactory-from-keystore-inputstream
                                                    ;                  (io/input-stream
                                                    ;                    (java.io.File.
                                                    ;                      (env :trustStorePath))))
                                                    })]
                   (do
                     (mh/subscribe mqtt {topic 0} handle-owntracks-update)
                     mqtt))
          ;:start (if-let [broker-url (env :broker-url)]
          ;         (do
          ;           (let [*mqtt* (mh/connect broker-url "whereis")]
          ;           (if-let [topic (env :owntracks-topic)]
          ;             (mh/subscribe *mqtt* {topic 0} handle-owntracks-update)
          ;             (log/warn "mqtt topic was not found, please set :owntracks-topic in your config, e.g: dev-config.edn")))
          ;           *mqtt*)
          ;         (do
          ;           (log/warn "mqtt broker URL was not found, please set :broker-url in your config, e.g: dev-config.edn"))
          ;         *mqtt*)
          :stop (mh/disconnect mqtt))

