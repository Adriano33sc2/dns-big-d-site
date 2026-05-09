(ns dns-big-d-site.backend.routes.build-orders
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [compojure.core :refer [DELETE GET POST PUT defroutes routes]]
            [dns-big-d-site.backend.db :as db]
            [dns-big-d-site.backend.middleware.auth :as auth]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]])
  (:import (java.io File FileOutputStream)
           (java.nio.file Files)
           (java.util.concurrent TimeUnit)))

(defn- parse-time-to-seconds [time-str]
  (let [parts (str/split time-str #":")]
    (if (= (count parts) 2)
      (let [[mins secs] parts
            mins-int (Integer/parseInt mins)
            secs-int (Integer/parseInt secs)]
        (+ (* mins-int 60) secs-int))
      0)))

(defn- build-order-line? [line]
  (boolean (re-matches #"\d+ \d+:\d+ .+" line)))

(defn- parse-spawningtool-output [output]
  (let [lines (str/split output #"\n")
        ;; Skip first 3 metadata lines, collect build order lines until blank line or non-matching line
        dns-section (loop [lines (drop 3 lines) result []]
                      (if (empty? lines)
                        result
                        (let [line (first lines)
                              rest (rest lines)]
                          (if (str/blank? line)
                            result
                            (if (build-order-line? line)
                              (recur rest (conj result line))
                              result)))))
        steps (mapv (fn [line]
                      (let [parts (str/split line #" " 3)
                            supply (when (> (count parts) 0)
                                     (try (Integer/parseInt (nth parts 0))
                                          (catch Exception _ nil)))
                            time-str (when (> (count parts) 1)
                                       (nth parts 1))
                            time-seconds (parse-time-to-seconds time-str)
                            action-name (when (> (count parts) 2)
                                          (nth parts 2))]
                        {:supply supply
                         :time-seconds time-seconds
                         :action-name action-name}))
                    dns-section)]
    steps))

(defn- list-build-orders [_]
  (let [rows (db/execute! "SELECT id, name, author, play_style, youtube_url
                           FROM build_orders ORDER BY created_at DESC")
        rows (mapv #(select-keys % [:build_orders/id :build_orders/name :build_orders/author :build_orders/play_style :build_orders/youtube_url]) rows)]
    {:status 200 :body {:build-orders rows}}))

(defn- get-build-order-by-id [{{:keys [id]} :params}]
  (let [bo (db/execute-one! "SELECT * FROM build_orders WHERE id = ?" (parse-long id))
        steps (db/execute! "SELECT * FROM build_order_steps WHERE build_order_id = ? ORDER BY sort_order" (parse-long id))
        bo (when bo
             (-> bo
                 (assoc :steps steps)))]
    (if bo
      {:status 200 :body bo}
      {:status 404 :body {:error "Build order not found"}})))

(defn- create-build-order [{:keys [name author play_style strategic_goals counters weaknesses transition_plan youtube_url]}]
  (let [id (db/execute-one! "INSERT INTO build_orders (name, author, play_style, strategic_goals, counters, weaknesses, transition_plan, youtube_url)
                             VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                             RETURNING id"
                            name author play_style strategic_goals counters weaknesses transition_plan youtube_url)]
    (:build_orders/id id)))

(defn- update-build-order [{{:keys [id]} :params {:keys [name author play_style strategic_goals counters weaknesses transition_plan youtube_url]} :body}]
  (try
    (let [id (parse-long id)
          set-clauses (filter second
                              [["name" name]
                               ["author" author]
                               ["play_style" play_style]
                               ["strategic_goals" strategic_goals]
                               ["counters" counters]
                               ["weaknesses" weaknesses]
                               ["transition_plan" transition_plan]
                               ["youtube_url" youtube_url]])
          set-str (str/join ", " (map #(format "%s = ?" (first %)) set-clauses))
          set-vals (mapv second set-clauses)]
      (when (empty? set-str)
        (throw (Exception. "No fields to update")))
      (apply (partial db/execute! (str "UPDATE build_orders SET " set-str ", updated_at = NOW() WHERE id = ?")) (into set-vals [id]))
      {:status 200})
    (catch Exception e
      {:status 500 :body {:error (.getMessage e)}})))

(defn- delete-build-order [id]
  (db/execute! "DELETE FROM build_order_steps WHERE build_order_id = ?" id)
  (db/execute! "DELETE FROM build_orders WHERE id = ?" id)
  {:status 204})

(defn- add-steps [id steps]
  (let [rows (mapv (fn [{:keys [supply time-seconds action-name notes sort-order]}]
                     [id supply time-seconds action-name notes sort-order])
                   steps)]
    (db/insert-multi! :build_order_steps [:build_order_id :supply :time_seconds :action_name :notes :sort_order] rows)))

(defn- add-step [{{:keys [id]} :params {:keys [supply time-seconds action-name notes sort-order]} :body}]
  (try
    (db/execute-one! "INSERT INTO build_order_steps (build_order_id, supply, time_seconds, action_name, notes, sort_order)
                      VALUES (?, ?, ?, ?, ?, ?)"
                     id supply time-seconds action-name notes sort-order)
    (catch Exception e
      {:status 500 :body {:error (.getMessage e)}})))

(defn- update-step [{{:keys [step-id]} :params {:keys [supply time_seconds action_name notes sort_order]} :body}]
  (try
    (let [step-id (parse-long step-id)
          set-clauses (filter second
                              [["supply" supply]
                               ["time_seconds" time_seconds]
                               ["action_name" action_name]
                               ["notes" notes]
                               ["sort_order" sort_order]])
          set-str (str/join ", " (map #(format "%s = ?" (first %)) set-clauses))
          set-vals (mapv second set-clauses)]
      (when (empty? set-str)
        (throw (Exception. "No fields to update")))
      (apply (partial db/execute! (str "UPDATE build_order_steps SET " set-str " WHERE id = ?")) (into set-vals [step-id]))
      (db/execute-one! "SELECT * FROM build_order_steps WHERE id = ?" step-id))
    {:status 200}
    (catch Exception e
      {:status 500 :body {:error (.getMessage e)}})))

(defn- delete-step [{{:keys [step-id]} :params}]
  (db/execute! "DELETE FROM build_order_steps WHERE id = ?" (parse-long step-id))
  {:status 204})

(defn- parse-replay-upload [file-stream bo-meta]
  (let [replay-file (File/createTempFile "replay" ".SC2Replay")]
    (try
      (with-open [out (FileOutputStream. replay-file)]
        (io/copy file-stream out))
      (let [process-builder (ProcessBuilder. ["/Users/iceman/sc2-replay-test/bin/python" "-m" "spawningtool" (str replay-file) "--build"])
            process (.start process-builder)]
        (with-open [reader (io/reader (.getInputStream process))]
          (let [output (slurp reader)
                correct? (.waitFor process 120 TimeUnit/SECONDS)]
            (if correct?
              (let [steps (parse-spawningtool-output output)
                    bo-id (create-build-order bo-meta)
                    steps-with-bo-id (mapv (fn [step idx]
                                             (assoc step :build-order-id bo-id :sort-order idx))
                                           steps (range))]
                (add-steps bo-id steps-with-bo-id)
                {:status 201
                 :body (:body (get-build-order-by-id {:params {:id (str bo-id)}}))})
              {:status 500 :body {:error "Spawningtool parsing failed"}}))))
      (catch Exception e
        {:status 500 :body {:error (.getMessage e)}})
      (finally
        (Files/delete (.toPath replay-file))))))

(defn replay-upload [request]
  (let [file-data (get-in request [:params :file])
        bo-meta (dissoc (:params request) :file)
        file-stream (:tempfile file-data)]
    (if-not file-stream
      {:status 400 :body {:error "No file uploaded"}}
      (parse-replay-upload file-stream bo-meta))))

;; ─── Public routes (no auth required) ──────────────────────────
(defroutes public-routes
  (GET "/api/build-orders" [] list-build-orders)
  (GET "/api/build-orders/:id" [_] get-build-order-by-id))

;; ─── Protected routes (auth required) ──────────────────────────
(defroutes protected-routes
  (POST "/api/build-orders" []
    {:status 201 :body #(create-build-order (:body %))})

  (PUT "/api/build-orders/:id" [] update-build-order)

  (DELETE "/api/build-orders/:id" [id]
    (delete-build-order (Integer/parseInt id)))

  (POST "/api/build-orders/:id/steps" [] add-step)

  (PUT "/api/build-orders/:id/steps/:step-id" [] update-step)

  (DELETE "/api/build-orders/:id/steps/:step-id" [] delete-step)

  (POST "/api/replay/upload" [] replay-upload))

(defroutes build-order-routes
  (routes public-routes (auth/auth-middleware protected-routes)))
