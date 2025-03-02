(ns derztunes.api
  (:require [derztunes.db :as db]
            [derztunes.json :as json]
            [derztunes.s3 :as s3]
            [derztunes.util :as util]
            [java-time.api :as jt]))

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

;; Take a track and ensure it has a valid, non-expired signed URL.
;; If the a new URL was generated, update the track.
(defn- activate-track! [s3-conn track]
  (if (valid-signed-url? track)
    track
    (let [expiry (util/hours->seconds 24)
          signed-url (s3/get-signed-url s3-conn (:track/path track) expiry)
          signed-url-expires-at (util/seconds-from-now! expiry)]
      (with-signed-url track signed-url signed-url-expires-at))))

(defn- with-incremented-play-count [track]
  (-> track
      (update :track/play-count inc)
      (assoc :track/updated-at (jt/instant))))

(defn sign-track-handler [db-conn s3-conn]
  (fn [req]
    (let [track-id (-> req :params :track-id)
          track-id (parse-uuid track-id)
          track (db/read-track! db-conn track-id)
          track (activate-track! s3-conn track)
          track (with-incremented-play-count track)]
      (db/update-track! db-conn track)
      (json/response track))))
