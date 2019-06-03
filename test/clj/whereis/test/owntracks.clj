(ns whereis.test.owntracks
  (:require [clojure.test :refer :all]
            [whereis.owntracks.core :as ot]))

(deftest parsing-of-usernames-and-devices
  (testing "we can get usernames from mqtt topics"
           (let [topic    "owntracks/username1/device1"
                 username (ot/username-from-topic topic)]
             (is (= "username1" username))))

  (testing "we can get device id out of mqtt topic"
           (let [topic  "owntracks/username1/device1"
                 device (ot/device-from-topic topic)]
             (is (= "device1" device)))))

(deftest handle-new-update
  (testing "owntracks-formatted mqtt message is parsed successfully"
           (let [topic "owntracks/jacob/iphone"
                 payload1 (.getBytes "{\"batt\":73,\"lon\":-123.593699576,\"acc\":65,\"p\":100.53157806396484,\"vac\":10,\"lat\":41.22265625,\"t\":\"u\",\"conn\":\"w\",\"tst\":1559508403,\"alt\":69,\"_type\":\"location\",\"tid\":\"jv\"}")
                 payload2 (.getBytes "{\"batt\":70,\"lon\":-123.928500307,\"acc\":65,\"p\":100.53157806396484,\"vac\":10,\"lat\":41.96386718,\"t\":\"u\",\"conn\":\"w\",\"tst\":1559534884,\"alt\":69,\"_type\":\"location\",\"tid\":\"jv\"}")]
             (do
               (ot/handle-owntracks-update topic nil payload1)
               (is (ot/have-location-for? "jacob"))
               (is (= 1559508403 (:tst (ot/get-latest-location "jacob"))))
               (ot/handle-owntracks-update topic nil payload2)
               (is (= 1559534884 (:tst (ot/get-latest-location "jacob"))))))))