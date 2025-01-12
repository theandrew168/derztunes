(ns derztunes.db
  (:require [next.jdbc :as jdbc]))

(defn connect! [db-uri]
  (jdbc/get-datasource db-uri))

(defn list-tracks! [db]
  (jdbc/execute! db ["SELECT * FROM track"]))

(comment
  (def uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db (connect! uri))

  (def tracks (list-tracks! db))
  (def track (first tracks))
  (:track/id track)

  :rcf)
