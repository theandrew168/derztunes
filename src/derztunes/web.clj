(ns derztunes.web
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [derztunes.db :as db]
            [hiccup.page :as html]
            [org.httpkit.server :as hk-server]))

(defn- index []
  (html/html5
   {:leng "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:title "DerzTunes"]
    [:link {:href "/css/derztunes.css" :rel "stylesheet"}]]
   [:body
    [:h1 "Welcome to DerzTunes!!!"]]))

(defn routes [db-conn s3-conn]
  (c/routes
   (c/GET "/" [] (index))
   (c/GET "/tracks" [] (map str (db/list-tracks! db-conn)))
   (route/resources "/" {:root "public"})
   (route/not-found "Page not found.")))

(defn run-server! [app]
  (hk-server/run-server app {:ip "127.0.0.1" :port 5000}))

(defn stop-server! [server]
  (server :timeout 500))

(comment

  :rcf)
