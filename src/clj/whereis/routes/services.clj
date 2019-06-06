(ns whereis.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [ring.util.http-response :refer :all]
    [clojure.spec.alpha :as s]
;    [schema.core :as s]
    [buddy.auth.accessrules :refer [restrict]]
    [buddy.auth :refer [authenticated?]]
    [whereis.owntracks.core :as owntracks]
    [whereis.middleware.formats :as formats]
    [clojure.tools.logging :as log]
    [clojure.java.io :as io]))

; changes to this schema need to change the keys selected in whereis.owntracks.core
(s/def ::username string?)
(s/def ::device string?)
(s/def ::lat number?)
(s/def ::lon number?)
(s/def ::tst number?)
(s/def LocationUpdate (s/keys* :req [::username ::device ::lat ::lon ::tst]))

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
;                 exception/exeception-middleware ; this doens't exist yet
                 ;; decoding the requeset body
                 muuntaja/format-request-middleware
                 ;; coercing response bodies
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]} ; might not be needed

   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "whereis API"
                         :version "1.0.0"
                         :description "Services to provide location-tracking information"}}}
    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
            {:url "/api/swagger.json"
             :config {:validator-url nil}})}]]
   ["/whereis/:username"
    {:get {:summary "Location of a user"
           :parameters {:path {:username string?}}
;           :responses {200 {:body LocationUpdate}}
           :handler (fn [{:keys [parameters]}]
                      (let [username (-> parameters :path :username)]
                        (if (owntracks/have-location-for? username)
                          (do
                            (log/warn (str "getting location for user " ))
                            (ok (assoc (owntracks/get-latest-location username) :username username)))
                          (not-found))))}}]])

;(defapi service-routes
;  {:swagger {:ui "/swagger-ui"
;             :spec "/swagger.json"
;             :data {:info {:version "1.0.0"
;                           :title "whereis API"
;                           :description "Services to provide location-tracking information"}}}}
;
;  (context "/api" []
;    :tags ["api"]
;    (GET "/whereis/:username" [username]
;      :return LocationUpdate
;      :path-params [username :- String]
;      :summary "location of a user"
;      (if (owntracks/have-location-for? username)
;        (do
;          (log/warn (str "getting location for user " username))
;          (ok (assoc (owntracks/get-latest-location username) :username username)))
;        (not-found)))
;
;    (GET "/authenticated" []
;      :auth-rules authenticated?
;      :current-user user
;      (ok {:user user}))))
