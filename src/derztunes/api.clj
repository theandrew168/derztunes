(ns derztunes.api
  (:require [clojure.data.json :as json]
            [derztunes.db :as db]
            [derztunes.s3 :as s3]
            [java-time.api :as jt]
            [ring.util.response :as response]))

;; Check if a given instant is within 30 minutes of the current time.
(defn- expires-soon? [expires-at]
  (let [minutes-until-expiration (jt/time-between (jt/instant) expires-at :minutes)]
    (< minutes-until-expiration 30)))

;; Given a track, check if its signed URL is valid and won't expire soon.
;; If it is, return it, otherwise return nil.
(defn- valid-signed-url? [track]
  (let [expires-at (:track/signed-url-expires-at track)]
    (if (or (nil? expires-at) (expires-soon? expires-at))
      nil
      (:track/signed-url track))))

;; Update a track with a new signed URL and its expiration time.
(defn- with-signed-url [track signed-url signed-url-expires-at]
  (-> track
      (assoc :track/signed-url signed-url)
      (assoc :track/signed-url-expires-at signed-url-expires-at)
      (assoc :track/updated-at (jt/instant))))

(defn- hours->seconds [hours]
  (* hours 60 60))

(defn- seconds-from-now [seconds]
  (jt/plus (jt/instant) (jt/seconds seconds)))

;; Take a track and ensure it has a valid, non-expired signed URL.
;; If the a new URL was generated, update the track.
(defn- activate-track! [s3-conn track]
  (if (valid-signed-url? track)
    track
    (let [expiry (hours->seconds 24)
          signed-url (s3/get-signed-url s3-conn (:track/path track) expiry)
          signed-url-expires-at (seconds-from-now expiry)]
      (with-signed-url track signed-url signed-url-expires-at))))

(defn- with-incremented-play-count [track]
  (-> track
      (update :track/play-count inc)
      (assoc :track/updated-at (jt/instant))))

(defn sign-track-handler [db-conn s3-conn]
  (fn [req]
    (let [id (-> req :params :id)
          id (parse-uuid id)
          track (db/read-track! db-conn id)
          track (activate-track! s3-conn track)
          track (with-incremented-play-count track)]
      (db/update-track! db-conn track)
      (response/content-type
       {:status 200
        :body (json/write-str track)}
       "application/json"))))
