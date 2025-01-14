(ns derztunes.web
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [derztunes.db :as db]
            [derztunes.s3 :as s3]
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

(defn- track-html [track]
  [:div
   [:h2 (:track/name track)]
   [:audio {:controls true :src (:track/signed-url track)}]])

(defn- index-html [tracks]
  (page-html
   [:body
    [:h1 "Welcome to DerzTunes!!!"]
    (map track-html tracks)]))

(defn- activate-track [db s3 track]
  (if (:track/signed-url track)
    track
    (let [signed-url (s3/get-signed-url s3 "derztunes" (:track/path track))
          track (assoc track :track/signed-url signed-url)]
      (db/update-track! db track)
      track)))

(defn- index-handler [db s3]
  (fn [req]
    (let [tracks (db/list-tracks! db)
          tracks (map #(activate-track db s3 %) tracks)]
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
