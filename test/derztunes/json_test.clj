(ns derztunes.json-test
  (:require [clojure.test :refer [deftest is testing]]
            [derztunes.json :as json]))

(deftest test-response
  (testing "Basic response"
    (is (= (json/response {:foo "bar"})
           {:status 200
            :body "{\"foo\":\"bar\"}"
            :headers {"Content-Type" "application/json"}})))
  (testing "Response with status"
    (is (= (json/response {:foo "bar"} 404)
           {:status 404
            :body "{\"foo\":\"bar\"}"
            :headers {"Content-Type" "application/json"}}))))
