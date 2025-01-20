(ns derztunes.db
  (:require [derztunes.model :as model]
            [next.jdbc :as jdbc]
            [next.jdbc.date-time :as jdbc.date-time]
            [next.jdbc.result-set :as rs]))

(defn connect! [db-uri]
  (jdbc.date-time/read-as-instant)
  (let [ds (jdbc/get-datasource db-uri)]
    (jdbc/with-options ds {:builder-fn rs/as-kebab-maps})))

(defn list-tracks! [db]
  (jdbc/execute! db ["SELECT * FROM track ORDER BY path ASC"]))

(defn search-tracks! [db q]
  (jdbc/execute! db ["SELECT * FROM track WHERE path ILIKE ? ORDER BY path ASC" (str "%" q "%")]))

(defn read-track! [db id]
  (jdbc/execute-one! db ["SELECT * FROM track WHERE id = ?" id]))

(defn create-track! [db track]
  (println "Syncing track:" (:track/path track))
  (jdbc/execute! db ["INSERT INTO track
                        (id, name, path, created_at, updated_at)
                      VALUES
                        (?, ?, ?, ?, ?)
                      ON CONFLICT (path) DO UPDATE SET name = EXCLUDED.name",
                     (:track/id track)
                     (:track/name track)
                     (:track/path track)
                     (:track/created-at track)
                     (:track/updated-at track)]))

(defn update-track! [db track]
  (jdbc/execute! db ["UPDATE track
                      SET
                        name = ?,
                        path = ?,
                        signed_url = ?,
                        signed_url_expires_at = ?,
                        updated_at = ?
                      WHERE id = ?",
                     (:track/name track)
                     (:track/path track)
                     (:track/signed-url track)
                     (:track/signed-url-expires-at track)
                     (:track/updated-at track)
                     (:track/id track)]))

(comment

  (def uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db (connect! uri))

  (create-track! db (model/make-track "foo" "/path/to/foo"))

  (def tracks (list-tracks! db))
  (println "Hello" "world")

  :rcf)
