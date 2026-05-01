(ns dns-big-d-site.core
  (:require
    [day8.re-frame.http-fx]
    [dns-big-d-site.core.events]
    [dns-big-d-site.core.subs]
    [dns-big-d-site.routing :as routing]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [reagent.dom.client :as rdc]))

;; ═══════════════════════════════════════════════════════════════════
;; I18N — Translation dictionaries
;; ═══════════════════════════════════════════════════════════════════

(def translations
  {:en
   {;; Nav
    :nav/programs "Programs"
    :nav/community "Community"
    :nav/youtube "YouTube"
    :nav/build-orders "Build Orders"

    ;; Hero
    :hero/eyebrow "▸ StarCraft II Elite Coaching"
    :hero/title-1 "Climb the"
    :hero/title-2 "Ladder with"
    :hero/sub "Personalized strategy, replays breakdowns, and the community you need to break through your rank ceiling — for all races and skill levels."
    :hero/cta-book "Book a Session"
    :hero/cta-youtube "Watch on YouTube"

    ;; Cards section
    :cards/title "Choose Your Path"
    :cards/subtitle "From live coaching to free guides — everything you need to dominate the ladder."

    ;; Card: Coaching
    :card.coaching/label "Coaching"
    :card.coaching/title "1-on-1 Replay Analysis"
    :card.coaching/desc "Get your replays reviewed by DnS. We'll identify the exact mistakes holding you back and build a personalised roadmap to climb the ladder."
    :card.coaching/f1 "Replay analysis"
    :card.coaching/f2 "Personalised builds that fit you"
    :card.coaching/f3 "Live Coaching while playing"
    :card.coaching/f4 "Mindset fixing: ladder anxiety/tilting"
    :card.coaching/btn "▸ Book a Session"

    ;; Card: Discord
    :card.discord/label "Community"
    :card.discord/title "Discord Server"
    :card.discord/desc "Join a thriving community of StarCraft II players. Share replays, ask questions, find practice partners and get weekly tips from DnS himself."
    :card.discord/f1 "Replay review channels"
    :card.discord/f2 "Group Coaching sessions"
    :card.discord/f3 "Advice to improve"
    :card.discord/btn "▸ Join the Discord"

    ;; Card: YouTube
    :card.youtube/label "Free"
    :card.youtube/title "YouTube Channel"
    :card.youtube/desc "Free educational content — build orders, masterclasses, pro games broken down by DnS and much, much more."
    :card.youtube/f1 "Weekly in-depth guides"
    :card.youtube/f2 "Masterclasses — A wiser approach to SC2"
    :card.youtube/f3 "Beginner to high level content"
    :card.youtube/btn "▸ Watch Now"

    ;; Stats
    :stat/gm "10+"
    :stat/gm-label "Years of professional Gaming"
    :stat/years "5+"
    :stat/years-label "Years Coaching"
    :stat/games "100+"
    :stat/games-label "student replays analyzed"

    ;; Testimonials
    :testimonial.1/quote "DnS helped me break out of Diamond and into Master's for the first time in my life, and I am 38 years old!!"
    :testimonial.1/author "— icemanmelting, Diamond → Master"
    :testimonial.2/quote "I really learned a lot and I know i'll keep on going later to potentially try the GM rank. If you play protoss and want to improve, DnS knows how to listen and adapt to anybody's level.."
    :testimonial.2/author "— Caporal-chef d'Umbrella, Gold → Diamond"
    :testimonial.3/quote "The feedback was clear, practical, and immediately applicable in ladder games. Overall, it was a very high-level, efficient coaching experience that provided real value. I would definitely recommend it."
    :testimonial.3/author "— GlhfGG, Master 3 → Master 1"

    ;; Modal
    :modal/title-1 "Choose Your "
    :modal/title-2 "Path"
    :modal/subtitle "Personalized coaching sessions with replay analysis, custom build orders, mindset training, and everything you need to dominate the ladder."
    :modal/single-name "Single Session"
    :modal/single-price "$40"
    :modal/single-detail "per hour"
    :modal/single-f1 "In-depth replay analysis"
    :modal/single-f2 "Personalized build orders"
    :modal/single-f3 "Strategic decision-making"
    :modal/single-f4 "Mental game mastery"
    :modal/single-f5 "Progress tracking system"
    :modal/single-btn "Book a Session"

    ;; Footer
    :footer/copy "© 2026 DnS SC2 Coaching · All Rights Reserved"

    ;; Login
    :login/title "Backoffice Access"
    :login/username "Username"
    :login/username-ph "Enter your username"
    :login/password "Password"
    :login/password-ph "Enter your password"
    :login/remember "Remember me"
    :login/forgot "Forgot password?"
    :login/signin "Sign In"
    :login/hint "Authorized personnel only · All access is logged"}

   :fr
   {;; Nav
    :nav/programs "Programmes"
    :nav/community "Communauté"
    :nav/youtube "YouTube"
    :nav/build-orders "Build Orders"

    ;; Hero
    :hero/eyebrow "▸ Coaching d'élite StarCraft II"
    :hero/title-1 "Grimpez le"
    :hero/title-2 "Ladder avec"
    :hero/sub "Stratégie personnalisée, analyses de replays et la communauté qu'il vous faut pour passer au niveau supérieur — toutes races, tous niveaux."
    :hero/cta-book "Réserver une Session"
    :hero/cta-youtube "Voir sur YouTube"

    ;; Divider
    :divider/label "TOUTES LES RACES · TOUTES LES LIGUES · UN COACH"

    ;; Cards section
    :cards/title "Choisissez Votre Voie"
    :cards/subtitle "Du coaching en direct aux guides gratuits — tout ce qu'il faut pour dominer le ladder."

    ;; Card: Coaching
    :card.coaching/label "Coaching"
    :card.coaching/title "Analyse de Replay 1-on-1"
    :card.coaching/desc "Faites analyser vos replays par DnS. On identifiera les erreurs exactes qui vous freinent et on construira une feuille de route personnalisée pour progresser."
    :card.coaching/f1 "Analyse de replays"
    :card.coaching/f2 "Builds personnalisés pour vous"
    :card.coaching/f3 "Coaching en direct pendant vos parties"
    :card.coaching/f4 "Travail de gestion des émotions"
    :card.coaching/btn "▸ Réserver une Session"

    ;; Card: Discord
    :card.discord/label "Communauté"
    :card.discord/title "Serveur Discord"
    :card.discord/desc "Rejoignez une communauté active de joueurs StarCraft II. Partagez vos replays, posez vos questions, trouvez des partenaires d'entraînement et recevez des conseils hebdomadaires de DnS."
    :card.discord/f1 "Onglet dédiés aux replays"
    :card.discord/f2 "Sessions de coaching en groupe"
    :card.discord/f3 "Conseils pour progresser"
    :card.discord/btn "▸ Rejoindre le Discord"

    ;; Card: YouTube
    :card.youtube/label "Gratuit"
    :card.youtube/title "Chaîne YouTube"
    :card.youtube/desc "Contenu éducatif gratuit — build orders, masterclasses, parties pro décortiquées par DnS et bien plus encore."
    :card.youtube/f1 "Guides approfondis chaque semaine"
    :card.youtube/f2 "Masterclasses — Une approche plus sage de SC2"
    :card.youtube/f3 "Contenu adapté à tous les niveaux"
    :card.youtube/btn "▸ Regarder"

    ;; Stats
    :stat/gm "10+"
    :stat/gm-label "Ans de très haut niveau"
    :stat/years "5+"
    :stat/years-label "Ans de Coaching"
    :stat/games "100+"
    :stat/games-label "Replays d'élèves analysés"

    ;; Testimonials
    :testimonial.1/quote "DnS m'a aidé à sortir du Diamant après deux ans de stagnation. Les analyses de replays sont incroyablement détaillées — il voit des choses que je n'aurais jamais remarquées seul."
    :testimonial.1/author "— Icemanmelting, Diamant → Maître"
    :testimonial.2/quote "J'ai vraiment beaucoup appris et je sais que je continuerai par la suite avec lui afin potentiellement tenter le rang GM. Si tu joues protoss et que tu veux évoluer DNS te sera d'une grande aide, car il sait écouter et s'adapter au niveau de chacun."
    :testimonial.2/author "— Caporal-chef d'Umbrella, Or → Diamant"
    :testimonial.3/quote "Les retours étaient clairs, pratiques et applicables immédiatement en ladder. Globalement, c'était du coaching de très haut niveau, efficace qui m'a apporté de la vraie valeur. Je recommande vivement."
    :testimonial.3/author "— CJ, Master 3 → Master 1"

    ;; Modal
    :modal/title-1 "Choisissez Votre "
    :modal/title-2 "Voie"
    :modal/subtitle "Sessions de coaching personnalisées avec analyse de replays, builds sur mesure, entraînement mental et tout ce qu'il faut pour dominer le ladder."
    :modal/single-name "Session Unique"
    :modal/single-price "40 $"
    :modal/single-detail "par heure"
    :modal/single-f1 "Analyse approfondie de replays"
    :modal/single-f2 "Build orders personnalisés"
    :modal/single-f3 "Prise de décision stratégique"
    :modal/single-f4 "Coaching mental"
    :modal/single-f5 "Plan d'amélioration sur mesure"
    :modal/single-f6 "Notes post-session"
    :modal/single-btn "Réserver une Session"
    :modal/pack-badge "Le Plus Populaire"
    :modal/pack-name "Pack Puissance"
    :modal/pack-price "150 $"
    :modal/pack-detail "5 heures de coaching"
    :modal/pack-detail2 "Réparties selon vos besoins"
    :modal/pack-save "Économisez 50 $"
    :modal/pack-f1 "Planning flexible — utilisez quand vous voulez"
    :modal/pack-f2 "Analyses de replays avancées"
    :modal/pack-f3 "Optimisation de plusieurs build orders"
    :modal/pack-f4 "Maîtrise du mental"
    :modal/pack-f5 "Système de suivi de progression"
    :modal/pack-btn "Commencer"

    ;; Footer
    :footer/copy "© 2026 DnS SC2 Coaching · Tous Droits Réservés"

    ;; Login
    :login/title "Accès Backoffice"
    :login/username "Nom d'utilisateur"
    :login/username-ph "Entrez votre nom d'utilisateur"
    :login/password "Mot de passe"
    :login/password-ph "Entrez votre mot de passe"
    :login/remember "Se souvenir de moi"
    :login/forgot "Mot de passe oublié?"
    :login/signin "Se connecter"
    :login/hint "Personnel autorisé uniquement · Tous les accès sont enregistrés"}})

