(ns derztunes.cli
  (:require [clojure.string :as str]))

(defn parse-flags
  "Parse command line flags into a map using very simple rules. If a flag has
   no value, it is considered true. Otherwise, it's value is read as a string."
  ([args] (parse-flags args {}))
  ([args m]
   (let [curr (first args)
         next (second args)]
     (if (nil? curr) m
         (if (or (nil? next) (str/starts-with? next "-"))
           (recur (drop 1 args) (assoc m curr true))
           (recur (drop 2 args) (assoc m curr next)))))))

(comment

  (parse-flags ["-migrate" "-conf" "derztunes.edn" "-sync"])

  :rcf)
