(ns derztunes.db
  (:require [derztunes.model :as model]
            [next.jdbc :as jdbc]
            [next.jdbc.date-time :as jdbc.date-time]
            [next.jdbc.result-set :as rs]))

(defn- jdbc-client! [db-uri]
  (jdbc.date-time/read-as-instant)
  (-> (jdbc/get-datasource db-uri)
      (jdbc/with-options {:builder-fn rs/as-kebab-maps})))

(defn connect! [db-uri]
  (let [client (jdbc-client! db-uri)]
    {:db/client client}))

(defn list-tracks! [conn]
  (jdbc/execute! (:db/client conn) ["SELECT * FROM track ORDER BY path ASC"]))

(defn search-tracks! [conn q]
  (jdbc/execute! (:db/client conn) ["SELECT * FROM track WHERE path ILIKE ? ORDER BY path ASC" (str "%" q "%")]))

(defn read-track! [conn id]
  (jdbc/execute-one! (:db/client conn) ["SELECT * FROM track WHERE id = ?" id]))

(defn create-track! [conn track]
  (println "Syncing track:" (:track/path track))
  (jdbc/execute!
   (:db/client conn)
   ["INSERT INTO track
      (id, path, created_at, updated_at)
    VALUES
      (?, ?, ?, ?)
    ON CONFLICT DO NOTHING",
    (:track/id track)
    (:track/path track)
    (:track/created-at track)
    (:track/updated-at track)]))

(defn update-track! [conn track]
  (jdbc/execute!
   (:db/client conn)
   ["UPDATE track
     SET
       track_number = ?,
       duration = ?,
       title = ?,
       artist = ?,
       album = ?,
       signed_url = ?,
       signed_url_expires_at = ?,
       play_count = ?,
       updated_at = ?
     WHERE id = ?",
    (:track/track-number track)
    (:track/duration track)
    (:track/title track)
    (:track/artist track)
    (:track/album track)
    (:track/signed-url track)
    (:track/signed-url-expires-at track)
    (:track/play-count track)
    (:track/updated-at track)
    (:track/id track)]))

(comment

  (def uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def conn (connect! uri))

  (create-track! conn (model/make-track "/path/to/foo"))

  (def tracks (list-tracks! conn))

  :rcf)
