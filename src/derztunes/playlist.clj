(ns derztunes.playlist
  (:require [derztunes.m3u :as m3u]
            [derztunes.db :as db]
            [derztunes.util :as util]
            [java-time.api :as jt]))

(defn make [name]
  (let [now (jt/instant)]
    {:playlist/id (random-uuid)
     :playlist/name name
     :playlist/created-at now
     :playlist/updated-at now}))

(defn import! [db-conn m3u-path]
  (let [name (util/base-name m3u-path)
        [name _] (util/split-ext name)
        text (slurp m3u-path)]
    (db/create-playlist! db-conn (make name))
    (let [playlist (db/read-playlist-by-name! db-conn name)
          enumerated-paths (m3u/parse-m3u text)]
      (doseq [enumerated-path enumerated-paths]
        (let [number (:playlist-track/number enumerated-path)
              path (:playlist-track/path enumerated-path)]
          (println "Importing:" path)
          (let [track (db/read-track-by-path! db-conn path)]
            (if track
              (db/create-playlist-track! db-conn playlist track number)
              (println "Track not found:" path))))))))

(comment

  (def db-uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db-conn (db/connect! db-uri))

  (import! db-conn "test.m3u")

  :rcf)
