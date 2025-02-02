(ns derztunes.m3u-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [derztunes.m3u :as m3u]))

(deftest parse-m3u
  (testing "Basic parsing"
    (let [res (io/resource "testdata/test.m3u")
          text (slurp res)]
      (is (= (m3u/parse-m3u text)
             [{:playlist-track/number 1, :playlist-track/path "test/01 Track Foo.mp3"}
              {:playlist-track/number 2, :playlist-track/path "test/02 Track Bar.mp3"}
              {:playlist-track/number 3, :playlist-track/path "test/03 Track Baz.mp3"}])))))
