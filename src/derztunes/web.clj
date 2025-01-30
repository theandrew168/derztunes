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
   [:span.title (or (:track/title track) (:track/path track))]
   [:span.artist (or (:track/artist track) "???")]
   [:span.album (or (:track/album track) "???")]
   [:span.play-count (or (:track/play-count track) 0)]])

(defn- playlist-html [playlist]
  [:li.playlist
   [:a {:href (str "/playlist/" (:playlist/id playlist))}
    [:div (:playlist/name playlist)]]])

(defn- index-html [q tracks playlists]
  (page-html
   [:body.sans-serif
    [:header.header
     [:div.player [:button#player "???"]]
     [:div.status
      [:a {:href "/"} "DerzTunes"]
      [:div#title "Click a song to play!"]
      [:audio#audio {:controls true}]]
     [:div.search
      [:form {:method "GET" :action "?"}
       [:input {:type "text" :name "q" :value q :placeholder "Search..."}]]]]
    [:main.main
     [:div.content-header
      [:span.playlist-header "Sources"]
      [:header.track-header
       [:span "Name"]
       [:span "Artist"]
       [:span "Album"]
       [:span "Plays"]]]
     [:div.content
      [:div.playlists
       [:ul
        [:li.playlist
         [:a {:href "/"}
          [:div "All Tracks"]]]
        (map playlist-html playlists)]]
      [:div.tracks
       [:ul (map track-html tracks)]]]]
    [:footer.footer
     [:div.controls "Controls"]
     [:div.metadata "Metadata"]
     [:div.settings "Settings"]]]))

(defn index-handler [db-conn]
  (fn [req]
    (let [params (:params req)
          q (:q params)
          tracks (if q
                   (db/search-tracks! db-conn q)
                   (db/list-tracks! db-conn))
          playlists (db/list-playlists! db-conn)]
      (index-html q tracks playlists))))

(defn playlist-handler [db-conn]
  (fn [req]
    (let [params (:params req)
          q (:q params)
          playlist-id (:playlist-id params)
          playlist (db/read-playlist! db-conn playlist-id)
          tracks (if q
                   (db/search-playlist-tracks! db-conn playlist q)
                   (db/list-playlist-tracks! db-conn playlist))
          playlists (db/list-playlists! db-conn)]
      (index-html q tracks playlists))))
