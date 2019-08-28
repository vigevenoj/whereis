(ns whereis.auth
  (:require
    [buddy.auth.backends :refer [jws]]
    [buddy.auth.backends.httpbasic :refer [http-basic-backend]]
    [buddy.hashers :as hashers]
    [whereis.jwt :as jwt]))

(defn basic-auth
  [_ request {:keys [username password]}]
  (let [user username]
    (-> user
        (dissoc password)
        (assoc :token (jwt/create-token user)))))

(defn basic-auth-backend
  [_]
  (http-basic-backend {:authfn (partial basic-auth _)}))

(def token-backend
  (jws {:secret "changeme" :options {:alg :hs512}}))