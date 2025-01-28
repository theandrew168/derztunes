(ns derztunes.model
  (:require [java-time.api :as jt]))

;; Not quite "pure", but "honest".
(defn make-track [path]
  (let [now (jt/instant)]
    {:track/id (random-uuid)
     :track/path path
     :track/created-at now
     :track/updated-at now}))

(comment

  (make-track "/path/to/foo")

  :rcf)
