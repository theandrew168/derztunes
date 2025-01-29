(ns derztunes.m3u
  (:require [clojure.string :as str]))

(defn- back-slash->forward-slash [path]
  (str/replace path #"\\" "/"))

(defn- m3u-path->s3-path [m3u-path]
  (let [path (back-slash->forward-slash m3u-path)
        fields (str/split path #"/")
        fields (drop 4 fields)]
    (str/join "/" fields)))

(defn parse-m3u [text]
  (let [lines (str/split-lines text)
        lines (filter #(not (str/starts-with? % "#")) lines)]
    (map m3u-path->s3-path lines)))

(comment

  (slurp "test.m3u")
  (parse-m3u (slurp "test.m3u"))

  :rcf)
