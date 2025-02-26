(ns derztunes.sync
  (:require [derztunes.db :as db]
            [derztunes.s3 :as s3]
            [derztunes.m4a :as m4a]
            [derztunes.model :as model]
            [derztunes.mp3 :as mp3]
            [derztunes.util :as util])
  (:import [java.io File FileOutputStream]))

(defn- object->track [object]
  (let [path (:object/name object)
        size (:object/size object)]
    (model/make-track path size)))

;; PRESENT:
;; Action: List objects (tracks) in S3
;; Calculation: Filter objects down to audio files
;; Calculation: Convert objects to tracks
;; Action: Create/update ALL tracks in the DB (slow)

;; FUTURE:
;; Action: List objects (tracks) in S3
;; Action: List tracks in the DB
;; Calculation: Filter objects down to audio files
;; Calculation: Convert objects to tracks
;; Calculation: Compare tracks to find only new/updated
;; Action: Create/update SPECIFIC tracks in the DB (fast, batch)

(defn tracks! [db-conn s3-conn]
  (let [objects (s3/list-objects! s3-conn)
        objects (filter #(util/audio-file? (:object/name %)) objects)
        tracks (map object->track objects)]
    (doall (pmap #(db/create-track! db-conn %) tracks))))

(defn- object->file [object]
  (let [file (File/createTempFile "derztunes" nil)
        stream (FileOutputStream. file)]
    (.transferTo object stream)
    file))

(defn- track-metadata [path file]
  (cond
    (util/mp3-file? path) (mp3/parse-metadata file)
    (util/m4a-file? path) (m4a/parse-metadata file)
    :else {}))

;; PRESENT:
;; Action: List tracks in the DB
;; Action: Fetch object (track) from S3
;; Action: Write object to temp file
;; Calculation: Parse metadata from the audio file
;; Action: Update ALL track metadata in the DB (slow)

;; FUTURE:
;; Action: List tracks in the DB
;; Action: Fetch object (track) from S3
;; Calculation: Parse metadata from the audio data (InputStream, not file)
;; Calculation: Compare parsed metadata to existing tracks
;; Action: Update SPECIFIC track metadata in the DB (fast, batch)

(defn- track-metadata! [db-conn s3-conn track]
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
    (doall (pmap #(track-metadata! db-conn s3-conn %) tracks))))

(comment

  (def db-uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db-conn (db/connect! db-uri))

  (def s3-uri "s3://minioadmin:minioadmin@localhost:9000/derztunes")
  (def s3-conn (s3/connect! s3-uri))

  (tracks! db-conn s3-conn)
  (metadata! db-conn s3-conn)

  :rcf)
