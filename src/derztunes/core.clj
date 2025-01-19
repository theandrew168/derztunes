(ns derztunes.core
  (:require [derztunes.config :as config]
            [derztunes.db :as db]
            [derztunes.s3 :as s3]
            [derztunes.web :as web]
            [integrant.core :as ig])
  (:gen-class))

;; TODO: Write a simple endpoint for fetching a track's signed URL (api.clj).
;; TODO: Write a second sync process for track metadata (duration, artist, album, etc).
;; TODO: Add models for artist and albums.
;; TODO: Add track number to track model.
;; TODO: Bake the bucket (from s3-uri) into the s3 client.
;; TODO: Optimize PG connection handling (hikari vs c3p0)
;; TODO: Add data model support for playlists.
;; TODO: Write a process to import playlists (.m3u XML files).

;; System deps:
;; Web -> S3 (for fetching and streaming audio data)
;; Web -> DB (for listing tracks and playlists)
(defn system [conf]
  {::db {:uri (config/db-uri conf)}
   ::s3 {:uri (config/s3-uri conf)}
   ::web {:db (ig/ref ::db) :s3 (ig/ref ::s3)}})

(defmethod ig/init-key ::db [_ {:keys [uri]}]
  (println "Connecting to DB...")
  (db/connect! uri))

(defmethod ig/init-key ::s3 [_ {:keys [uri]}]
  (println "Connecting to S3...")
  (s3/connect! uri))

(defmethod ig/init-key ::web [_ {:keys [db s3]}]
  (println "Starting web server...")
  (web/run-server! (web/routes db s3)))

(defmethod ig/halt-key! ::web [_ server]
  (println "Stopping web server...")
  (web/stop-server! server))

;; TODO: Add -conf flag for specifying different config files.
;; TODO: Add -sync flag for syncing tracks from S3.
;; TODO: Add -migrate flag for applying migrations and exiting.
(defn -main []
  (let [conf (config/from-file! "derztunes.edn")
        sys (ig/init (system conf))]
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(ig/halt! sys)))))

(comment

  (def conf (config/from-file! "derztunes.edn"))
  (def sys (ig/init (system conf)))
  (ig/halt! sys)

  :rcf)
