(ns dns-big-d-site.backend.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [dns-big-d-site.backend.routes.auth :refer [auth-routes]]
            [dns-big-d-site.backend.middleware.auth :as auth]
            [dns-big-d-site.backend.db :as db])
  (:gen-class))

(def app-routes
  (defroutes main-middleware
    auth-routes
    (route/not-found "Not Found")))

(def app
  (-> main-middleware
      (wrap-cors :access-control-allow-origin [#"http://localhost:\d+"]
                 :access-control-allow-methods [:get :post :put :delete :options])
      (wrap-file "public")
      (wrap-resource "public")))

(defn -main [& [port]]
  (let [p (Integer. (or port 3000))]
    (println "Initializing database...")
    (db/init-db!)
    (db/run-migrations)
    (println "Starting server on port" p)
    (jetty/run-jetty app
                     {:port p
                      :join? false})))
