(ns derztunes.core
  (:require [derztunes.config :as config]
            [derztunes.s3 :as s3])
  (:gen-class))

;; TODO: Add -conf flag for specifying different config files.
;; TODO: Add -migrate flag for applying migrations and exiting.
(defn -main []
  (println "Hello, World!"))

(comment

  (def conf (config/from-file! "derztunes.edn"))
  (config/db-uri conf)
  (config/s3-uri conf)

  (def s3-client (s3/connect! (config/s3-uri conf)))
  (s3/list-buckets! s3-client)
  (s3/list-objects! s3-client "derztunes")

  :rcf)
