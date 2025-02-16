(ns derztunes.model
  (:require [java-time.api :as jt]))

;; Not quite "pure", but "honest".
(defn make-track [path size]
  (let [now (jt/instant)]
    {:track/id (random-uuid)
     :track/path path
     :track/size size
     :track/created-at now
     :track/updated-at now}))

(defn make-playlist [name]
  (let [now (jt/instant)]
    {:playlist/id (random-uuid)
     :playlist/name name
     :playlist/created-at now
     :playlist/updated-at now}))
