(ns whereis.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [whereis.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[whereis started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[whereis has shut down successfully]=-"))
   :middleware wrap-dev})
