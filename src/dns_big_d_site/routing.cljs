(ns dns-big-d-site.routing
  (:require [bidi.bidi :as bidi]
            [re-frame.core :as rf]))

(def app-routes
  ["/" {"" :home
        "build-orders" :build-orders-list
        ["build-orders/" :id] :build-order-detail}])

(defn- navigate-to [path]
  (js/history.pushState nil "" path))

(rf/reg-event-fx :dns-big-d-site.routing/navigate
  (fn [_ [_ route]]
    {:dispatch [:dns-big-d-site.routing/set-current-route route]
     :dispatch-v (case route
                   :build-orders-list [:dns-big-d-site.core/fetch-build-orders]
                   :build-order-detail nil)}))

(rf/reg-event-db :dns-big-d-site.routing/set-current-route
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
    (rf/dispatch-sync [:dns-big-d-site.routing/navigate route])))

(defn init-routing []
  (.addEventListener js/window "popstate" handle-pop-state)
  (let [route (match-route)]
    (rf/dispatch-sync [:dns-big-d-site.routing/navigate route])))
