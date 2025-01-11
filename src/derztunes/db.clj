(ns derztunes.db
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]))

(defn connect! [db-uri]
  (jdbc/get-datasource db-uri))

(defrecord DB [conn uri]
  component/Lifecycle

  (start [this]
    (let [conn (jdbc/get-datasource uri)]
      (assoc this :conn conn)))

  (stop [this]
    (assoc this :conn nil)))

(defn list-tracks! [db]
  (jdbc/execute! db ["SELECT * FROM track"]))

(comment
  (def uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db (connect! uri))

  (def tracks (list-tracks! db))
  (def track (first tracks))
  (get track :track/id)

  :rcf)
