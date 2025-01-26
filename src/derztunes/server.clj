(ns derztunes.server
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [derztunes.api :as api]
            [derztunes.web :as web]
            [org.httpkit.server :as hk-server]
            [ring.middleware.defaults :as defaults]))

(defn routes [db-conn s3-conn]
  (c/routes
   (c/GET "/" [] (web/index-handler db-conn))
   (c/POST "/api/v1/track/:id/sign" [] (api/sign-track-handler db-conn s3-conn))
   (route/resources "/" {:root "public"})
   (route/not-found "Page not found.")))

(defn app [db-conn s3-conn]
  (defaults/wrap-defaults (routes db-conn s3-conn) defaults/api-defaults))

(defn start! [app port]
  (hk-server/run-server app {:ip "127.0.0.1" :port port}))

(defn stop! [server]
  (server :timeout 500))
