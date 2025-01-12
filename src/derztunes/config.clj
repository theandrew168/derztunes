(ns derztunes.config
  (:require [clojure.edn :as edn]))

(defn from-file! [path]
  (-> (slurp path)
      (edn/read-string)))

(defn db-uri [conf]
  (:db-uri conf))

(defn s3-uri [conf]
  (:s3-uri conf))
