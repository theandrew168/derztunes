(ns derztunes.m4a
  (:require [clojure.java.io :as io])
  (:import [org.mp4parser IsoFile]
           [org.mp4parser.tools Path]))

;; References:
;; https://github.com/sannies/mp4parser
;; https://github.com/sannies/mp4parser/blob/master/examples/src/main/java/org/mp4parser/examples/metadata/MetaDataRead.java

(defn- ignore-errors [f]
  (try
    (f)
    (catch Exception _
      nil)))

(defn- track-number [m4a]
  (ignore-errors
   (fn []
     (let [box (Path/getPath m4a "moov/udta/meta/ilst/trkn")]
       (.getA box)))))

(defn- title [m4a]
  (ignore-errors
   (fn []
     (let [box (Path/getPath m4a "moov/udta/meta/ilst/©nam")]
       (.getValue box)))))

(defn- artist [m4a]
  (ignore-errors
   (fn []
     (let [box (Path/getPath m4a "moov/udta/meta/ilst/©ART")]
       (.getValue box)))))

(defn- album [m4a]
  (ignore-errors
   (fn []
     (let [box (Path/getPath m4a "moov/udta/meta/ilst/©alb")]
       (.getValue box)))))

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
  (.getValue (Path/getPath m4a "moov/udta/meta/ilst/©nam"))
  (.getValue (Path/getPath m4a "moov/udta/meta/ilst/©ART"))
  (.getValue (Path/getPath m4a "moov/udta/meta/ilst/©alb"))

  (.getDuration (Path/getPath m4a "moov/mvhd"))

  (parse-metadata (io/file res))

  :rcf)
