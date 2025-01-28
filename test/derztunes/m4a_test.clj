(ns derztunes.m4a-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [derztunes.m4a :as m4a]))

(deftest test-parse-metadata
  (testing "Basic metadata"
    (let [res (io/resource "testdata/test.m4a")
          file (io/file res)]
      (is (= (m4a/parse-metadata file)
             {:duration 728
              :track nil
              :title "Symphony No.6 (1st movement)"
              :artist "Ludwig van Beethoven"
              :album "www.mfiles.co.uk"})))))
