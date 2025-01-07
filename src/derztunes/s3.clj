(ns derztunes.s3
  (:require [clojure.string :as string])
  (:import [io.minio ListObjectsArgs MinioClient]
           [java.net URI]))

(defn- parse-endpoint [s3-uri]
  (let [uri (URI. s3-uri)
        host (.getHost uri)
        port (.getPort uri)
        scheme (if (or (= host "localhost") (= host "127.0.0.1")) "http" "https")]
    (format "%s://%s:%s" scheme host port)))

(defn- parse-credentials [s3-uri]
  (let [uri (URI. s3-uri)
        user-info (.getUserInfo uri)
        [access-key secret-key] (string/split user-info #":")]
    {:access-key access-key :secret-key secret-key}))

(defn- parse-bucket [s3-uri]
  (let [uri (URI. s3-uri)
        path (.getPath uri)]
    (subs path 1)))

(defn connect! [s3-uri]
  (let [endpoint (parse-endpoint s3-uri)
        credentials (parse-credentials s3-uri)]
    (-> (MinioClient/builder)
        (.endpoint endpoint)
        (.credentials (credentials :access-key) (credentials :secret-key))
        (.build))))

(defn- bucket->map [bucket]
  {:name (.name bucket)})

(defn list-buckets! [client]
  (map bucket->map (.listBuckets client)))

(defn- bucket->args [bucket]
  (-> (ListObjectsArgs/builder)
      (.bucket bucket)
      (.recursive true)
      (.maxKeys 100) ; TODO: Remove this when ready to go live.
      (.build)))

(defn- object->map [object]
  {:name (.objectName object)
   :size (.size object)})

(defn list-objects! [client bucket]
  (map #(object->map (.get %)) (.listObjects client (bucket->args bucket))))

(comment
  (def uri "s3://minioadmin:minioadmin@localhost:9000/derztunes")
  (parse-endpoint uri)
  (parse-credentials uri)
  (parse-bucket uri)

  :rcf)
