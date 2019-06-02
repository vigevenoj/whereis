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