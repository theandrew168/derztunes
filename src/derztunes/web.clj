(ns derztunes.web
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [derztunes.db :as db]
            [hiccup.page :as html]
            [org.httpkit.server :as hk-server]))

(defn- page-html [content]
  (html/html5
   {:leng "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:title "DerzTunes"]
    [:link {:href "/css/derztunes.css" :rel "stylesheet"}]]
   content))

(defn- index-html [tracks]
  (page-html
   [:body
    [:h1 "Welcome to DerzTunes!!!"]
    (map #(vector :p (str %)) tracks)]))

(defn- index-handler [db s3]
  (fn [req]
    (let [tracks (db/list-tracks! db)]
      (index-html tracks))))

(defn routes [db-conn s3-conn]
  (c/routes
   (c/GET "/" [] (index-handler db-conn s3-conn))
   (route/resources "/" {:root "public"})
   (route/not-found "Page not found.")))

(defn run-server! [app]
  (hk-server/run-server app {:ip "127.0.0.1" :port 5000}))

(defn stop-server! [server]
  (server :timeout 500))

(comment

  :rcf)
