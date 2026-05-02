(ns dns-big-d-site.backend.middleware.auth
  (:require [dns-big-d-site.backend.db :as db]
            [ring.util.response :refer [response]]
            [clojure.string :as str])
  (:import (java.time Instant Duration)
           (java.sql Timestamp)))

(defn- generate-token []
  (str/join "" (repeatedly 64 #(char (+ (rand 26) 97)))))

(defn create-session [username]
  (let [token (generate-token)
        expires-at (Timestamp/from (.plus (Instant/now) (Duration/ofHours 24)))]
    (db/execute-one! "INSERT INTO sessions (user_id, token, expires_at)
                       SELECT u.id, ?, ? FROM users u WHERE u.username = ?"
                     token expires-at username)
    token))

(defn validate-session [token]
  (when token
    (let [row (db/execute-one! "SELECT s.user_id, u.username, u.role, s.expires_at, s.is_active
                                FROM sessions s
                                JOIN users u ON s.user_id = u.id
                               WHERE s.token = ? AND s.is_active = true"
                               token)]
      (when row
        (if (< (.getTime (:sessions/expires_at row)) (System/currentTimeMillis))
          nil
          {:user-id (:sessions/user_id row)
           :username (:users/username row)
           :role (keyword (:users/role row))})))))

(defn extract-token [request]
  (let [auth-header (get-in request [:headers "authorization"])]
    (when auth-header
      (if (str/starts-with? auth-header "Bearer ")
        (subs auth-header 7)
        auth-header))))

(defn auth-middleware [handler]
  (fn [request]
    (let [token (extract-token request)
          user-data (validate-session token)]
      (if user-data
        (handler (assoc request :current-user user-data))
        (-> (response {:error "Unauthorized"})
            (assoc :status 401
                   :headers {"WWW-Authenticate" "Bearer"}))))))

(defn require-admin [handler]
  (fn [request]
    (let [user-data (:current-user request)]
      (if (and user-data (= (:role user-data) :admin))
        (handler request)
        (-> (response {:error "Forbidden"})
            (assoc :status 403))))))
