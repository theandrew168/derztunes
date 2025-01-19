(ns derztunes.web
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [derztunes.db :as db]
            [derztunes.s3 :as s3]
            [hiccup.page :as html]
            [java-time.api :as jt]
            [org.httpkit.server :as hk-server]))

(defn- page-html [content]
  (html/html5
   {:leng "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:title "DerzTunes"]
    [:script {:src "/js/derztunes.js" :defer true}]
    [:link {:href "/css/reset.css" :rel "stylesheet"}]
    [:link {:href "/css/fonts.css" :rel "stylesheet"}]
    [:link {:href "/css/derztunes.css" :rel "stylesheet"}]]
   content))

(defn- track-html [track]
  [:li.track (:track/name track) [:audio {:src (:track/signed-url track)}]])

(defn- index-html [tracks]
  (page-html
   [:body
    [:header.header
     [:div.player [:button#player "???"]]
     [:div.status [:span#status "Status"]]
     [:div.search [:input {:type "text" :placeholder "Search..."}]]]
    [:main.main
     [:div.sidebar "Playlists go here!"]
     [:div.content
      [:ul (map track-html tracks)]]]
    [:footer.footer
     [:div.controls "Controls"]
     [:div.metadata "Metadata"]
     [:div.settings "Settings"]]]))

;; TODO: Find a better place for these.

;; Check if a given instant is within 30 minutes of the current time.
(defn- expires-soon? [expires-at]
  (let [minutes-until-expiration (jt/time-between (jt/instant) expires-at :minutes)]
    (< minutes-until-expiration 30)))

;; Given a track, check if its signed URL is valid and won't expire soon.
;; If it is, return it, otherwise return nil.
(defn signed-url [track]
  (let [expires-at (:track/signed-url-expires-at track)]
    (if (or (nil? expires-at) (expires-soon? expires-at))
      nil
      (:track/signed-url track))))

;; Update a track with a new signed URL and its expiration time.
(defn with-signed-url [track signed-url signed-url-expires-at]
  (assoc track
         :track/signed-url signed-url
         :track/signed-url-expires-at signed-url-expires-at
         :track/updated-at (jt/instant)))

;; Take a track and ensure it has a valid, non-expired signed URL.
;; If the a new URL was generated, update the row in the database.
(defn- activate-track! [db-conn s3-conn t]
  (if (signed-url t)
    t
    (let [expiry (* 24 60 60)
          signed-url (s3/get-signed-url s3-conn "derztunes" (:track/path t) expiry)
          signed-url-expires-at (jt/plus (jt/instant) (jt/seconds expiry))
          t (with-signed-url t signed-url signed-url-expires-at)]
      (db/update-track! db-conn t)
      t)))

(defn- index-handler [db-conn s3-conn]
  (fn [_]
    (let [tracks (db/list-tracks! db-conn)
          tracks (map #(activate-track! db-conn s3-conn %) tracks)]
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
