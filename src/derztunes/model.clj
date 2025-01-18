(ns derztunes.model
  (:require [java-time.api :as jt]))

;; Not quite "pure", but "honest".
(defn make-track [name path]
  (let [now (jt/instant)]
    {:track/id (random-uuid)
     :track/name name
     :track/path path
     :track/created-at now
     :track/updated-at now}))

(comment

  (make-track "foo" "/path/to/foo")

  :rcf)
