(ns whereis.owntracks.core
  :require
  [clojure.tools.logging :as log]
  [whereis.config :refer [env]]
  [clojurewerkz.machine-head.client :as mh]
  [mount.core :refer [defstate]]
  (:require [clojure.tools.logging :as log]))

; use http://www.luminusweb.net/docs/components.md as a reference:
; here we can call (mh/generate-id)
; and define a connection (mh/connect (-> config :mqtt :url) id)
; and subscribe via (mh/subscribe conn {(-> config :mqtt :topic) 0}
; and define a function to call to handle mqtt messages on our topics
(defstate ^:dynamic *mqtt*
          :start (if-let [broker-url (env :broker-url)
                          client-id (mh/generate-id)]
                   (mh/connect broker-url client-id)
                   (do
                     (log/warn "mqtt broker URL was not found, please set :broker-url in your config, e.g: dev-config.edn")
                     *mqtt*))
          :stop (mh/disconnect *mqtt*))