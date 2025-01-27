(ns derztunes.core
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [derztunes.cli :as cli]
   [derztunes.db :as db]
   [derztunes.s3 :as s3]
   [derztunes.server :as server]
   [derztunes.sync :as sync])
  (:gen-class))

;; TODO: Drop the track name nullable (will be nullable as title).
;; TODO: Add track cols for duration, track, title, artist, album, and genre.
;; TODO: Write a second sync process for track metadata (duration, artist, album, etc).
;; TODO: Add extra metadata to the FE track table.
;; TODO: Use HTMX for searching.
;; TODO: Use HTMX for infinite scrolling.
;; TODO: Add data model support for playlists.
;; TODO: Write a process to import playlists (.m3u XML files).
;; TODO: Support systemd notifications for successful startups.
;; TODO: Optimize PG connection handling (hikari vs c3p0)

;; System deps:
;; Web -> S3 (for fetching and streaming audio data)
;; Web -> DB (for listing tracks and playlists)

(defn read-config! [path]
  (-> (slurp path)
      (edn/read-string)))

(defn read-port! []
  (let [port (System/getenv "PORT")
        port (if (str/blank? port) "5000" port)]
    (Integer/parseInt port)))

(defn -main [& args]
  (let [flags (cli/parse-flags args)
        conf-path (or (get flags "-conf") "derztunes.edn")
        conf (read-config! conf-path)
        port (read-port!)
        db-conn (db/connect! (:db-uri conf))
        s3-conn (s3/connect! (:s3-uri conf))]
    (println (format "Reading config from: %s" conf-path))
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
            server (server/start! app port)]
        (println (format "Starting web server on port %s...", port))
        (.addShutdownHook (Runtime/getRuntime) (Thread. #(server/stop! server)))))))

(comment

  (def conf (read-config! "derztunes.edn"))
  (def port (read-port!))
  (def db-conn (db/connect! (:db-uri conf)))
  (def s3-conn (s3/connect! (:s3-uri conf)))
  (def app (server/app db-conn s3-conn))
  (def server (server/start! app port))
  (server/stop! server)

  :rcf)
