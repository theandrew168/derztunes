(ns derztunes.db
  (:require [next.jdbc :as jdbc]))

(defn connect! [db-uri]
  (jdbc/get-datasource db-uri))

(defn list-tracks! [db]
  (jdbc/execute! db ["SELECT * FROM track"]))

(defn create-track! [db track]
  (jdbc/execute! db ["INSERT INTO track (name, path) VALUES (?, ?) ON CONFLICT (path) DO UPDATE SET name = EXCLUDED.name",
                     (:name track) (:path track)]))

(defn update-track! [db track]
  (jdbc/execute! db ["UPDATE track SET signed_url = ? WHERE path = ?",
                     (:track/signed-url track) (:track/path track)]))

(comment
  (def uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db (connect! uri))

  (def tracks (list-tracks! db))
  (def track (first tracks))
  (:track/id track)

  :rcf)
