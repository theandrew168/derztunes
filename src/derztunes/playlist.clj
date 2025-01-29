(ns derztunes.playlist
  (:require [derztunes.m3u :as m3u]
            [derztunes.db :as db]
            [derztunes.model :as model]))

;; TODO: Accept a path here, parse the name, and read the file.
(defn import! [db-conn name m3u-text]
  (db/create-playlist! db-conn (model/make-playlist name))
  (let [playlist (db/read-playlist-by-name! db-conn name)
        paths (m3u/parse-m3u m3u-text)]
    (doseq [path paths]
      (println "Importing:" path)
      (let [track (db/read-track-by-path! db-conn path)]
        (db/create-playlist-track! db-conn playlist track)))))

(comment

  (def db-uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db-conn (db/connect! db-uri))

  (import! db-conn "Dan Music" (slurp "test.m3u"))

  :rcf)
