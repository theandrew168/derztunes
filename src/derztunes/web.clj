(ns derztunes.web
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [hiccup.page :as html]
            [org.httpkit.server :as hk-server]))

(defn- index []
  (html/html5
   [:head
    [:title "DerzTunes"]]
   [:body
    [:h1 "Welcome to DerzTunes!!!"]]))

(defn- routes []
  (c/routes
   (c/GET "/" [] (index))
   (route/not-found "Page not found.")))

(def app (routes))

(defn run-server! []
  (hk-server/run-server #'app {:ip "127.0.0.1" :port 5000}))

(defn stop-server! [server]
  (server :timeout 500))

(comment
  (def server (run-server!))
  (stop-server! server)

  :rcf)
