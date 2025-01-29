(ns derztunes.playlist
  (:require [derztunes.m3u :as m3u]
            [derztunes.db :as db]
            [derztunes.model :as model]
            [derztunes.util :as util]))

(defn import! [db-conn m3u-path]
  (let [name (util/base-name m3u-path)
        [name _] (util/split-ext name)
        text (slurp m3u-path)]
    (db/create-playlist! db-conn (model/make-playlist name))
    (let [playlist (db/read-playlist-by-name! db-conn name)
          paths (m3u/parse-m3u text)]
      (doseq [path paths]
        (println "Importing:" path)
        (let [track (db/read-track-by-path! db-conn path)]
          (db/create-playlist-track! db-conn playlist track))))))

(comment

  (def db-uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db-conn (db/connect! db-uri))

  (import! db-conn "test.m3u")

  :rcf)
