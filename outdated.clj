(require '[babashka.http-client :as http])
(require '[cheshire.core :as json])
(require '[clojure.edn :as edn])

(defn read-deps []
  (-> (slurp "deps.edn")
      (edn/read-string)
      :deps))

(defn fetch-json [url]
  (println "fetching" url)
  (->
   (http/get url)
   :body
   (json/parse-string true)))

(defn fetch-artifact [dep]
  (let [url (str "https://clojars.org/api/artifacts/" dep)]
    (fetch-json url)))

(defn run []
  (let [deps (read-deps)
        artifacts (map fetch-artifact (keys deps))]
    (println deps)
    (doall (map println artifacts))))

(run)
