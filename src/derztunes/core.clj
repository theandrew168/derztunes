(ns derztunes.core
  (:require [derztunes.cli :as cli]
            [derztunes.config :as config]
            [derztunes.db :as db]
            [derztunes.migrate :as migrate]
            [derztunes.playlist :as playlist]
            [derztunes.s3 :as s3]
            [derztunes.server :as server]
            [derztunes.sync :as sync])
  (:gen-class))

;; TODO: Use HTMX for searching.
;; TODO: Use HTMX for infinite scrolling.
;; TODO: Fix duration parsing for some MP3 files.
;; TODO: Write my own metadata parsers for funsies?
;; TODO: Find better tracks for the demo site.
;; TODO: Support systemd notifications for successful startups.
;; TODO: Optimize PG connection handling (hikari vs c3p0)
;; TODO: Consider using Rum instead of Hiccup for HTML generation.
;; TODO: Consider using Reitit instead of Compojure for routing.
;; TODO: Add basic prometheus metrics.

;; System deps:
;; Web -> S3 (for fetching and streaming audio data)
;; Web -> DB (for listing tracks and playlists)

(defn -main [& args]
  (let [env (System/getenv)
        flags (cli/parse-flags args)
        conf-path (or (get flags "-conf") "derztunes.edn")
        conf (config/read-file! conf-path)
        port (config/read-port env)
        db-conn (db/connect! (:db-uri conf))
        s3-conn (s3/connect! (:s3-uri conf))]
    (println (format "Reading config: %s" conf-path))
    (cond
      (contains? flags "-migrate")
      (do
        (println "Applying migrations...")
        (migrate/migrate! db-conn))
      (contains? flags "-sync")
      (do
        (println "Syncing tracks...")
        (sync/tracks! db-conn s3-conn)
        (println "Syncing metadata...")
        (sync/metadata! db-conn s3-conn)
        (println "Done syncing.")
        (shutdown-agents))
      (contains? flags "-tracks")
      (do
        (println "Syncing tracks...")
        (sync/tracks! db-conn s3-conn)
        (println "Done syncing.")
        (shutdown-agents))
      (contains? flags "-metadata")
      (do
        (println "Syncing metadata...")
        (sync/metadata! db-conn s3-conn)
        (println "Done syncing.")
        (shutdown-agents))
      (contains? flags "-import")
      (do
        (println "Importing playlist...")
        (playlist/import! db-conn (get flags "-import")))
      :else
      (let [app (server/app db-conn s3-conn)
            server (server/start! app port)]
        (println (format "Starting web server on port %s...", port))
        (.addShutdownHook (Runtime/getRuntime) (Thread. #(server/stop! server)))))))

(comment

  (def env (System/getenv))
  (def conf (config/read-file! "derztunes.edn"))
  (def port (config/read-port env))
  (def db-conn (db/connect! (:db-uri conf)))
  (def s3-conn (s3/connect! (:s3-uri conf)))
  (def app (server/app db-conn s3-conn))
  (def server (server/start! app port))
  (server/stop! server)

  :rcf)
