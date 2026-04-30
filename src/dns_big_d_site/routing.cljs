(ns dns-big-d-site.routing
  (:require [bidi.bidi :as bidi]
            [re-frame.core :as rf]))

(def app-routes
  ["/" {"" :home
        "build-orders" :build-orders-list
        ["build-orders/" :id] :build-order-detail}])

(defn- navigate-to [path]
  (js/history.pushState nil "" path))

(rf/reg-event-fx :navigate
  (fn [{:keys [db]} [_ route]]
    (cond-> {:dispatch [:set-current-route route]
             :db (assoc db :current-route route)}
      (= route :build-orders-list)
      (assoc :dispatch [:fetch-build-orders]))))

(rf/reg-event-db :set-current-route
                 (fn [db [_ route]]
                   (assoc db :current-route route)))

(defn match-route []
  (let [path (or (.. js/location -pathname) "/")
        result (bidi/match-route app-routes path)]
    (if result
      (:template result)
      :home)))

(defn- handle-pop-state [e]
  (let [route (match-route)]
    (rf/dispatch-sync [:navigate route])))

(defn init-routing []
  (.addEventListener js/window "popstate" handle-pop-state)
  (let [route (match-route)]
    (rf/dispatch-sync [:navigate route])))
