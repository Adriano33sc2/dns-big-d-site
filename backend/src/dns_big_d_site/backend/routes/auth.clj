(ns dns-big-d-site.backend.routes.auth
  (:require [compojure.core :refer [defroutes POST]]
            [ring.middleware.json :as json]
            [buddy.hashers :as hashers]
            [dns-big-d-site.backend.db :as db]
            [dns-big-d-site.backend.middleware.auth :as auth]))

(defn- find-user-by-username [username]
  (db/execute-one! "SELECT id, username, password_hash, role FROM users WHERE username = ?"
                   username))

(defn login [{{:keys [username password]} :body}]
  (let [user (find-user-by-username username)]
    (if (and user (hashers/check password (:users/password_hash user)))
      (let [token (auth/create-session username)]
        {:status 200
         :body {:token token
                :username username
                :role (keyword (:role user))}})
      {:status 401
       :body {:error "Invalid credentials"}})))

(defroutes auth-routes
  (POST "/api/auth/login" [] login))

(defroutes auth-routes-with-middleware
  (-> auth-routes
      (json/wrap-json-body {:key-fn keyword})
      json/wrap-json-response))
