(ns dns-big-d-site.core.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx]]
            [ajax.core :as ajax]
            [dns-big-d-site.core.db :refer [default-db]]))

(defn- api-url []
  "http://localhost:3000")

(defn- auth-headers [db]
  (let [token (:auth-token db)]
    (if token
      {"Authorization" (str "Bearer " token)}
      {})))

;; -- Initialize DB --------------------------------------------------

(reg-event-db :initialize-db (fn [_ _]
                               (let [token (.getItem js/localStorage "auth-token")]
                                 (if token
                                   (assoc default-db :auth-token token)
                                   default-db))))

;; -- Language -------------------------------------------------------

(reg-event-db :set-lang (fn [db [_ lang]] (assoc db :lang lang)))

;; -- Modals ---------------------------------------------------------

(reg-event-db :open-booking-modal (fn [db _] (assoc db :booking-modal-open? true)))
(reg-event-db :close-booking-modal (fn [db _] (assoc db :booking-modal-open? false)))
(reg-event-db :open-login-modal (fn [db _] (assoc db :login-modal-open? true)))
(reg-event-db :close-login-modal (fn [db _] (assoc db :login-modal-open? false
                                                      :login-error nil)))

;; -- Login ----------------------------------------------------------

(reg-event-db :set-login-username (fn [db [_ val]] (assoc db :login-username val)))
(reg-event-db :set-login-password (fn [db [_ val]] (assoc db :login-password val)))
(reg-event-db :set-login-error (fn [db [_ err]] (assoc db :login-error err)))

(reg-event-fx :login (fn [{:keys [db]} _]
                       (let [username (get db :login-username)
                             password (get db :login-password)]
                         {:db db
                          :http-xhrio {:method :post
                                       :uri "http://localhost:3000/api/auth/login"
                                       :params {:username username
                                                :password password}
                                       :timeout 5000
                                       :format (ajax/json-request-format)
                                       :response-format (ajax/json-response-format {:keywords? true})
                                       :on-success [:login-success]
                                       :on-failure [:login-failure]}})))

(reg-event-db :login-success (fn [db [_ body]]
                               (let [token (when body (:token body))
                                     username (when body (:username body))
                                     role (when body (:role body))]
                                 (if token
                                   (.setItem js/localStorage "auth-token" token)
                                   (.removeItem js/localStorage "auth-token"))
                                 (-> db
                                     (assoc :login-modal-open? false)
                                     (assoc :login-username "")
                                     (assoc :login-password "")
                                     (assoc :login-error nil)
                                     (assoc :auth-token token)
                                     (assoc :current-user {:username username :role role})))))

(reg-event-db :login-failure (fn [db [_ _]]
                               (assoc db :login-error "Invalid credentials")))

(reg-event-db :logout (fn [db _]
                        (.removeItem js/localStorage "auth-token")
                        (-> db
                            (assoc :auth-token nil)
                            (assoc :current-user nil))))

;; -- Testimonials ---------------------------------------------------

(reg-event-db :set-active-testimonial (fn [db [_ idx]] (assoc db :active-testimonial idx)))

(reg-event-db
  :next-testimonial
  (fn [db _]
    (let [next-idx (mod (inc (:active-testimonial db)) 3)]
      (assoc db :active-testimonial next-idx))))

;; -- Delete Confirm -------------------------------------------------

(reg-event-db :set-delete-confirm (fn [db [_ {:keys [type id]}]]
                                    (assoc db :delete-confirm {:type type :id id})))

(reg-event-db :clear-delete-confirm (fn [db _]
                                      (assoc db :delete-confirm nil)))

;; -- Build Orders ---------------------------------------------------

(reg-event-db :set-build-orders (fn [db [_ orders]]
                                  (assoc db :build-orders orders)))

(reg-event-db :set-selected-build-order (fn [db [_ bo]]
                                          (assoc db :selected-build-order bo)))

(reg-event-db :set-loading (fn [db [_ val]]
                             (assoc db :loading? val)))

