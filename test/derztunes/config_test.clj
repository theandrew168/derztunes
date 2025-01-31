(ns derztunes.config-test
  (:require [clojure.test :refer [deftest is testing]]
            [derztunes.config :as config]))

(deftest test-read-port
  (testing "Reading a port from the environment with a default value"
    (is (= (config/read-port {"PORT" "8080"}) 8080))
    (is (= (config/read-port {"PORT" ""}) 5000))
    (is (= (config/read-port {}) 5000))
    (is (= (config/read-port {} "5001") 5001))))
