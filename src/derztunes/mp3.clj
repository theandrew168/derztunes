(ns derztunes.mp3
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [com.mpatric.mp3agic Mp3File]))

;; References:
;; https://github.com/mpatric/mp3agic
;; https://github.com/mpatric/mp3agic-examples/blob/master/src/main/java/com/mpatric/mp3agic/app/Mp3Details.java

(defn- track [tag]
  (when-let [track (.getTrack tag)]
    (let [[a _] (str/split track "/")]
      (when a
        (Integer/parseInt a)))))

(defn- title [tag]
  (.getTitle tag))

(defn- artist [tag]
  (.getArtist tag))

(defn- album [tag]
  (.getAlbum tag))

(defn- genre [tag]
  (.getGenreDescription tag))

(defn parse-metadata
  [file]
  (let [mp3 (Mp3File. file)
        tag (or (.getId3v2Tag mp3) (.getId3v1Tag mp3))]
    (if tag
      {:track (track tag)
       :title (title tag)
       :artist (artist tag)
       :album (album tag)
       :genre (genre tag)}
      {})))

(comment

  (def res (io/resource "testdata/test.mp3"))

  (def mp3 (Mp3File. (io/file res)))
  (.getVersion mp3)

  (def tag (.getId3v1Tag mp3))
  (def tag (.getId3v2Tag mp3))
  (.getTrack tag)
  (.getArtist tag)
  (.getAlbum tag)
  (.getTitle tag)
  (.getGenreDescription tag)

  (parse-metadata (io/file res))

  :rcf)
