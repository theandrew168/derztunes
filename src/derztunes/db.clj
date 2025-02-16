(ns derztunes.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.date-time :as jdbc.date-time]
            [next.jdbc.result-set :as rs]))

(defn- jdbc-client! [db-uri]
  (jdbc.date-time/read-as-instant)
  (-> (jdbc/get-datasource db-uri)
      (jdbc/with-options {:builder-fn rs/as-kebab-maps})))

(defn connect! [db-uri]
  (let [client (jdbc-client! db-uri)]
    {:db/client client}))

(defn create-track! [conn track]
  (println "Syncing track:" (:track/path track))
  (jdbc/execute!
   (:db/client conn)
   ["INSERT INTO track
       (id, path, size, created_at, updated_at)
     VALUES
       (?, ?, ?, ?, ?)
     ON CONFLICT (path) DO UPDATE SET
       size = EXCLUDED.size"
    (:track/id track)
    (:track/path track)
    (:track/size track)
    (:track/created-at track)
    (:track/updated-at track)]))

(defn list-tracks! [conn]
  (jdbc/execute!
   (:db/client conn)
   ["SELECT *
     FROM track
     ORDER BY
       artist ASC,
       album ASC,
       track_number ASC,
       title ASC,
       id ASC"]))

(defn search-tracks! [conn q]
  (jdbc/execute!
   (:db/client conn)
   ["SELECT *
     FROM track
     WHERE (title ILIKE ? OR artist ILIKE ? OR album ILIKE ?)
     ORDER BY
       artist ASC,
       album ASC,
       track_number ASC,
       title ASC,
       id ASC"
    (str "%" q "%")
    (str "%" q "%")
    (str "%" q "%")]))

(defn read-track! [conn id]
  (jdbc/execute-one!
   (:db/client conn)
   ["SELECT *
     FROM track
     WHERE id = UUID(?)"
    id]))

(defn read-track-by-path! [conn path]
  (jdbc/execute-one!
   (:db/client conn)
   ["SELECT *
     FROM track
     WHERE path = ?"
    path]))

(defn update-track! [conn track]
  (jdbc/execute!
   (:db/client conn)
   ["UPDATE track
     SET
       size = ?,
       track_number = ?,
       duration = ?,
       title = ?,
       artist = ?,
       album = ?,
       signed_url = ?,
       signed_url_expires_at = ?,
       play_count = ?,
       updated_at = ?
     WHERE id = ?"
    (:track/size track)
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

(defn create-playlist! [conn playlist]
  (jdbc/execute!
   (:db/client conn)
   ["INSERT INTO playlist
       (id, name, created_at, updated_at)
     VALUES
       (?, ?, ?, ?)
     ON CONFLICT DO NOTHING"
    (:playlist/id playlist)
    (:playlist/name playlist)
    (:playlist/created-at playlist)
    (:playlist/updated-at playlist)]))

(defn list-playlists! [conn]
  (jdbc/execute!
   (:db/client conn)
   ["SELECT *
     FROM playlist
     ORDER BY name ASC"]))

(defn read-playlist! [conn id]
  (jdbc/execute-one!
   (:db/client conn)
   ["SELECT *
     FROM playlist
     WHERE id = UUID(?)"
    id]))

(defn read-playlist-by-name! [conn name]
  (jdbc/execute-one!
   (:db/client conn)
   ["SELECT *
     FROM playlist
     WHERE name = ?"
    name]))

(defn create-playlist-track! [conn playlist track number]
  (jdbc/execute!
   (:db/client conn)
   ["INSERT INTO playlist_track
       (playlist_id, track_id, playlist_track_number)
     VALUES
       (?, ?, ?)
     ON CONFLICT DO NOTHING",
    (:playlist/id playlist)
    (:track/id track)
    number]))

(defn list-playlist-tracks! [conn playlist]
  (jdbc/execute!
   (:db/client conn)
   ["SELECT track.*
     FROM playlist
     INNER JOIN playlist_track
       ON playlist_track.playlist_id = playlist.id
     INNER JOIN track
       ON track.id = playlist_track.track_id
     WHERE playlist.id = UUID(?)
     ORDER BY playlist_track.playlist_track_number ASC"
    (:playlist/id playlist)]))

(defn search-playlist-tracks! [conn playlist q]
  (jdbc/execute!
   (:db/client conn)
   ["SELECT track.*
     FROM playlist
     INNER JOIN playlist_track
       ON playlist_track.playlist_id = playlist.id
     INNER JOIN track
       ON track.id = playlist_track.track_id
     WHERE playlist.id = UUID(?)
       AND (track.title ILIKE ? OR track.artist ILIKE ? OR track.album ILIKE ?)
     ORDER BY playlist_track.playlist_track_number ASC"
    (:playlist/id playlist)
    (str "%" q "%")
    (str "%" q "%")
    (str "%" q "%")]))

(comment

  (def uri "postgresql://postgres:postgres@localhost:5432/postgres")
  (def conn (connect! uri))

  :rcf)
