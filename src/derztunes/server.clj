(ns derztunes.server
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [derztunes.api :as api]
            [derztunes.web :as web]
            [org.httpkit.server :as hk-server]))

(defn routes [db-conn s3-conn]
  (c/routes
   (c/GET "/" [] (web/index-handler db-conn))
   (c/POST "/api/v1/track/:id/sign" [] (api/sign-track-handler db-conn s3-conn))
   (route/resources "/" {:root "public"})
   (route/not-found "Page not found.")))

(defn app [db-conn s3-conn]
  (routes db-conn s3-conn))

(defn start! [app]
  (hk-server/run-server app {:ip "127.0.0.1" :port 5000}))

(defn stop! [server]
  (server :timeout 500))
