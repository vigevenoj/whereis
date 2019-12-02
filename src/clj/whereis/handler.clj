(ns whereis.handler
  (:require [whereis.middleware :as middleware]
            [whereis.layout :refer [error-page]]
            [whereis.routes.home :refer [home-routes]]
            [whereis.routes.services :refer [service-routes]]
            [ring.util.http-response :as response]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring :as ring]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [whereis.env :refer [defaults]]
            [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
   (ring/ring-handler
    (ring/router
     [(home-routes)
      (service-routes)])
    (ring/routes
     (swagger-ui/create-swagger-ui-handler
      {:path   "/swagger-ui"
       :url    "/api/swagger.json"
       :securityDefinitions
       {:BasicAuth {:type "basic"}
        :ApiKeyAuth {:type "apiKey" :name "X-API-Key" :in "header"}}
       :config {:validator-url nil
                 }})
     (ring/create-resource-handler
      {:path "/"})
     (wrap-content-type
      (wrap-webjars (constantly nil)))
     (ring/create-default-handler
      {:not-found
       (constantly (error-page {:status 404, :title "404 - Page not found"}))
       :method-not-allowed
       (constantly (error-page {:status 405, :title "405 - Not allowed"}))
       :not-acceptable
       (constantly (error-page {:status 406, :title "406 - Not acceptable"}))})))))

