(ns derztunes.sync
  (:require [clojure.string :as str]
            [derztunes.db :as db]
            [derztunes.model :as model]
            [derztunes.s3 :as s3]))

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

(defn metadata! [_ _]
  (println "TODO: Sync track metadata"))

(comment

  (def db-uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db-conn (db/connect! db-uri))

  (def s3-uri "s3://minioadmin:minioadmin@localhost:9000/derztunes")
  (def s3-conn (s3/connect! s3-uri))

  (tracks! db-conn s3-conn)
  (metadata! db-conn s3-conn)

  :rcf)
