(ns derztunes.server
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [derztunes.api :as api]
            [derztunes.web :as web]
            [org.httpkit.server :as hk-server]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.gzip :as gzip]))

(defn routes [db-conn s3-conn]
  (c/routes
   (c/GET "/" [] (web/index-handler db-conn))
   (c/GET "/playlist/:playlist-id" [] (web/playlist-handler db-conn))
   (c/POST "/api/v1/track/:track-id/sign" [] (api/sign-track-handler db-conn s3-conn))
   (c/GET "/ping" []
     {:status 200
      :headers {"Content-Type" "text/plain"}
      :body "pong\n"})
   (route/resources "/" {:root "public"})
   (route/not-found "Page not found.")))

(defn app [db-conn s3-conn]
  (-> (routes db-conn s3-conn)
      (defaults/wrap-defaults defaults/api-defaults)
      (gzip/wrap-gzip)))

(defn start! [app port]
  (hk-server/run-server app {:ip "127.0.0.1" :port port}))

(defn stop! [server]
  (server :timeout 500))
