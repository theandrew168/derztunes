(ns derztunes.m3u
  (:require [clojure.string :as str]
            [derztunes.util :as util]))

(defn- m3u-path->s3-path [m3u-path]
  (let [path (util/back-slash->forward-slash m3u-path)
        fields (str/split path #"/")
        fields (drop 4 fields)]
    (str/join "/" fields)))

(defn- enumerate-paths [m3u-paths]
  (map-indexed (fn [i track]
                 {:number (+ 1 i)
                  :path (m3u-path->s3-path track)})
               m3u-paths))

(defn parse-m3u [text]
  (let [lines (str/split-lines text)
        m3u-paths (filter #(not (str/starts-with? % "#")) lines)]
    (enumerate-paths m3u-paths)))

(comment

  (slurp "test.m3u")
  (parse-m3u (slurp "test.m3u"))

  :rcf)
