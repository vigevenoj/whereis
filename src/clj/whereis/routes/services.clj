(ns whereis.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [compojure.api.meta :refer [restructure-param]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]
            [whereis.owntracks.core :as owntracks]
            [clojure.tools.logging :as log]))

(defn access-error [_ _]
  (unauthorized {:error "unauthorized"}))

(defn wrap-restricted [handler rule]
  (restrict handler {:handler  rule
                     :on-error access-error}))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-restricted rule]))

(defmethod restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

; changes to this schema need to change the keys selected in whereis.owntracks.core
(s/defschema LocationUpdate
  {:username s/Str
   :device s/Str
   :lat s/Num
   :lon s/Num
   :tst s/Num})

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "whereis API"
                           :description "Services to provide location-tracking information"}}}}

  (context "/api" []
    :tags ["api"]
    (GET "/whereis/:username" [username]
      :return LocationUpdate
      :path-params [username :- String]
      :summary "location of a user"
      (if (owntracks/have-location-for? username)
        (do
          (log/warn (str "getting location for user " username))
          (ok (assoc (owntracks/get-latest-location username) :username username)))
        (not-found)))

    (GET "/authenticated" []
      :auth-rules authenticated?
      :current-user user
      (ok {:user user}))))
