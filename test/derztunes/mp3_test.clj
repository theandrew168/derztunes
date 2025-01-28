(ns derztunes.mp3-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [derztunes.mp3 :as mp3]))

(deftest test-parse-metadata
  (testing "Basic metadata"
    (let [res (io/resource "testdata/test.mp3")
          file (io/file res)]
      (is (= (mp3/parse-metadata file)
             {:track/track-number nil
              :track/title "Symphony No.6 (1st movement)"
              :track/artist "Ludwig van Beethoven"
              :track/album "www.mfiles.co.uk"})))))
