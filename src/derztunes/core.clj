(ns derztunes.core
  (:refer-clojure :exclude [import])
  (:require [derztunes.cli :as cli]
            [derztunes.config :as config]
            [derztunes.db :as db]
            [derztunes.migrate :as migrate]
            [derztunes.import :as import]
            [derztunes.s3 :as s3]
            [derztunes.server :as server]
            [derztunes.sync :as sync])
  (:gen-class))

;; TODO: Write my own metadata parsers for MP3 files.
;; TODO: Write my own metadata parsers for M4A files.
;; TODO: Add data for the bottom metadata section.
;; TODO: Consider rewriting the tracks table as an HTML table.

;; TODO: Make the track table sortable by column.
;; TODO: Make the track table columns resizable.
;; TODO: Use HTMX for searching.
;; TODO: Find better tracks for the demo site.
;; TODO: Support systemd notifications for successful startups.
;; TODO: Support systemd socket activation (need to PR a fix for http-kit?).
;; TODO: Optimize PG connection handling (hikari vs c3p0)
;; TODO: Consider using Reitit instead of Compojure for routing.
;; TODO: Add basic prometheus metrics.
;; TODO: Add admin page(s) for managing track metadata and playlists.
;; TODO: Add tests for api.clj.
;; TODO: Add tests for db.clj.
;; TODO: Add tests for import.clj.
;; TODO: Add tests for model.clj.
;; TODO: Add tests for s3.clj.
;; TODO: Add tests for server.clj.
;; TODO: Add tests for sync.clj.
;; TODO: Add tests for web.clj.

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
        (import/playlist! db-conn (get flags "-import")))
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
