(ns derztunes.config
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]))

(defn read-file! [path]
  (-> (slurp path)
      (edn/read-string)))

(defn- env-or-default! [key default]
  (let [value (System/getenv key)]
    (if (str/blank? value) default value)))

(defn read-port! []
  (let [port (env-or-default! "PORT" "5000")]
    (Integer/parseInt port)))
