(ns derztunes.json
  (:require [clojure.data.json :as json]))

(defn response
  ([body] (response body 200))
  ([body status]
   {:status status
    :body (json/write-str body)
    :headers {"Content-Type" "application/json"}}))