;; ═══════════════════════════════════════════════════════════════════
;; I18N HELPER
;; ═══════════════════════════════════════════════════════════════════

(defn t
  "Translate a key using the current language."
  [k]
  (let [lang @(rf/subscribe [:lang])]
    (get-in translations [lang k] (str k))))

;; ═══════════════════════════════════════════════════════════════════
;; VIEWS
;; ═══════════════════════════════════════════════════════════════════

;; ─── Language Switcher ──────────────────────────────────────────

(defn lang-switcher []
  (let [lang @(rf/subscribe [:lang])]
    [:div.lang-switcher
     [:button.lang-btn
      {:class (when (= lang :en) "lang-active")
       :on-click #(rf/dispatch [:set-lang :en])}
      "EN"]
     [:span.lang-sep "│"]
     [:button.lang-btn
      {:class (when (= lang :fr) "lang-active")
       :on-click #(rf/dispatch [:set-lang :fr])}
      "FR"]]))

;; ─── Header ─────────────────────────────────────────────────────

(defn header []
  [:header
   [:div.logo
    "DnS"
    [:span "SC2 Coaching"]]
   [:div.header-right
    [:nav
     [:a {:href "#programs"} (t :nav/programs)]
     [:a {:href "#discord"} (t :nav/community)]
     [:a {:href "#youtube"} (t :nav/youtube)]
        [:a {:href "#build-orders" :on-click #(do (.preventDefault %) (js/history.pushState nil "" "/build-orders") (rf/dispatch [:navigate :build-orders-list]))} (t :nav/build-orders)]]
    [lang-switcher]]])

;; ─── Hero ───────────────────────────────────────────────────────

(defn hero []
  [:section.hero
   [:div.hero-orb]
   [:p.hero-eyebrow (t :hero/eyebrow)]
   [:h1 (t :hero/title-1) [:br] (t :hero/title-2) [:br] [:em "DnS"]]
   [:p.hero-sub (t :hero/sub)]
   [:div.hero-cta-row
    [:button.btn.btn-gold
     {:on-click #(rf/dispatch [:open-booking-modal])}
     (t :hero/cta-book)]
    [:a.btn.btn-outline {:href "https://www.youtube.com/@DnS_SC2"
                         :target "_blank"
                         :rel "noopener noreferrer"}
     (t :hero/cta-youtube)]]])

;; ─── Divider ────────────────────────────────────────────────────

(defn divider []
  [:div.divider
   [:span.divider-label (t :divider/label)]])

;; ─── Card ───────────────────────────────────────────────────────

(defn card-component [{:keys [id thumb badge badge-icon label-cls]}]
  [:div.card (when (#{:discord :youtube} id)
               {:id (name id)})
   ;; Thumbnail
   [:div {:class (str "card-thumb " thumb)}
    [:div.thumb-hex]
    [:div.thumb-gfx
     [:div {:class (str "thumb-badge " badge)} badge-icon]]
    [:span {:class (str "card-label " label-cls)}
     (t (keyword (str "card." (name id)) "label"))]]
   ;; Body
   [:div.card-body
    [:p.card-title (t (keyword (str "card." (name id)) "title"))]
    [:p.card-desc (t (keyword (str "card." (name id)) "desc"))]
    [:ul.card-features
     (doall
       (for [i (range 1 10)
             :let [k (keyword (str "card." (name id)) (str "f" i))
                   v (get-in translations [:en k])]
             :when v]
         ^{:key i}
         [:li (t k)]))]]
   ;; Footer
   [:div.card-footer
    (case id
      :coaching [:button.btn.btn-card
                 {:on-click #(rf/dispatch [:open-booking-modal])}
                 (t :card.coaching/btn)]
      :discord [:a.btn.btn-card {:href "https://discord.gg/RnDY9hyKVA"
                                 :target "_blank"
                                 :rel "noopener noreferrer"}
                (t :card.discord/btn)]
      :youtube [:a.btn.btn-card {:href "https://www.youtube.com/@DnS_SC2"
                                 :target "_blank"
                                 :rel "noopener noreferrer"}
                (t :card.youtube/btn)]
      [:a.btn.btn-card {:href "#"}
       (t (keyword (str "card." (name id)) "btn"))])]])

;; ─── Cards Section ──────────────────────────────────────────────

(defn cards-section []
  [:<>
   [:div.section-title {:id "programs"}
    [:h2 (t :cards/title)]
    [:p (t :cards/subtitle)]]
   [:div.cards-grid
    [card-component {:id :coaching :thumb "thumb-terran" :badge "badge-terran" :badge-icon "🎯" :label-cls "label-coaching"}]
    [card-component {:id :discord :thumb "thumb-discord" :badge "badge-discord" :badge-icon "🛡️" :label-cls "label-community"}]
    [card-component {:id :youtube :thumb "thumb-youtube" :badge "badge-yt" :badge-icon "▶" :label-cls "label-free"}]]])

;; ─── Stats Strip ────────────────────────────────────────────────

(defn stats-strip []
  [:div.stats-strip
   (doall
     (for [[num-k lbl-k] [[:stat/gm :stat/gm-label]
                          [:stat/years :stat/years-label]
                          [:stat/games :stat/games-label]]]
       ^{:key lbl-k}
       [:div.stat
        [:span.stat-number (t num-k)]
        [:span.stat-label (t lbl-k)]]))])

;; ─── Testimonials (rotating with fade) ──────────────────────────

(defn testimonial-section []
  (let [timer-ref (r/atom nil)
        start-timer (fn []
                      (when-let [tm @timer-ref]
                        (js/clearInterval tm))
                      (reset! timer-ref
                              (js/setInterval
                                #(rf/dispatch [:next-testimonial])
                                6000)))]
    (r/create-class
      {:component-did-mount
       (fn [_] (start-timer))

       :component-will-unmount
       (fn [_]
         (when-let [tm @timer-ref]
           (js/clearInterval tm)))

       :reagent-render
       (fn []
         (let [active-idx @(rf/subscribe [:active-testimonial])
               items [[1 :testimonial.1/quote :testimonial.1/author]
                      [2 :testimonial.2/quote :testimonial.2/author]
                      [3 :testimonial.3/quote :testimonial.3/author]]]
           [:div.testimonial-section
            [:div.testimonial-carousel
             (doall
               (for [[idx qk ak] items]
                 ^{:key idx}
                 [:div.testimonial-slide
                  {:class (if (= (dec idx) active-idx) "testimonial-active" "testimonial-hidden")}
                  [:blockquote (t qk)]
                  [:p.testimonial-author (t ak)]]))]
            [:div.testimonial-dots
             (doall
               (for [i (range 3)]
                 ^{:key i}
                 [:button.testimonial-dot
                  {:class (when (= i active-idx) "dot-active")
                   :on-click (fn []
                               (rf/dispatch [:set-active-testimonial i])
                               (start-timer))}]))]]))})))

;; ─── Footer ─────────────────────────────────────────────────────

(defn footer-component []
  (let [click-count (r/atom 0)
        timer (r/atom nil)]
    (fn []
      [:footer
       [:div.footer-logo "DnS Coaching"]
       [:p.footer-copy
        {:on-click (fn []
                     (reset! click-count (inc @click-count))
                     (when-let [tm @timer]
                       (js/clearTimeout tm))
                     (reset! timer
                             (js/setTimeout #(reset! click-count 0) 2000))
                     (when (= @click-count 3)
                       (rf/dispatch [:open-login-modal])
                       (reset! click-count 0)))}
        (t :footer/copy)]])))

;; ─── Login Modal ──────────────────────────────────────────────

(defn login-modal []
  (let [open? @(rf/subscribe [:login-modal-open?])
        username @(rf/subscribe [:login-username])
        password @(rf/subscribe [:login-password])
        error @(rf/subscribe [:login-error])]
    (when open?
      [:div.login-overlay
       {:on-click (fn [e]
                    (when (= (.-target e) (.-currentTarget e))
                      (rf/dispatch [:close-login-modal])))}
       [:div.login-content
        [:button.login-close
         {:on-click #(rf/dispatch [:close-login-modal])}
         "✕"]
        [:div.login-logo
         [:span.logo-main "DnS"]
         [:span.logo-sub "BACKOFFICE"]]
        [:h2.login-title (t :login/title)]
        [:form.login-form
         {:on-submit #(do (.preventDefault %)
                          (rf/dispatch [:login {:username username :password password}]))}
         [:div.login-field
          [:label.login-label (t :login/username)]
          [:input.login-input
           {:type "text"
            :id "login-username"
            :value username
            :placeholder (t :login/username-ph)
            :on-change #(rf/dispatch [:set-login-username (-> % .-target .-value)])
            :auto-focus true}]]
         [:div.login-field
          [:label.login-label (t :login/password)]
          [:input.login-input
           {:type "password"
            :id "login-password"
            :value password
            :placeholder (t :login/password-ph)
            :on-change #(rf/dispatch [:set-login-password (-> % .-target .-value)])}]]
         [:div.login-options
          [:label.login-remember
           [:input.login-checkbox {:type "checkbox" :id "remember-me"}]
           [:span (t :login/remember)]]
          [:a.login-forgot {:href "#"} (t :login/forgot)]]
         (when error
           [:div.login-error-msg error])
         [:button.login-submit
          {:type "submit"}
          (t :login/signin)]]
        [:div.login-divider]
        [:p.login-hint (t :login/hint)]]])))

;; ─── Booking Modal ──────────────────────────────────────────────

(defn booking-modal []
  (let [open? @(rf/subscribe [:booking-modal-open?])]
    (when open?
      [:div.modal-overlay
       {:on-click (fn [e]
                    (when (= (.-target e) (.-currentTarget e))
                      (rf/dispatch [:close-booking-modal])))}
       [:div.modal-content
        [:button.modal-close
         {:on-click #(rf/dispatch [:close-booking-modal])}
         "✕"]
        [:h2.modal-title (t :modal/title-1) [:em (t :modal/title-2)]]
        [:p.modal-subtitle (t :modal/subtitle)]
        [:div.pricing-grid
         ;; Single Session
         [:div.pricing-card
          [:p.pricing-name (t :modal/single-name)]
          [:p.pricing-price (t :modal/single-price)]
          [:p.pricing-detail (t :modal/single-detail)]
          [:ul.pricing-features
           [:li (t :modal/single-f1)]
           [:li (t :modal/single-f2)]
           [:li (t :modal/single-f3)]
           [:li (t :modal/single-f4)]
           [:li (t :modal/single-f5)]
           [:li (t :modal/single-f6)]]
          [:a.btn.btn-card.pricing-btn
           {:href "https://buy.stripe.com/eVq8wIcZLcyNbfMdDn63K00"
            :target "_blank"
            :rel "noopener noreferrer"}
           (t :modal/single-btn)]]
         ;; Power Package
         [:div.pricing-card.pricing-featured
          [:span.pricing-badge (t :modal/pack-badge)]
          [:p.pricing-name (t :modal/pack-name)]
          [:p.pricing-price.pricing-price-gold (t :modal/pack-price)]
          [:p.pricing-detail (t :modal/pack-detail)]
          [:p.pricing-detail-sub (t :modal/pack-detail2)]
          [:p.pricing-save (t :modal/pack-save)]
          [:ul.pricing-features
           [:li (t :modal/pack-f1)]
           [:li (t :modal/pack-f2)]
           [:li (t :modal/pack-f3)]
           [:li (t :modal/pack-f4)]
           [:li (t :modal/pack-f5)]]
          [:a.btn.btn-gold.pricing-btn
           {:href "https://buy.stripe.com/cNicMY4tf42hgA68j363K01"
            :target "_blank"
            :rel "noopener noreferrer"}
           (t :modal/pack-btn)]]]]])))

;; ═══════════════════════════════════════════════════════════════════
;; BUILD ORDER COMPONENTS
;; ═══════════════════════════════════════════════════════════════════

(def SC2-ICONS
  {"Pylon" "pylon.jpg"
   "Gateway" "gateway.jpg"
   "Assimilator" "assimilator.jpg"
   "Nexus" "nexus.jpg"
   "Zealot" "zealot.jpg"
   "CyberneticsCore" "cybernetics_core.jpg"
   "WarpGate" "warp_gate.jpg"
   "Sentry" "sentry.jpg"
   "Stalker" "stalker.jpg"
   "Forge" "forge.jpg"
   "ShieldBattery" "shield_battery.jpg"
   "TwilightCouncil" "twilight_council.jpg"
   "WarpPrism" "warp_prism.jpg"
   "Adept" "adept.jpg"
   "Observer" "observer.jpg"
   "RoboticsFacility" "robotics_facility.jpg"
   "RoboticsBay" "robotics_bay.jpg"
   "Stargate" "stargate.jpg"
   "Phoenix" "phoenix.jpg"
   "Blink" "blink.jpg"
   "TemplarArchive" "templar_archive.jpg"
   "DarkShrine" "dark_shrine.jpg"
   "PhotonCannon" "photon_cannon.jpg"
   "Oracle" "oracle.jpg"
   "FleetBeacon" "fleet_beacon.jpg"
   "VoidRay" "void_ray.jpg"
   "Immortal" "immortal.jpg"
   "Colossus" "colossus.jpg"
   "Mothership" "mothership.jpg"
   "Carrier" "carrier.jpg"
   "Disruptor" "disruptor.jpg"
   "Tempest" "tempest.jpg"
   "Archon" "archon.jpg"
   "HighTemplar" "high_templar.jpg"
   "Zealot2" "zealot.jpg"})

(defn- get-icon-path [action-name]
  (let [normalized (-> action-name
                       clojure.string/lower-case
                       (clojure.string/replace #" " "-")
                       (clojure.string/replace #"[^a-z0-9-]" ""))]
    (str "/img/" normalized ".jpg")))

(defn- action-icon [action-name]
  [:div.action-icon-wrapper
   {:class "action-icon"}
   [:img.action-img {:src (get-icon-path action-name)
                     :alt action-name
                     :on-error #(set! (.. % -target -src) "/img/placeholder.jpg")}]
   [:span.action-initial (first (clojure.string/upper-case action-name))]])

(defn- format-time [seconds]
  (let [mins (quot seconds 60)
        secs (mod seconds 60)]
    (str mins ":" (if (< secs 10) "0" "") secs)))

(defn- parse-time-to-seconds [time-str]
  (let [parts (clojure.string/split time-str #":")]
    (if (= (count parts) 2)
      (let [[mins secs] parts
            mins-int (parse-long mins)
            secs-int (parse-long secs)]
        (+ (* mins-int 60) secs-int))
      0)))

(defn- info-card [{:keys [label value]}]
  [:div.info-card
   [:span.info-card-label label]
   [:p.info-card-value (or value "—")]])

(defn- build-order-list []
  (fn []
    (let [orders @(rf/subscribe [:build-orders])
          logged-in? @(rf/subscribe [:is-logged-in?])]
      [:div
       [:div.bo-page-header
        [:h1.page-title "Build Orders"]
        (when logged-in?
          [:button.btn.btn-gold.bo-upload-btn
           {:on-click #(rf/dispatch [:open-upload-modal])}
           "Upload Replay"])]
       [:div.bo-grid
        (if (empty? orders)
          [:div.bo-empty "No build orders yet."]
          (doall
            (for [bo orders]
              ^{:key (:id bo)}
              [:div.bo-card
               {:on-click #(js/window.location.assign (str "/build-orders/" (:id bo)))}
               [:div.bo-card-race
                (when (:play_style bo)
                  [:span.race-badge (:play_style bo)])]
               (when logged-in?
                 [:div.bo-card-delete
                  {:on-click #(do (.stopPropagation %)
                                  (rf/dispatch [:set-delete-confirm {:type :bo :id (:id bo)}]))}
                  "🗑"])
               [:h3.bo-card-title (:name bo)]
               [:p.bo-card-author "by " (:author bo)]])))]
       ]
      ;; 1. Open the header vector


      ;; 4. Now start the grid vector separately
      )))


(defn- build-order-detail []
  (fn []
    (let [bo @(rf/subscribe [:selected-build-order])
          logged-in? @(rf/subscribe [:is-logged-in?])
          edit-bo-id @(rf/subscribe [:edit-bo-id])]
      (if-not bo
        [:div.bo-page-header
         [:h1.page-title "Build Order Not Found"]]
        [:<>
         [:div.bo-detail-header
          (if (= (:id bo) edit-bo-id)
            [:<>
             [:input.bo-edit-input {:value (:name bo)
                                    :on-change #(rf/dispatch [:update-selected-bo-field :name (-> % .-target .-value)])}]]
            [:h1.page-title (:name bo)])
          (when logged-in?
            [:div.bo-detail-actions
             [:button.btn.btn-outline {:on-click #(js/window.location.assign "/build-orders")} "← Back to List"]
             (if (= (:id bo) edit-bo-id)
               [:<>
                [:button.btn.btn-gold {:on-click #(rf/dispatch [:update-build-order-api (:id bo)
                                                                       {:name (:name bo)
                                                                        :author (:author bo)
                                                                        :play_style (:play_style bo)
                                                                        :youtube_url (:youtube_url bo)
                                                                        :strategic_goals (:strategic_goals bo)
                                                                        :counters (:counters bo)
                                                                        :weaknesses (:weaknesses bo)
                                                                        :transition_plan (:transition_plan bo)}])} "Save"]
                [:button.btn.btn-outline {:on-click #(rf/dispatch [:set-edit-bo-id nil])} "Cancel"]]
              [:button.btn.btn-outline {:on-click #(rf/dispatch [:set-edit-bo-id (:id bo)])} "Edit"])])]
         (if (= (:id bo) edit-bo-id)
           [:input.bo-edit-input {:value (:author bo)
                                  :on-change #(rf/dispatch [:update-selected-bo-field :author (-> % .-target .-value)])}]
           [:p.bo-detail-author "by " (:author bo)])
         (if (= (:id bo) edit-bo-id)
           [:<>
            [:div.bo-info-row
             [:div.info-card-edit
              [:span.info-card-label "Play Style"]
              [:input.bo-edit-input {:value (:play_style bo) :placeholder "e.g. Protoss"
                                     :on-change #(rf/dispatch [:update-selected-bo-field :play_style (-> % .-target .-value)])}]]
             [:div.info-card-edit
              [:span.info-card-label "Strategic Goals"]
              [:textarea.bo-edit-input {:value (:strategic_goals bo) :placeholder "Main goals of this build"
                                        :on-change #(rf/dispatch [:update-selected-bo-field :strategic_goals (-> % .-target .-value)])}]]
             [:div.info-card-edit
              [:span.info-card-label "Counters"]
              [:textarea.bo-edit-input {:value (:counters bo) :placeholder "What this build counters"
                                        :on-change #(rf/dispatch [:update-selected-bo-field :counters (-> % .-target .-value)])}]]
             [:div.info-card-edit
              [:span.info-card-label "Weaknesses"]
              [:textarea.bo-edit-input {:value (:weaknesses bo) :placeholder "Build weaknesses"
                                        :on-change #(rf/dispatch [:update-selected-bo-field :weaknesses (-> % .-target .-value)])}]]
             [:div.info-card-edit
              [:span.info-card-label "Transition Plan"]
              [:textarea.bo-edit-input {:value (:transition_plan bo) :placeholder "What to do if build doesn't win"
                                        :on-change #(rf/dispatch [:update-selected-bo-field :transition_plan (-> % .-target .-value)])}]]]
            [:div.bo-info-row
             [info-card {:label "Strategic Goals" :value (:strategic_goals bo)}]
             [info-card {:label "Counters" :value (:counters bo)}]
             [info-card {:label "Weaknesses" :value (:weaknesses bo)}]
             [info-card {:label "Transition Plan" :value (:transition_plan bo)}]]])
         (if (= (:id bo) edit-bo-id)
           [:div.info-card-edit
            [:span.info-card-label "YouTube URL"]
            [:input.bo-edit-input {:value (:youtube_url bo) :placeholder "https://www.youtube.com/watch?v=..."
                                   :on-change #(rf/dispatch [:update-selected-bo-field :youtube_url (-> % .-target .-value)])}]]
           [:<>
            (when (:youtube_url bo)
              [:div.bo-video-section
               [:h2.section-title "Video"]
               (let [video-id (when (:youtube_url bo)
                                (let [url (:youtube_url bo)]
                                  (if-let [[_ id] (re-matches #".*(?:v=|youtu\.be/)([a-zA-Z0-9_-]{11}).*" url)]
                                    id
                                    nil)))]
                 (if video-id
                   [:iframe.youtube-embed
                    {:src (str "https://www.youtube.com/embed/" video-id)
                     :frameborder "0"
                     :allow "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                     :allowfullscreen true}]))])])
         [:div.bo-steps-section
          [:h2.section-title "Build Steps"]
          (when logged-in?
            [:button.btn.btn-outline {:on-click #(rf/dispatch [:add-step-api (:id bo)
                                                                               {:supply 0 :time-seconds 0 :action-name "" :notes "" :sort-order (count (:steps bo))}])}
              "+ Add Step"])
          [:div.bo-steps-table
           [:div.bo-step-row.bo-step-header
            [:div.bo-col.bo-col-supply "Supply"]
            [:div.bo-col.bo-col-time "Time"]
            [:div.bo-col.bo-col-action "Action"]
            [:div.bo-col.bo-col-notes "Notes"]
            (when logged-in? [:div.bo-col.bo-col-actions ""])]]
          (doall
            (for [step (:steps bo)
                  :let [edit-mode? (and logged-in? (= (:edit-step-id bo) (:id step)))]]
                  ^{:key (:id step)}
                  [:div.bo-step-row
                   {:class (when edit-mode? "step-editing")}
                   ;; Supply column
                   [:div.bo-col.bo-col-supply
                    (if edit-mode?
                      [:input.step-input {:type "number" :value (:supply step)
                                          :on-change #(rf/dispatch [:update-step-in-selected-bo (:id step) (assoc step :supply (parse-long (-> % .-target .-value )))])}]
                      [:span (:supply step)])]
                   ;; Time column
                   [:div.bo-col.bo-col-time
                    (if edit-mode?
                      [:input.step-input {:type "text" :value (format-time (:time-seconds step))
                                          :on-change #(rf/dispatch [:update-step-in-selected-bo (:id step) (assoc step :time-seconds (parse-time-to-seconds (-> % .-target .-value)))])}]
                      [:span (format-time (:time-seconds step))])]
                   ;; Action column
                   [:div.bo-col.bo-col-action
                    [action-icon (:action_name step)]
                    (if edit-mode?
                      [:input.step-input {:value (:action_name step)
                                          :on-change #(rf/dispatch [:update-step-in-selected-bo (:id step) (assoc step :action_name (-> % .-target .-value))])}]
                      [:span.action-text (:action_name step)])]
                   ;; Notes column
                   [:div.bo-col.bo-col-notes
                    (if edit-mode?
                      [:input.step-input {:value (:notes step) :placeholder "e.g. chronoboost"
                                          :on-change #(rf/dispatch [:update-step-in-selected-bo (:id step) (assoc step :notes (-> % .-target .-value))])}]
                      [:span (:notes step)])]
                   ;; Actions column (logged in)
                   (when logged-in?
                     [:div.bo-col.bo-col-actions
                      (if edit-mode?
                        [:<>
                         [:button.btn-step.save {:on-click #(do (rf/dispatch [:update-step-api (:id step)
                                                                                                              (dissoc step :edit-step-id)])
                                                                (rf/dispatch [:update-selected-bo-field :edit-step-id nil]))} "Save"]
                         [:button.btn-step.cancel {:on-click #(rf/dispatch [:update-selected-bo-field :edit-step-id nil])} "Cancel"]]
                        [:<>
                         [:button.btn-step.edit {:on-click #(rf/dispatch [:update-selected-bo-field :edit-step-id (:id step)])} "Edit"]
                         [:button.btn-step.delete {:on-click #(rf/dispatch [:set-delete-confirm {:type :step :id (:id step)}])} "Delete"]])])]))
          (when logged-in?
            [:button.btn.btn-outline {:on-click #(rf/dispatch [:add-step-api (:id bo)
                                                                                           {:supply 0 :time-seconds 0 :action-name "" :notes "" :sort-order (count (:steps bo))}])}
             "+ Add Step"])]]))))

(defn- replay-upload-modal []
  (let [open? @(rf/subscribe [:upload-modal-open?])
        uploading? @(rf/subscribe [:replay-uploading?])
        error @(rf/subscribe [:upload-error])]
    (when open?
      [:div.upload-overlay
       {:on-click #(rf/dispatch [:close-upload-modal])}
       [:div.upload-content
        {:on-click #(.stopPropagation %)}
        [:button.upload-close {:on-click #(rf/dispatch [:close-upload-modal])} "✕"]
        [:h2.upload-title "Upload Replay"]
        [:p.upload-subtitle "Upload a .SC2Replay file and we'll extract the build order automatically."]
        [:form.upload-form
         {:on-submit #(do (.preventDefault %)
                          (let [target (-> % .-target)
                                file-input (aget target "file")
                                 files (. file-input -files)
                                 file (aget files 0)]
                            (if-not file
                              (rf/dispatch [:set-upload-error "Please select a replay file"])
                              (do
                                (rf/dispatch [:set-replay-uploading true])
                                (rf/dispatch [:set-upload-error nil])
                                (let [bo-meta {:name @(rf/subscribe [:upload-name])
                                               :author @(rf/subscribe [:upload-author])
                                               :play_style @(rf/subscribe [:upload-playstyle])
                                               :strategic_goals @(rf/subscribe [:upload-strategic-goals])
                                               :counters @(rf/subscribe [:upload-counters])
                                               :weaknesses @(rf/subscribe [:upload-weaknesses])
                                               :transition_plan @(rf/subscribe [:upload-transition-plan])
                                               :youtube_url @(rf/subscribe [:upload-youtube-url])}]
                                  (rf/dispatch [:upload-replay file bo-meta]))))))}
         [:div.upload-field
          [:label.upload-label "Replay File (.SC2Replay)"]
          [:input.upload-file {:type "file" :id "file" :name "file" :accept ".SC2Replay"}]]
         [:div.upload-field
          [:label.upload-label "Build Order Name"]
          [:input.upload-input {:type "text" :id "bo-name" :placeholder "e.g. 14 Pylon Expand"
                                :value @(rf/subscribe [:upload-name])
                                :on-change #(rf/dispatch [:set-upload-name (-> % .-target .-value)])}]]
         [:div.upload-field
          [:label.upload-label "Author"]
          [:input.upload-input {:type "text" :id "bo-author" :placeholder "Author name"
                                :value @(rf/subscribe [:upload-author])
                                :on-change #(rf/dispatch [:set-upload-author (-> % .-target .-value)])}]]
         [:div.upload-field
          [:label.upload-label "Play Style"]
          [:input.upload-input {:type "text" :id "bo-playstyle" :placeholder "e.g. Protoss, Terran, Zerg"
                                :value @(rf/subscribe [:upload-playstyle])
                                :on-change #(rf/dispatch [:set-upload-playstyle (-> % .-target .-value)])}]]
         [:div.upload-field
          [:label.upload-label "Strategic Goals"]
          [:textarea.upload-input {:id "bo-strategic-goals" :placeholder "Main goals of this build"
                                   :value @(rf/subscribe [:upload-strategic-goals])
                                   :on-change #(rf/dispatch [:set-upload-strategic-goals (-> % .-target .-value)])}]]
         [:div.upload-field
          [:label.upload-label "Counters"]
          [:textarea.upload-input {:id "bo-counters" :placeholder "What this build counters"
                                   :value @(rf/subscribe [:upload-counters])
                                   :on-change #(rf/dispatch [:set-upload-counters (-> % .-target .-value)])}]]
         [:div.upload-field
          [:label.upload-label "Weaknesses"]
          [:textarea.upload-input {:id "bo-weaknesses" :placeholder "Build weaknesses"
                                   :value @(rf/subscribe [:upload-weaknesses])
                                   :on-change #(rf/dispatch [:set-upload-weaknesses (-> % .-target .-value)])}]]
         [:div.upload-field
          [:label.upload-label "Transition Plan"]
          [:textarea.upload-input {:id "bo-transition-plan" :placeholder "What to do if build doesn't win"
                                   :value @(rf/subscribe [:upload-transition-plan])
                                   :on-change #(rf/dispatch [:set-upload-transition-plan (-> % .-target .-value)])}]]
         [:div.upload-field
          [:label.upload-label "YouTube URL (optional)"]
          [:input.upload-input {:type "text" :id "bo-youtube-url" :placeholder "https://www.youtube.com/watch?v=..."
                                :value @(rf/subscribe [:upload-youtube-url])
                                :on-change #(rf/dispatch [:set-upload-youtube-url (-> % .-target .-value)])}]]
         (when error
           [:div.upload-error-msg error])
         (when uploading?
           [:div.upload-loading "Parsing replay... This may take a moment."])
         [:button.upload-submit {:type "submit" :disabled uploading?}
          (if uploading? "Parsing..." "Upload & Parse")]]]])))

;; ─── Delete Confirm Modal ──────────────────────────────────────

(defn- delete-confirm-modal []
  (let [confirm @(rf/subscribe [:delete-confirm])
        logged-in? @(rf/subscribe [:is-logged-in?])]
    (when (and confirm logged-in?)
      [:div.delete-overlay
       {:on-click #(rf/dispatch [:clear-delete-confirm])}
       [:div.delete-content
        {:on-click #(.stopPropagation %)}
        [:h2.delete-title "Confirm Delete"]
        [:p.delete-message (if (= (:type confirm) :bo)
                             "Are you sure you want to delete this build order? This cannot be undone."
                             "Are you sure you want to delete this step?")]
        [:div.delete-actions
         [:button.btn.btn-outline {:on-click #(rf/dispatch [:clear-delete-confirm])} "Cancel"]
         [:button.btn.btn-gold {:on-click #(do
                                             (if (= (:type confirm) :bo)
                                               (rf/dispatch [:delete-build-order (:id confirm)])
                                               (rf/dispatch [:delete-step-api (:id confirm)]))
                                             (rf/dispatch [:clear-delete-confirm]))} "Delete"]]]])))

;; ─── App Root with Routing ──────────────────────────────────────

(defn- nav-pages [route]
  (case route
    :build-order-detail [build-order-detail]
    :build-orders-list [build-order-list]
    [:<>
     [hero]
     [divider]
     [cards-section]
     [stats-strip]
     [testimonial-section]]))

(defn app []
  (let [active-nav @(rf/subscribe [:current-route])]
    (println active-nav)
    [:div.flex.flex-col.min-h-screen.bg-white
     [header]
     [:div.flex-grow [nav-pages active-nav]]
     [footer-component]
     [login-modal]
     [booking-modal]
     [replay-upload-modal]
     [delete-confirm-modal]]))

;; ═══════════════════════════════════════════════════════════════════
;; INIT
;; ═══════════════════════════════════════════════════════════════════

(defonce root (rdc/create-root (.getElementById js/document "app")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (rdc/render root [app]))

(defn ^:export init []
  (rf/dispatch-sync [:initialize-db])
  (routing/init-routing)
  (mount-root))
