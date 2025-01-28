(ns derztunes.sync
  (:require [clojure.string :as str]
            [derztunes.db :as db]
            [derztunes.model :as model]
            [derztunes.s3 :as s3]
            [derztunes.m4a :as m4a]
            [derztunes.mp3 :as mp3])
  (:import [java.io File FileOutputStream]))

(defn- object->track [object]
  (let [path (:name object)]
    (model/make-track path)))

(defn- mp3-file? [path]
  (str/ends-with? path ".mp3"))

(defn- m4a-file? [path]
  (str/ends-with? path ".m4a"))

(defn- audio-file? [object]
  (let [path (:name object)]
    (or (mp3-file? path) (m4a-file? path))))

(defn tracks! [db-conn s3-conn]
  (let [objects (s3/list-objects! s3-conn)
        objects (filter audio-file? objects)
        tracks (map object->track objects)]
    (doall (map #(db/create-track! db-conn %) tracks))))

(defn- object->file [object]
  (let [file (File/createTempFile "derztunes" nil)
        stream (FileOutputStream. file)]
    (.transferTo object stream)
    file))

(defn- track-metadata [path file]
  (cond
    (mp3-file? path) (mp3/parse-metadata file)
    (m4a-file? path) (m4a/parse-metadata file)
    :else {}))

(defn- sync-track-metadata! [db-conn s3-conn track]
  (println "Syncing metadata:" (:track/path track))
  (let [path (:track/path track)
        object (s3/get-object! s3-conn path)
        file (object->file object)
        metadata (track-metadata path file)
        track (merge track metadata)]
    (db/update-track! db-conn track)
    (.delete file)))

(defn metadata! [db-conn s3-conn]
  (let [tracks (db/list-tracks! db-conn)]
    (doall (map #(sync-track-metadata! db-conn s3-conn %) tracks))))

(comment

  (def db-uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db-conn (db/connect! db-uri))

  (def s3-uri "s3://minioadmin:minioadmin@localhost:9000/derztunes")
  (def s3-conn (s3/connect! s3-uri))

  (tracks! db-conn s3-conn)
  (metadata! db-conn s3-conn)

  :rcf)
