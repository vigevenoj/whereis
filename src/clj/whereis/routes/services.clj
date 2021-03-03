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
(s/def LocationUpdate (s/keys* :req-un [::username ::device ::lat ::lon ::tst]))

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
                         :description "Services to provide location-tracking information"}
                  :securityDefinitions
                  {:BasicAuth {:type "basic"}
                   :ApiKeyAuth {:type "apiKey" :name "X-API-Key" :in "header"}}}}
    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
            {:url "/api/swagger.json"
             :config {:validator-url nil}})}]
    ]
   ["/whereis" {:swagger {:tags ["locations"]}}
    ["/:username"
      {:get {:summary "Location of a user"
            :security [:BasicAuth :ApiKeyAuth]
            :parameters {:path {:username string?}}
            :responses {200 {:description "A location update"}
                        401 {:description "Not authorized"}
                        404 {:description "Not found, maybe unauthorized"}}
;            :middleware [whereis.middleware/auth]
            :handler (fn [{:keys [parameters]}]
                       (let [username (-> parameters :path :username)]
                         (if (owntracks/have-location-for? username)
                           (do
                             (log/warn (str "getting location for user " username))
                             {:status 200
                              :body (owntracks/get-latest-location username)})
                           {:status 404
                            :body {:error "not found"}})))}}]
    ]
   ["/debug" {:swagger {:tags ["debug"]}}
    ["/userinfo" {:get
                  {:summary "user info endpoint"
                   :handler (fn [request]
                              {:status 200
                               :body (:headers request)})}}]
    ["/login" {:get
               {:summary "auth test endpoint"
;                :middleware [(whereis.middleware/basic-auth nil) whereis.middleware/auth]
                :handler (fn [request]
                           {:status 200
                            :body (keys request)})}}]] ])
