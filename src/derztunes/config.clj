(ns derztunes.config
  (:require [clojure.edn :as edn]
            [derztunes.util :as util]))

(defn read-file! [path]
  (-> (slurp path)
      (edn/read-string)))

(defn read-port
  ([env] (read-port env "5000"))
  ([env default]
   (let [port (util/get-or env "PORT" default)]
     (Integer/parseInt port))))
