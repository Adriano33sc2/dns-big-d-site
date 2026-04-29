(ns dns-big-d-site.backend.core
  (:require [compojure.core :refer [defroutes]]
            [compojure.route :as route]
            [dns-big-d-site.backend.db :as db]
            [dns-big-d-site.backend.routes.auth :refer [auth-routes-with-middleware]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.resource :refer [wrap-resource]])
  (:gen-class))

(defroutes main-middleware
  auth-routes-with-middleware
  (route/not-found "Not Found"))

(def app
  (-> main-middleware
       (wrap-cors :access-control-allow-origin [#"http://localhost:\d+"]
                  :access-control-allow-methods [:get :post :put :delete :options])
      (wrap-file "public")
      (wrap-resource "public")))

(defn -main [& [port]]
  (let [port (or port 3000)]
    (println "Initializing database...")
    (db/init-db!)
    (db/run-migrations)
    (println "Starting server on port" port)
    (jetty/run-jetty app
                     {:port port
                      :join? false})))
