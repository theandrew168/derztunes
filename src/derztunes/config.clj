(ns derztunes.config
  (:require [clojure.edn :as edn]))

(defn from-file! [path]
  (-> (slurp path)
      (edn/read-string)))

(defn db-uri [conf]
  (get conf :db-uri))

(defn s3-uri [conf]
  (get conf :s3-uri))
