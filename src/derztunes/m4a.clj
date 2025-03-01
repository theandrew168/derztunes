(ns derztunes.m4a
  (:require [clojure.java.io :as io]
            [derztunes.util :as util])
  (:import [org.mp4parser IsoFile]
           [org.mp4parser.tools Path]))

;; References:
;; https://github.com/sannies/mp4parser
;; https://github.com/sannies/mp4parser/blob/master/isoparser/src/main/java/org/mp4parser/IsoFile.java
;; https://github.com/sannies/mp4parser/blob/master/examples/src/main/java/org/mp4parser/examples/metadata/MetaDataRead.java


(defn- track-number [m4a]
  (util/ignore-errors
   (let [box (Path/getPath m4a "moov/udta/meta/ilst/trkn")]
     (.getA box))))

(defn- title [m4a]
  (util/ignore-errors
   (let [box (Path/getPath m4a "moov/udta/meta/ilst/©nam")]
     (.getValue box))))

(defn- artist [m4a]
  (util/ignore-errors
   (let [box (Path/getPath m4a "moov/udta/meta/ilst/©ART")]
     (.getValue box))))

(defn- album [m4a]
  (util/ignore-errors
   (let [box (Path/getPath m4a "moov/udta/meta/ilst/©alb")]
     (.getValue box))))

(defn parse-metadata
  [file]
  (let [m4a (IsoFile. file)]
    {:track/track-number (track-number m4a)
     :track/title (title m4a)
     :track/artist (artist m4a)
     :track/album (album m4a)}))

(comment

  (def res (io/resource "testdata/test.m4a"))

  (def m4a (IsoFile. (io/file res)))
  (album m4a)

  (parse-metadata (io/file res))

  :rcf)
