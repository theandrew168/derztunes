(ns derztunes.web
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [derztunes.db :as db]
            [derztunes.s3 :as s3]
            [derztunes.track :as track]
            [hiccup.page :as html]
            [java-time.api :as jt]
            [org.httpkit.server :as hk-server]))

(defn- page-html [content]
  (html/html5
   {:leng "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:title "DerzTunes"]
    [:link {:href "/css/derztunes.css" :rel "stylesheet"}]]
   content))

(defn- track-html [t]
  [:div
   [:h2 (track/name t)]
   [:audio {:controls true :src (track/signed-url t)}]])

(defn- index-html [tracks]
  (page-html
   [:body
    [:h1 "Welcome to DerzTunes!!!"]
    (map track-html tracks)]))

;; TODO: Find a better place for this.
(defn- activate-track! [db s3 t]
  (if (track/signed-url t)
    t
    (let [expiry (* 24 60 60)
          signed-url (s3/get-signed-url s3 "derztunes" (track/path t) expiry)
          signed-url-expires-at (jt/plus (jt/instant) (jt/seconds expiry))
          t (track/with-signed-url t signed-url signed-url-expires-at)]
      (db/update-track! db t)
      t)))

(defn- index-handler [db s3]
  (fn [_]
    (let [tracks (db/list-tracks! db)
          tracks (map #(activate-track! db s3 %) tracks)]
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
