(ns derztunes.web
  (:require [derztunes.db :as db]
            [rum.core :as rum]))

(defn- render [content]
  (str "<!DOCTYPE html>" (rum/render-static-markup content)))

(defn- page-html [content]
  [:html {:lang "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:title "DerzTunes"]
    [:script {:src "/js/derztunes.js" :defer true}]
    [:link {:href "/css/reset.css" :rel "stylesheet"}]
    [:link {:href "/css/fonts.css" :rel "stylesheet"}]
    [:link {:href "/css/derztunes.css" :rel "stylesheet"}]]
   content])

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

(defn- controls-html []
  [:div.controls
   [:button#shuffle "Shuffle: Off"]
   [:button#repeat "Repeat: Off"]])

(defn- index-html [q tracks playlists]
  (page-html
   [:body.sans-serif
    [:header.header
     [:div.player
      [:button#prev "<"]
      [:button#play "???"]
      [:button#next ">"]]
     [:div.status
      [:a {:href "/"} "DerzTunes"]
      [:div#title "Click a song to play!"]
      [:audio#audio {:controls true}]]
     [:div.search
      [:form {:method "GET" :action "?"}
       [:input {:type "text" :name "q" :value q :placeholder "Search..."}]]]]
    [:div.content-header
     [:span.playlist-header "Sources"]
     [:span.track-header
      [:span.title "Name"]
      [:span.artist "Artist"]
      [:span.album "Album"]
      [:span.play-count "Plays"]]]
    [:main.main
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
     (controls-html)
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
      (render (index-html q tracks playlists)))))

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
      (render (index-html q tracks playlists)))))

(comment

  (page-html "foo")

  :rcf)
