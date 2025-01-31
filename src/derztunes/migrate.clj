(ns derztunes.migrate
  (:require [clojure.set :as set]
            [derztunes.db :as db]
            [derztunes.util :as util]
            [next.jdbc :as jdbc]
            [resauce.core :as resauce]))

(defn- create-migration-table! [conn]
  (jdbc/execute-one!
   (:db/client conn)
   ["CREATE TABLE IF NOT EXISTS migration (
       id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       name TEXT NOT NULL UNIQUE
     )"]))

(defn- list-applied-migrations! [conn]
  (jdbc/execute!
   (:db/client conn)
   ["SELECT name FROM migration"]))

(defn- migration-urls! []
  (resauce/resource-dir "migration"))

(defn- url->migration [url]
  (let [path (.getPath url)
        name (util/base-name path)]
    {:migration/name name :migration/url url}))

(defn- desired-migrations [urls]
  (map url->migration urls))

(defn- pending-migrations [desired applied]
  (let [desired-names (set (map :migration/name desired))
        applied-names (set (map :migration/name applied))
        pending-names (set/difference desired-names applied-names)]
    (filter #(util/in? pending-names (:migration/name %)) desired)))

(defn- apply-migration! [conn migration]
  (let [name (:migration/name migration)
        url (:migration/url migration)]
    (println "Applying migration:" name)
    (jdbc/with-transaction [tx (:db/client conn)]
      (jdbc/execute-one! tx [(slurp url)])
      (jdbc/execute-one! tx ["INSERT INTO migration (name) VALUES (?)" name]))))

(defn migrate! [conn]
  (create-migration-table! conn)
  (let [urls (migration-urls!)
        desired (desired-migrations urls)
        applied (list-applied-migrations! conn)
        pending (pending-migrations desired applied)]
    (doseq [migration (sort-by :migration/name pending)]
      (apply-migration! conn migration))))

(comment

  (def conn (db/connect! "jdbc:postgresql://postgres:postgres@localhost:5432/postgres"))

  (create-migration-table! conn)
  (list-applied-migrations! conn)
  (desired-migrations (migration-urls!))

  (pending-migrations
   (desired-migrations (migration-urls!))
   (list-applied-migrations! conn))

  (migrate! conn)

  :rcf)
