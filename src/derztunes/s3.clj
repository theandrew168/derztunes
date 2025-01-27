(ns derztunes.s3
  (:require [clojure.string :as str])
  (:import [io.minio GetPresignedObjectUrlArgs ListObjectsArgs MinioClient]
           [io.minio.http Method]
           [java.net URI]))

;; References:
;; https://minio-java.min.io/overview-summary.html
;; https://github.com/minio/minio-java/blob/release/examples/ListObjects.java
;; https://github.com/minio/minio-java/blob/release/examples/GetPresignedObjectUrl.java

;; TODO: Support parsing URIs without explicit ports.
(defn- parse-endpoint [s3-uri]
  (let [uri (URI. s3-uri)
        host (.getHost uri)
        port (.getPort uri)
        scheme (if (or (= host "localhost") (= host "127.0.0.1")) "http" "https")]
    (format "%s://%s:%s" scheme host port)))

(defn- parse-credentials [s3-uri]
  (let [uri (URI. s3-uri)
        user-info (.getUserInfo uri)
        [access-key secret-key] (str/split user-info #":")]
    {:access-key access-key :secret-key secret-key}))

(defn- parse-bucket [s3-uri]
  (let [uri (URI. s3-uri)
        path (.getPath uri)]
    (subs path 1)))

(defn- minio-client! [endpoint credentials]
  (-> (MinioClient/builder)
      (.endpoint endpoint)
      (.credentials (credentials :access-key) (credentials :secret-key))
      (.build)))

(defn connect! [s3-uri]
  (let [endpoint (parse-endpoint s3-uri)
        credentials (parse-credentials s3-uri)
        bucket (parse-bucket s3-uri)
        client (minio-client! endpoint credentials)]
    {:s3/client client :s3/bucket bucket}))

(defn- list-objects-args [bucket]
  (-> (ListObjectsArgs/builder)
      (.bucket bucket)
      (.recursive true)
      (.build)))

(defn- object->map [object]
  {:name (.objectName object)
   :size (.size object)})

(defn list-objects! [conn]
  (map #(object->map (.get %)) (.listObjects (:s3/client conn) (list-objects-args (:s3/bucket conn)))))

;; TODO: Expiry is in seconds. How can I make that clear?
(defn- get-presigned-object-url-args [bucket object expiry]
  (-> (GetPresignedObjectUrlArgs/builder)
      (.method Method/GET)
      (.bucket bucket)
      (.object object)
      (.expiry expiry)
      (.build)))

(defn get-signed-url [conn object expiry]
  (.getPresignedObjectUrl (:s3/client conn) (get-presigned-object-url-args (:s3/bucket conn) object expiry)))

(comment

  (def uri "s3://minioadmin:minioadmin@localhost:9000/derztunes")
  (parse-endpoint uri)
  (parse-credentials uri)
  (parse-bucket uri)

  (def conn (connect! uri))
  (get-signed-url conn "test.mp3" (* 24 60 60))

  :rcf)
