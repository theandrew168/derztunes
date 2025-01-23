(ns derztunes.sync
  (:require [clojure.string :as str]
            [derztunes.db :as db]
            [derztunes.model :as model]
            [derztunes.s3 :as s3]))

;; Given a file path in S3, return the name of the file without its extension.
;; TODO: Check for and remove any numeric prefixes (like "01" or "1-01").
(defn- path->name [path]
  (let [name (last (str/split path #"/"))]
    (subs name 0 (- (count name) 4))))

(defn- object->track [object]
  (let [path (:name object)
        name (path->name path)]
    (model/make-track name path)))

(defn- mp3-file? [name]
  (str/ends-with? name ".mp3"))

(defn- m4a-file? [name]
  (str/ends-with? name ".m4a"))

(defn- audio-file? [object]
  (let [name (:name object)]
    (or (mp3-file? name) (m4a-file? name))))

(defn tracks! [db-conn s3-conn]
  (let [objects (s3/list-objects! s3-conn "derztunes")
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

  (path->name "derztunes/test.mp3")
  (object->track {:name "derztunes/test.mp3"})

  :rcf)
