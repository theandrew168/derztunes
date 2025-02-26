(ns derztunes.import
  (:require [derztunes.m3u :as m3u]
            [derztunes.db :as db]
            [derztunes.model :as model]
            [derztunes.util :as util]))

;; PRESENT:
;; Action: Read playlist from file
;; Action: Create playlist in the DB (idempotent)
;; Calculation: Parse playlist into playlist tracks
;; for each track in playlist:
;;  Action: Read track from the DB (by path)
;;  Action: Create playlist track in the DB (idempotent)

;; FUTURE:
;; Action: Read playlist from file
;; Action: Create playlist in the DB (idempotent)
;; Action: List tracks in the DB
;; Calculation: Parse playlist into playlist tracks
;; Calculation: Compare playlist tracks to find only new/updated
;; Action: Create/update SPECIFIC playlist tracks in the DB (fast, batch)

(defn- playlist-track! [db-conn playlist playlist-track]
  (let [number (:playlist-track/number playlist-track)
        path (:playlist-track/path playlist-track)]
    (println "Importing:" path)
    (let [track (db/read-track-by-path! db-conn path)]
      (if track
        (db/create-playlist-track! db-conn playlist track number)
        (println "Track not found:" path)))))

(defn playlist! [db-conn m3u-path]
  (let [name (util/base-name m3u-path)
        [name _] (util/split-ext name)
        text (slurp m3u-path)]
    (db/create-playlist! db-conn (model/make-playlist name))
    (let [playlist (db/read-playlist-by-name! db-conn name)
          playlist-tracks (m3u/parse-m3u text)]
      (doall (pmap #(playlist-track! db-conn playlist %) playlist-tracks)))))

(comment

  (def db-uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db-conn (db/connect! db-uri))

  (playlist! db-conn "test.m3u")

  :rcf)
