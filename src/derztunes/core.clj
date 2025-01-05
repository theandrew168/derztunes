(ns derztunes.core
  (:require [derztunes.config :as config])
  (:gen-class))

;; TODO: List files present in the S3 bucket.

;; TODO: Add -conf flag for specifying different config files.
;; TODO: Add -migrate flag for applying migrations and exiting.
(defn -main []
  (println "Hello, World!"))

(comment

  (def conf (config/from-file! "derztunes.edn"))
  (config/db-uri conf)
  (config/s3-uri conf)

  :rcf)
