(ns derztunes.s3
  (:require [clojure.string :as string])
  (:import [io.minio GetPresignedObjectUrlArgs ListObjectsArgs MinioClient]
           [io.minio.http Method]
           [java.net URI]))

;; References:
;; https://minio-java.min.io/overview-summary.html
;; https://github.com/minio/minio-java/blob/release/examples/ListObjects.java
;; https://github.com/minio/minio-java/blob/release/examples/GetPresignedObjectUrl.java

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

;; TODO: Proper domain model for this?
(defn- bucket->map [bucket]
  {:name (.name bucket)})

(defn list-buckets! [client]
  (map bucket->map (.listBuckets client)))

(defn- list-objects-args [bucket]
  (-> (ListObjectsArgs/builder)
      (.bucket bucket)
      (.recursive true)
      (.maxKeys 100) ; TODO: Remove this when ready to go live.
      (.build)))

;; TODO: Proper domain model for this?
(defn- object->map [object]
  {:name (.objectName object)
   :size (.size object)})

(defn list-objects! [client bucket]
  (map #(object->map (.get %)) (.listObjects client (list-objects-args bucket))))

;; TODO: Expiry is in seconds. How can I make that clear?
(defn- get-presigned-object-url-args [bucket object expiry]
  (-> (GetPresignedObjectUrlArgs/builder)
      (.method Method/GET)
      (.bucket bucket)
      (.object object)
      (.expiry expiry)
      (.build)))

(defn get-signed-url [client bucket object expiry]
  (.getPresignedObjectUrl client (get-presigned-object-url-args bucket object expiry)))

(comment

  (def uri "s3://minioadmin:minioadmin@localhost:9000/derztunes")
  (parse-endpoint uri)
  (parse-credentials uri)
  (parse-bucket uri)

  (def s3 (connect! uri))
  (get-signed-url s3 "derztunes" "test.mp3" (* 24 60 60))

  :rcf)
