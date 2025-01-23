(ns derztunes.core
  (:require
   [clojure.edn :as edn]
   [derztunes.cli :as cli]
   [derztunes.db :as db]
   [derztunes.s3 :as s3]
   [derztunes.server :as server]
   [derztunes.sync :as sync])
  (:gen-class))

;; TODO: Use HTMX for searching.
;; TODO: Use HTMX for infinite scrolling.
;; TODO: Write a second sync process for track metadata (duration, artist, album, etc).
;; TODO: Add models for artist and albums.
;; TODO: Add track number to track model.
;; TODO: Bake the bucket (from s3-uri) into the s3 client.
;; TODO: Optimize PG connection handling (hikari vs c3p0)
;; TODO: Add data model support for playlists.
;; TODO: Write a process to import playlists (.m3u XML files).
;; TODO: Support systemd notifications for successful startups.

;; System deps:
;; Web -> S3 (for fetching and streaming audio data)
;; Web -> DB (for listing tracks and playlists)

(defn read-config! [path]
  (-> (slurp path)
      (edn/read-string)))

(defn -main [& args]
  (let [flags (cli/parse-flags args)
        conf (or (get flags "-conf") "derztunes.edn")
        conf (read-config! conf)
        db-conn (db/connect! (:db-uri conf))
        s3-conn (s3/connect! (:s3-uri conf))]
    (cond
      (contains? flags "-migrate") (println "TODO: Migrate and exit")
      (contains? flags "-sync")
      (do
        ;; TODO: Figure out why this doesn't terminate. Something about closing streams via the Minio SDK?
        (println "Syncing tracks...")
        (sync/tracks! db-conn s3-conn)
        (println "Syncing metadata...")
        (sync/metadata! db-conn s3-conn)
        (println "Done syncing."))
      :else
      (let [app (server/app db-conn s3-conn)
            server (server/start! app)]
        (println "Starting web server...")
        (.addShutdownHook (Runtime/getRuntime) (Thread. #(server/stop! server)))))))

(comment

  (def conf (read-config! "derztunes.edn"))
  (def db-conn (db/connect! (:db-uri conf)))
  (def s3-conn (s3/connect! (:s3-uri conf)))
  (def app (server/app db-conn s3-conn))
  (def server (server/start! app))
  (server/stop! server)

  :rcf)
