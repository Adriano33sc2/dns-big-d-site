(ns dns-big-d-site.backend.core
  (:require [clojure.java.io :as io]
            [compojure.core :refer [routes]]
            [compojure.route :as route]
            [dns-big-d-site.backend.db :as db]
            [dns-big-d-site.backend.routes.auth :refer [auth-routes]]
            [dns-big-d-site.backend.routes.build-orders :refer [build-order-routes]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.json :as json]
            [ring.middleware.multipart-params :as multipart]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.resource :refer [wrap-resource]])
  (:gen-class))

(defn- serve-index [request]
  (when (and (= :get (:request-method request))
             (not (.startsWith (:uri request) "/api")))
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (slurp (io/resource "index.html"))}))

(def app
  (-> (routes serve-index
              auth-routes
              build-order-routes
              (route/not-found "Not Found"))
      json/wrap-json-response
      (json/wrap-json-body {:key-fn keyword})
      (wrap-cors :access-control-allow-origin [#"http://localhost:\d+" #"https://the-coaching-lab\.net"]
                 :access-control-allow-methods [:get :post :put :delete :options])
      wrap-keyword-params
      (multipart/wrap-multipart-params {:max-file-size (* 1024 1024 50)}) ; 50MB max
      (wrap-resource "")))

(defn -main [& [port]]
  (let [port (or port 3000)]
    (println "Initializing database...")
    (db/init-db!)
    (db/run-migrations)
    (println "Starting server on port" port)
    (jetty/run-jetty app
                     {:port port
                      :join? false})))
