(ns derztunes.core
  (:require [derztunes.config :as config]
            [derztunes.db :as db]
            [derztunes.s3 :as s3])
  (:gen-class))

;; TODO: Design the data model: track, playlist. Artist and albums eventually?
;; TODO: Write a process to index the S3 bucket's music into the database.
;; TODO: Write a process to import playlists (.m3u XML files).
;; TODO: Bake the bucket (from s3-uri) into the s3 client.
;; TODO: Optimize PG connection handling (hikari vs c3p0)

;; TODO: Add -conf flag for specifying different config files.
;; TODO: Add -migrate flag for applying migrations and exiting.
(defn -main []
  (println "Hello, World!"))

(comment

  (def conf (config/from-file! "derztunes.edn"))
  (config/db-uri conf)
  (config/s3-uri conf)

  (def s3-client (s3/connect! (config/s3-uri conf)))
  (s3/list-buckets! s3-client)
  (s3/list-objects! s3-client "derztunes")

  (def db-client (db/connect! (config/db-uri conf)))
  (db/list-tracks! db-client)

  :rcf)
