(ns whereis.auth
  (:require [mount.core :as mount]
            [clojure.java.io :as io])
  (:import [org.keycloak.adapters KeycloakDeployment KeycloakDeploymentBuilder]
           [org.keycloak.representations AccessToken]
           [org.keycloak RSATokenVerifier]))


(defn load-keycloak-deployment
  ""
  ([]
   (load-keycloak-deployment "default-keycloak.json"))
  ([keycloak-json-file]
   (with-open [keycloak-json-is (io/input-stream (io/resource keycloak-json-file))]
     (KeycloakDeploymentBuilder/build keycloak-json-is))))

(mount/defstate keycloak-deployment
  :start (load-keycloak-deployment))

(defn verify
  ; "token" is the "access_token" from the json returned by the keycloak openid-connect token endpoint
  ; don't call this with the whole json blob
  [token]
  (let [kid "lWUvbrOQAxC8Pt_GYEyyxOo-ooZ3YLsWGiupHzfhLpA" ;; put in config file
        deployment keycloak-deployment
        public-key (.getPublicKey (.getPublicKeyLocator deployment) kid deployment)]
    (RSATokenVerifier/verifyToken token public-key (.getRealmInfoUrl deployment))))

(defn extract
  "return a map with :user and :roles keys with values extracted from the keycloak access token"
  [access-token]
  {:user (.getPreferredUsername access-token)
   :roles (set (map keyword (.getRoles (.getRealmAccess access-token))))})