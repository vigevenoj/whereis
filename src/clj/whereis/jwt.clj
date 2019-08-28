(ns whereis.jwt
  (:require
    [buddy.sign.jwt :as jwt]))

(def sign #(jwt/sign % "changeme" {:alg :hs512}))
(def unsign #(jwt/unsign % "changeme" {:alg :hs512}))

(defn create-token
  "Creates signed jwt token with use data as payload
  `valid-seconds` sets the expiration
  `terse?` includes only the user id in the payload"
  [user & {:keys [terse? valid-seconds]
           :or {terse? false?
                valid-seconds 7200}}] ; 2 hours
  (let [fields (if terse?
                 [:username]
                 [:username])
        payload (-> user
                    (select-keys fields)
                    (assoc :exp (.plusSeconds
                                  (java.time.Instant/now) valid-seconds)))]
    (sign payload)))