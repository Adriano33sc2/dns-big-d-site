(ns dns-big-d-site.backend.core
  (:require [compojure.core :refer [routes]]
            [compojure.route :as route]
            [dns-big-d-site.backend.db :as db]
            [dns-big-d-site.backend.routes.auth :refer [auth-routes-with-middleware]]
            [dns-big-d-site.backend.routes.build-orders :refer [build-order-routes-with-middleware]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.multipart-params :as multipart]
            [ring.middleware.resource :refer [wrap-resource]])
  (:gen-class))

(def app
  (-> (routes auth-routes-with-middleware
              build-order-routes-with-middleware
              (route/not-found "Not Found"))
      (wrap-cors :access-control-allow-origin [#"http://localhost:\d+"]
                 :access-control-allow-methods [:get :post :put :delete :options])
      (multipart/wrap-multipart-params {:max-file-size (* 1024 1024 50)}) ; 50MB max
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
