(ns user
  (:require [whereis.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [whereis.figwheel :refer [start-fw stop-fw cljs]]
            [whereis.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'whereis.core/repl-server))

(defn stop []
  (mount/stop-except #'whereis.core/repl-server))

(defn restart []
  (stop)
  (start))