(reg-event-db :update-build-order (fn [db [_ updated-bo]]
                                    (let [orders (:build-orders db)
                                          updated-orders (mapv #(if (= (:id %) (:id updated-bo)) updated-bo %) orders)]
                                      (-> db
                                          (assoc :build-orders updated-orders)
                                          (assoc :selected-build-order updated-bo)))))

(reg-event-db :delete-from-orders-list (fn [db [_ bo-id]]
                                         (let [orders (:build-orders db)
                                               updated-orders (remove #(= (:id %) bo-id) orders)]
                                           (assoc db :build-orders updated-orders))))

;; -- Build Order Steps (local) --------------------------------------

(reg-event-db :add-step-to-selected-bo (fn [db [_ step]]
                                         (let [bo (:selected-build-order db)
                                               new-steps (conj (:steps bo) step)]
                                           (assoc db :selected-build-order (assoc bo :steps new-steps)))))

(reg-event-db :update-step-in-selected-bo (fn [db [_ step-id updated-step]]
                                            (let [bo (:selected-build-order db)
                                                  new-steps (mapv #(if (= (:id %) step-id) updated-step %) (:steps bo))]
                                              (assoc db :selected-build-order (assoc bo :steps new-steps)))))

(reg-event-db :remove-step-from-selected-bo (fn [db [_ step-id]]
                                              (let [bo (:selected-build-order db)
                                                    new-steps (remove #(= (:id %) step-id) (:steps bo))]
                                                (assoc db :selected-build-order (assoc bo :steps new-steps)))))

(reg-event-db :update-selected-bo-field (fn [db [_ field value]]
                                          (let [bo (:selected-build-order db)]
                                            (when bo
                                              (assoc db :selected-build-order (assoc bo field value))))))

;; -- Build Order Steps (API) ----------------------------------------

(reg-event-fx :add-step-api
              (fn [{:keys [db]} [_ bo-id params]]
                {:http-xhrio {:method :post
                              :uri (str (api-url) "/api/build-orders/" bo-id "/steps")
                              :timeout 10000
                              :params params
                              :format (ajax/json-request-format)
                              :response-format (ajax/json-response-format {:keywords? true})
                              :headers (auth-headers db)
                              :on-success [:add-step-api-success]
                              :on-failure [:fetch-bo-failure]}}))

(reg-event-db :add-step-api-success (fn [db [_ response]]
                                      (let [step (:body response)
                                            bo (:selected-build-order db)]
                                        (when bo
                                          (assoc db :selected-build-order (update bo :steps conj step))))))

(reg-event-fx :update-step-api
              (fn [{:keys [db]} [_ step-id params]]
                (let [bo (:selected-build-order db)
                      bo-id (:id bo)]
                  {:http-xhrio {:method :put
                                :uri (str (api-url) "/api/build-orders/" bo-id "/steps/" step-id)
                                :timeout 10000
                                :params params
                                :format (ajax/json-request-format)
                                :response-format (ajax/json-response-format {:keywords? true})
                                :headers (auth-headers db)
                                :on-success [:update-step-api-success step-id]
                                :on-failure [:fetch-bo-failure]}})))

(reg-event-db :update-step-api-success (fn [db [_ _ step-id]]
                                         #_(let [bo (:selected-build-order db)]
                                             (when bo
                                               (assoc db :selected-build-order
                                                         (update bo :steps (fn [steps]
                                                                             (mapv #(if (= (:id %) step-id) % steps)))))))))

(reg-event-fx :delete-step-api
              (fn [{:keys [db]} [_ step-id]]
                (let [bo (:selected-build-order db)
                      bo-id (:id bo)]
                  {:http-xhrio {:method :delete
                                :uri (str (api-url) "/api/build-orders/" bo-id "/steps/" step-id)
                                :timeout 10000
                                :format (ajax/json-request-format)
                                :response-format (ajax/json-response-format {:keywords? true})
                                :headers (auth-headers db)
                                :on-success [:delete-step-api-success step-id]
                                :on-failure [:fetch-bo-failure]}})))

(reg-event-db :delete-step-api-success (fn [db [_ _ step-id]]
                                         (let [bo (:selected-build-order db)]
                                           (when bo
                                             (assoc db :selected-build-order (update bo :steps remove #(= (:id %) step-id)))))))

;; -- Build Orders API -----------------------------------------------

(reg-event-fx :fetch-build-orders
              (fn [{:keys [db]} _]
                {:http-xhrio {:method :get
                              :uri (str (api-url) "/api/build-orders")
                              :timeout 10000
                              :format (ajax/json-request-format)
                              :response-format (ajax/json-response-format {:keywords? true})
                              :headers (auth-headers db)
                              :on-success [:fetch-bo-success]
                              :on-failure [:fetch-bo-failure]}}))

(reg-event-db :fetch-bo-success (fn [db [_ response]]
                                  (assoc db :build-orders (:body response))))

(reg-event-db :fetch-bo-failure (fn [db [_ _]]
                                  db))

(reg-event-fx :fetch-build-order
              (fn [{:keys [db]} [_ id]]
                {:http-xhrio {:method :get
                              :uri (str (api-url) "/api/build-orders/" id)
                              :timeout 10000
                              :format (ajax/json-request-format)
                              :response-format (ajax/json-response-format {:keywords? true})
                              :headers (auth-headers db)
                              :on-success [:fetch-single-bo-success]
                              :on-failure [:fetch-bo-failure]}}))

(reg-event-db :fetch-single-bo-success (fn [db [_ response]]
                                         (assoc db :selected-build-order (:body response))))

(reg-event-fx :create-build-order
              (fn [{:keys [db]} [_ params]]
                {:http-xhrio {:method :post
                              :uri (str (api-url) "/api/build-orders")
                              :timeout 10000
                              :params params
                              :format (ajax/json-request-format)
                              :response-format (ajax/json-response-format {:keywords? true})
                              :headers (auth-headers db)
                              :on-success [:create-bo-success]
                              :on-failure [:fetch-bo-failure]}}))

(reg-event-db :create-bo-success (fn [db [_ response]]
                                   (let [bo (:body response)]
                                     (-> db
                                         (update :build-orders conj bo)
                                         (assoc :selected-build-order bo)))))

(reg-event-fx :update-build-order-api
              (fn [{:keys [db]} [_ id params]]
                {:http-xhrio {:method :put
                              :uri (str (api-url) "/api/build-orders/" id)
                              :timeout 10000
                              :params params
                              :format (ajax/json-request-format)
                              :response-format (ajax/json-response-format {:keywords? true})
                              :headers (auth-headers db)
                              :on-success [:update-bo-api-success]
                              :on-failure [:fetch-bo-failure]}}))

(reg-event-db :update-bo-api-success (fn [db [_ response]]
                                       (let [bo (:body response)]
                                         (-> db
                                             (update :build-orders (fn [orders]
                                                                     (mapv #(if (= (:id %) (:id bo)) bo %) orders)))
                                             (assoc :selected-build-order bo)
                                             (assoc :edit-bo-id nil)))))

(reg-event-db :set-edit-bo-id (fn [db [_ bo-id]]
                                (assoc db :edit-bo-id bo-id)))

(reg-event-fx :delete-build-order
              (fn [{:keys [db]} [_ id]]
                {:http-xhrio {:method :delete
                              :uri (str (api-url) "/api/build-orders/" id)
                              :timeout 10000
                              :format (ajax/json-request-format)
                              :response-format (ajax/json-response-format {:keywords? true})
                              :headers (auth-headers db)
                              :on-success [:delete-bo-success id]
                              :on-failure [:fetch-bo-failure]}}))

(reg-event-db :delete-bo-success (fn [db [_ _ bo-id]]
                                   (let [orders (:build-orders db)
                                         updated (remove #(= (:id %) bo-id) orders)]
                                     (-> db
                                         (assoc :build-orders updated)
                                         (assoc :selected-build-order nil)))))

;; -- Upload Modal ---------------------------------------------------

(reg-event-db :open-upload-modal (fn [db _]
                                   (assoc db :upload-modal-open? true)))

(reg-event-db :close-upload-modal (fn [db _]
                                    (assoc db :upload-modal-open? false)))

(reg-event-db :set-replay-uploading (fn [db [_ val]]
                                      (assoc db :replay-uploading? val)))

(reg-event-fx :upload-replay
              (fn [{:keys [db]} [_ file bo-meta]]
                (println "File: " file)
                (println "BOMeta: " bo-meta)
                (let [form-data (js/FormData.)]
                  (.append form-data "file" file)
                  (doseq [[k v] bo-meta]
                    (.append form-data (name k) (str v)))
                  {:http-xhrio {:method :post
                                :uri (str (api-url) "/api/replay/upload")
                                :timeout 120000
                                :params form-data
                                :response-format (ajax/json-response-format {:keywords? true})
                                :headers (assoc (auth-headers db) "Content-Type" nil)
                                :on-success [:upload-replay-success]
                                :on-failure [:upload-replay-failure]}})))

(reg-event-db :upload-replay-success (fn [db [_ response]]
                                       (let [bo (:body response)]
                                         (-> db
                                             (assoc :upload-modal-open? false)
                                             (update :build-orders conj bo)
                                             (assoc :selected-build-order bo)))))

(reg-event-db :upload-replay-failure (fn [db [_ _]]
                                       (assoc db :replay-uploading? false)))

(reg-event-db :set-upload-name (fn [db [_ val]] (assoc db :upload-name val)))
(reg-event-db :set-upload-author (fn [db [_ val]] (assoc db :upload-author val)))
(reg-event-db :set-upload-playstyle (fn [db [_ val]] (assoc db :upload-play_style val)))
(reg-event-db :set-upload-strategic-goals (fn [db [_ val]] (assoc db :upload-strategic_goals val)))
(reg-event-db :set-upload-counters (fn [db [_ val]] (assoc db :upload-counters val)))
(reg-event-db :set-upload-weaknesses (fn [db [_ val]] (assoc db :upload-weaknesses val)))
(reg-event-db :set-upload-transition-plan (fn [db [_ val]] (assoc db :upload-transition_plan val)))
(reg-event-db :set-upload-youtube-url (fn [db [_ val]] (assoc db :upload-youtube_url val)))
(reg-event-db :set-upload-error (fn [db [_ val]] (assoc db :upload-error val)))

(reg-event-db :reset-upload-form (fn [db _]
                                   (-> db
                                       (assoc :upload-name "")
                                       (assoc :upload-author "")
                                       (assoc :upload-play_style "")
                                       (assoc :upload-strategic_goals "")
                                       (assoc :upload-counters "")
                                       (assoc :upload-weaknesses "")
                                       (assoc :upload-transition_plan "")
                                       (assoc :upload-youtube_url "")
                                       (assoc :upload-error nil))))
