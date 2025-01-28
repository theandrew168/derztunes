(ns derztunes.m4a-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [derztunes.m4a :as m4a]))

(deftest test-parse-metadata
  (testing "Basic metadata"
    (let [res (io/resource "testdata/test.m4a")
          file (io/file res)]
      (is (= (m4a/parse-metadata file)
             {:track/track-number nil
              :track/title "Symphony No.6 (1st movement)"
              :track/artist "Ludwig van Beethoven"
              :track/album "www.mfiles.co.uk"})))))
