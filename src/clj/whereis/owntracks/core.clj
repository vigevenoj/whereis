(ns whereis.owntracks.core
  (:require
  [clojure.tools.logging :as log]
  [whereis.config :refer [env]]
  [clojurewerkz.machine-head.client :as mh]
  [mount.core :refer [defstate]]
  [clojure.tools.logging :as log]))

; use http://www.luminusweb.net/docs/components.md as a reference:
; here we can call (mh/generate-id)
; and define a connection (mh/connect (-> config :mqtt :url) id)
; and subscribe via (mh/subscribe conn {(-> config :mqtt :topic) 0}
; and define a function to call to handle mqtt messages on our topics

(def locations
  (atom {}))

(defn update-latest-location
  "Update the latest location for a user."
  ; turn username into key, associate new-location with that, like
  ; {:user1 {user-1-location-data} :user2 {user-2-location-data} }
  [username new-location]
  (swap! locations assoc (keyword username) new-location))

(defn get-latest-location [username]
  "Return the latest location for [username]"
  (let [key (keyword username)]
    (-> @locations key)))

(defn handle-owntracks-update [^String topic meta ^bytes payload]
  (do
    (update-latest-location topic (String. payload "UTF-8"))
      (log/warn (String. payload "UTF-8"))))

(defstate mqtt
          :start (let [broker-url (env :broker-url)
                       topic (env :owntracks-topic)
                       mqtt (mh/connect broker-url "conn")]
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

