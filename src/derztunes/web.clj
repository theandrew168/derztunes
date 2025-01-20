(ns derztunes.web
  (:require [derztunes.db :as db]
            [hiccup.page :as html]))

;; TODO: Split the actual handler logic into the handler? Let the -html funcs all be data?
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
  [:li.track
   [:input {:type "hidden" :value (:track/id track)}]
   (:track/path track)])

(defn- index-html [tracks q]
  (page-html
   [:body
    [:header.header
     [:div.player [:button#player "???"]]
     [:div.status [:span#status "Status"]]
     [:div.search
      [:form {:method "GET" :action "/"}
       [:input {:type "text" :name "q" :placeholder "Search..."}]]]]
    [:main.main
     [:div.sidebar "Playlists go here!"]
     [:div.content
      [:ul (map track-html tracks)]]]
    [:footer.footer
     [:div.controls "Controls"]
     [:div.metadata "Metadata"]
     [:div.settings "Settings"]]]))

(defn index-handler [db-conn]
  (fn [req]
    (println (:params req))
    (let [params (:params req)
          q (:q params)
          tracks (if q
                   (db/search-tracks! db-conn q)
                   (db/list-tracks! db-conn))]
      (index-html tracks q))))
