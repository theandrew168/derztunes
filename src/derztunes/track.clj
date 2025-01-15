(ns derztunes.track
  (:require [java-time.api :as jt]))

;; Not quite "pure", but "honest".
(defn make [name path]
  (let [now (jt/instant)]
    {:track/id (random-uuid)
     :track/name name
     :track/path path
     :track/created-at now
     :track/updated-at now}))

;; Expects "track"-qualified keys.
;; TODO: Verify that all required keys are present.
(defn from-map [m] m)

(defn id [track]
  (:track/id track))

#_{:clj-kondo/ignore [:redefined-var]}
(defn name [track]
  (:track/name track))

(defn path [track]
  (:track/path track))

(defn- expires-soon? [expires-at]
  (let [minutes-until-expiration (jt/time-between (jt/instant) expires-at :minutes)]
    (< minutes-until-expiration 30)))

(defn signed-url [track]
  (let [expires-at (:track/signed-url-expires-at track)]
    (if (or (nil? expires-at) (expires-soon? expires-at))
      nil
      (:track/signed-url track))))

(defn signed-url-expires-at [track]
  (:track/signed-url-expires-at track))

(defn created-at [track]
  (:track/created-at track))

(defn updated-at [track]
  (:track/updated-at track))

(defn with-signed-url [track signed-url signed-url-expires-at]
  (assoc track
         :track/signed-url signed-url
         :track/signed-url-expires-at signed-url-expires-at
         :track/updated-at (jt/instant)))

(comment

  (make "foo" "/path/to/fooz")

  (def t (make "foo" "/path/to/foo"))
  (def t (with-signed-url t "http://example.com/foo.mp3" (jt/instant)))
  (signed-url t)

  (expires-soon? (signed-url-expires-at t))
  (expires-soon? (jt/plus (jt/instant) (jt/minutes 31)))

  :rcf)
