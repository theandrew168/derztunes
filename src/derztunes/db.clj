(ns derztunes.db
  (:require [derztunes.track :as track]
            [next.jdbc :as jdbc]
            [next.jdbc.date-time :as jdbc.date-time]
            [next.jdbc.result-set :as rs]))

(defn connect! [db-uri]
  (jdbc.date-time/read-as-instant)
  (let [ds (jdbc/get-datasource db-uri)]
    (jdbc/with-options ds {:builder-fn rs/as-kebab-maps})))

(defn list-tracks! [db]
  (let [rows (jdbc/execute! db ["SELECT * FROM track ORDER BY path ASC"])]
    (map track/from-map rows)))

(defn create-track! [db t]
  (jdbc/execute! db ["INSERT INTO track
                        (id, name, path, created_at, updated_at)
                      VALUES
                        (?, ?, ?, ?, ?)
                      ON CONFLICT (path) DO UPDATE SET name = EXCLUDED.name",
                     (track/id t) (track/name t) (track/path t) (track/created-at t) (track/updated-at t)]))

(defn update-track! [db t]
  (jdbc/execute! db ["UPDATE track
                      SET
                        name = ?,
                        path = ?,
                        signed_url = ?,
                        signed_url_expires_at = ?,
                        updated_at = ?
                      WHERE id = ?",
                     (track/name t) (track/path t) (track/signed-url t) (track/signed-url-expires-at t) (track/updated-at t) (track/id t)]))

(comment

  (def uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def db (connect! uri))

  (create-track! db (track/make "foo" "/path/to/foo"))

  (def tracks (list-tracks! db))
  (def t (first tracks))
  (track/signed-url t)

  :rcf)
