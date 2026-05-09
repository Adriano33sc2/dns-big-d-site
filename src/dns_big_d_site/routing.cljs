(ns dns-big-d-site.routing
  (:require [bidi.bidi :as bidi]
            [re-frame.core :as rf]))

(def app-routes
  ["/" {"" :home
        "build-orders" :build-orders-list
        ["build-orders/" :id] :build-order-detail}])

(rf/reg-event-fx
 :navigate
 (fn [{:keys [db]} [_ route-map]]
   (let [route (:route route-map)
         id    (:id route-map)
         db'   (assoc db :current-route route :current-route-id id)]
     (cond
       (= route :build-orders-list)
       {:db db' :dispatch [:fetch-build-orders]}

       (= route :build-order-detail)
       {:db db' :dispatch [:fetch-build-order id]}

       :else
       {:db db'}))))

(defn match-route []
  (let [path   (or (.-pathname js/location) "/")
        result (bidi/match-route app-routes path)]
    (if result
      {:route (:template result)
       :id    (get-in result [:route-params :id])}
      {:route :home :id nil})))

(defn- handle-pop-state [_]
  (let [{:keys [route id]} (match-route)]
    (rf/dispatch-sync [:navigate {:route route :id id}])))

(defn init-routing []
  (.addEventListener js/window "popstate" handle-pop-state)
  (let [{:keys [route id]} (match-route)]
    (rf/dispatch-sync [:navigate {:route route :id id}])))
