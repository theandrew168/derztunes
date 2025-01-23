(ns derztunes.cli
  (:require [clojure.string :as str]))

(defn parse-flags [args m]
  (let [curr (first args)
        next (second args)]
    (if (nil? curr) m
        (if (or (nil? next) (str/starts-with? next "-"))
          (recur (drop 1 args) (assoc m curr true))
          (recur (drop 2 args) (assoc m curr next))))))

(comment

  (parse-flags ["-conf" "derztunes.edn" "-sync"] {})

  :rcf)
