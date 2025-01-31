(ns derztunes.util
  (:require [clojure.string :as str]
            [java-time.api :as jt]))

(defn get-or [m k default]
  (let [v (get m k)]
    (if (str/blank? v) default v)))

(defn base-name [path]
  (-> path
      (str/split #"/")
      (last)))

(defn split-ext [name]
  (let [parts (str/split name #"\.")
        name (first parts)
        ext (str/join "." (rest parts))
        ext (if (str/blank? ext) "" (str "." ext))]
    [name ext]))

(defn mp3-file? [path]
  (str/ends-with? path ".mp3"))

(defn m4a-file? [path]
  (str/ends-with? path ".m4a"))

(defn audio-file? [path]
  (or (mp3-file? path) (m4a-file? path)))

(defn back-slash->forward-slash [path]
  (str/replace path #"\\" "/"))

(defn hours->seconds [hours]
  (* hours 60 60))

(defn seconds-from [t seconds]
  (jt/plus t (jt/seconds seconds)))

(defn seconds-from-now! [seconds]
  (seconds-from (jt/instant) seconds))

;; Based on:
;; https://stackoverflow.com/a/15715610
(defmacro ignore-errors [& body]
  `(try ~@body (catch Exception e#)))
