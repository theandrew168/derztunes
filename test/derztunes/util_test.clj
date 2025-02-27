(ns derztunes.util-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [derztunes.util :as util]
            [java-time.api :as jt]))

(deftest test-in?
  (testing "If an item is in a collection"
    (is (util/in? [1 2 3] 2))
    (is (not (util/in? [1 2 3] 4)))
    (is (util/in? #{1 2 3} 2))
    (is (not (util/in? #{1 2 3} 4)))))

(deftest test-get-or
  (testing "Getting a value from a map or a default value"
    (is (= (util/get-or {:port "8080"} :port "5000")
           "8080"))
    (is (= (util/get-or {:port ""} :port "5000")
           "5000"))
    (is (= (util/get-or {} :port "5000")
           "5000"))))

(deftest test-base-name
  (testing "Extraction of base name from a path"
    (is (= (util/base-name "/path/to/foo")
           "foo"))
    (is (= (util/base-name "/path/to/foo/bar.txt")
           "bar.txt"))))

(deftest test-split-ext
  (testing "Splitting of a file name into name and extension"
    (is (= (util/split-ext "foo.txt")
           ["foo" ".txt"]))
    (is (= (util/split-ext "foo.bar.txt")
           ["foo" ".bar.txt"]))
    (is (= (util/split-ext "foo")
           ["foo" ""]))
    (is (= (util/split-ext "foo.")
           ["foo" ""]))))

(deftest test-mp3-file?
  (testing "If a file path points to an MP3 file"
    (is (util/mp3-file? "/path/to/foo.mp3"))
    (is (not (util/mp3-file? "/path/to/foo.m4a")))))

(deftest test-m4a-file?
  (testing "If a file path points to an M4A file"
    (is (util/m4a-file? "/path/to/foo.m4a"))
    (is (not (util/m4a-file? "/path/to/foo.mp3")))))

(deftest test-audio-file?
  (testing "If a file path points to an audio file"
    (is (util/audio-file? "/path/to/foo.mp3"))
    (is (util/audio-file? "/path/to/foo.m4a"))
    (is (not (util/audio-file? "/path/to/foo.txt")))))

(deftest test-back-slash->forward-slash
  (testing "Conversion of back slashes to forward slashes"
    (is (= (util/back-slash->forward-slash "C:\\Users\\foo\\bar")
           "C:/Users/foo/bar"))))

(deftest test-hours->seconds
  (testing "Conversion of hours to seconds"
    (is (= (util/hours->seconds 1)
           3600))
    (is (= (util/hours->seconds 2)
           7200))))

(deftest test-seconds-from
  (testing "Seconds from a given time"
    (let [t (jt/instant)]
      (is (= (util/seconds-from t 60)
             (jt/plus t (jt/seconds 60)))))))

(deftest test-seconds-from-now!
  (testing "Seconds from now"
    (is (jt/after? (util/seconds-from-now! 60)
                   (jt/instant)))))

(deftest test-stream->bytes
  (testing "Conversion of a stream to bytes"
    (let [buf [1 2 3 4]
          in (io/input-stream (byte-array buf))]
      (is (= (seq (util/stream->bytes in)) buf)))))

(deftest test-ignore-errors
  (testing "Ignoring errors"
    (is (nil? (util/ignore-errors (/ 1 0)))
        (is (= (util/ignore-errors (/ 1 1))
               1)))))
