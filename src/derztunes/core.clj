(ns derztunes.core
  (:require [com.stuartsierra.component :as component]
            [derztunes.config :as config]
            [derztunes.db :as db]
            [derztunes.s3 :as s3]
            [derztunes.web :as web])
  (:gen-class))

;; TODO: Design the data model: track, playlist. Artist and albums eventually?
;; TODO: Write a process to index the S3 bucket's music into the database.
;; TODO: Bake the bucket (from s3-uri) into the s3 client.
;; TODO: Optimize PG connection handling (hikari vs c3p0)
;; TODO: Write a process to import playlists (.m3u XML files).

;; System deps:
;; Web -> S3 (for fetching and streaming audio data)
;; Web -> DB (for listing tracks and playlists)
(defn system [config]
  (let [db-uri (config/db-uri config)
        s3-uri (config/s3-uri config)]
    (component/system-map
     :db (db/map->DB {:uri db-uri})
     :s3 (s3/map->S3 {:uri s3-uri})
     :web (component/using (web/map->Web {}) [:db :s3]))))

;; TODO: Add -conf flag for specifying different config files.
;; TODO: Add -migrate flag for applying migrations and exiting.
(defn -main []
  (println "Listening on port 5000...")
  (let [conf (config/from-file! "derztunes.edn")
        sys (system conf)]
    (component/start sys)))

(comment

  (def conf (config/from-file! "derztunes.edn"))

  (def sys (system conf))
  (alter-var-root #'sys component/start)
  (alter-var-root #'sys component/stop)

  :rcf)
