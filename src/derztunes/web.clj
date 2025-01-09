(ns derztunes.web
  (:require [org.httpkit.server :as hk-server]))

(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Welcome to DerzTunes!!!"})

(defn run-server! []
  (hk-server/run-server #'app {:port 8080}))

(defn stop-server! [server]
  (server :timeout 500))

(comment
  (def server (run-server!))
  (stop-server! server)

  :rcf)
