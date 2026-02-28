(ns dns-big-d-site.core
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [reagent.dom.client :as rdc]))

;; ═══════════════════════════════════════════════════════════════════
;; I18N — Translation dictionaries
;; ═══════════════════════════════════════════════════════════════════

(def translations
  {:en
   {;; Nav
    :nav/programs        "Programs"
    :nav/community       "Community"
    :nav/youtube         "YouTube"

    ;; Hero
    :hero/eyebrow        "▸ StarCraft II Elite Coaching"
    :hero/title-1        "Climb the"
    :hero/title-2        "Ladder with"
    :hero/sub            "Personalized strategy, replays breakdowns, and the community you need to break through your rank ceiling — for all races and skill levels."
    :hero/cta-book       "Book a Session"
    :hero/cta-youtube    "Watch on YouTube"

    ;; Cards section
    :cards/title         "Choose Your Path"
    :cards/subtitle      "From live coaching to free guides — everything you need to dominate the ladder."

    ;; Card: Coaching
    :card.coaching/label    "Coaching"
    :card.coaching/title    "1-on-1 Replay Analysis"
    :card.coaching/desc     "Get your replays reviewed by DnS. We'll identify the exact mistakes holding you back and build a personalised roadmap to climb the ladder."
    :card.coaching/f1       "Replay analysis"
    :card.coaching/f2       "Personalised builds that fit you"
    :card.coaching/f3       "Live Coaching while playing"
    :card.coaching/f4       "Mindset fixing: ladder anxiety/tilting"
    :card.coaching/btn      "▸ Book a Session"

    ;; Card: Discord
    :card.discord/label     "Community"
    :card.discord/title     "Discord Server"
    :card.discord/desc      "Join a thriving community of StarCraft II players. Share replays, ask questions, find practice partners and get weekly tips from DnS himself."
    :card.discord/f1        "Replay review channels"
    :card.discord/f2        "Group Coaching sessions"
    :card.discord/f3        "Advice to improve"
    :card.discord/btn       "▸ Join the Discord"

    ;; Card: YouTube
    :card.youtube/label     "Free"
    :card.youtube/title     "YouTube Channel"
    :card.youtube/desc      "Free educational content — build orders, masterclasses, pro games broken down by DnS and much, much more."
    :card.youtube/f1        "Weekly in-depth guides"
    :card.youtube/f2        "Masterclasses — A wiser approach to SC2"
    :card.youtube/f3        "Beginner to high level content"
    :card.youtube/btn       "▸ Watch Now"

    ;; Stats
    :stat/gm             "10+"
    :stat/gm-label       "Years of professional Gaming"
    :stat/years          "5+"
    :stat/years-label    "Years Coaching"
    :stat/games          "100+"
    :stat/games-label    "student replays analyzed"

    ;; Testimonials
    :testimonial.1/quote "DnS helped me break out of Diamond and into Master's for the first time in my life, and I am 38 years old!!"
    :testimonial.1/author "— icemanmelting, Diamond → Master"
    :testimonial.2/quote "I really learned a lot and I know i’ll keep on going later to potentially try the GM rank. If you play protoss and want to improve, DnS knows how to listen and adapt to anybody’s level.."
    :testimonial.2/author "— Caporal-chef d’Umbrella, Gold → Diamond"
    :testimonial.3/quote "The feedback was clear, practical, and immediately applicable in ladder games. Overall, it was a very high-level, efficient coaching experience that provided real value. I would definitely recommend it."
    :testimonial.3/author "— GlhfGG, Master 3 → Master 1"

    ;; Modal
    :modal/title-1       "Choose Your "
    :modal/title-2       "Path"
    :modal/subtitle      "Personalized coaching sessions with replay analysis, custom build orders, mindset training, and everything you need to dominate the ladder."
    :modal/single-name   "Single Session"
    :modal/single-price  "$40"
    :modal/single-detail "per hour"
    :modal/single-f1     "In-depth replay analysis"
    :modal/single-f2     "Personalized build orders"
    :modal/single-f3     "Strategic decision-making"
    :modal/single-f4     "Mindset coaching"
    :modal/single-f5     "Custom improvement plan"
    :modal/single-f6     "Post-session notes"
    :modal/single-btn    "Book a Session"
    :modal/pack-badge    "Most Popular"
    :modal/pack-name     "Power Package"
    :modal/pack-price    "$150"
    :modal/pack-detail   "5 hours of coaching"
    :modal/pack-detail2  "Spread across sessions as you need"
    :modal/pack-save     "Save $50"
    :modal/pack-f1       "Flexible scheduling — use when you need"
    :modal/pack-f2       "Advanced replay breakdowns"
    :modal/pack-f3       "Multiple build order refinement"
    :modal/pack-f4       "Mental game mastery"
    :modal/pack-f5       "Progress tracking system"
    :modal/pack-btn      "Get Started"

    ;; Footer
    :footer/copy         "© 2026 DnS SC2 Coaching · All Rights Reserved"}

   :fr
   {;; Nav
    :nav/programs        "Programmes"
    :nav/community       "Communauté"
    :nav/youtube         "YouTube"

    ;; Hero
    :hero/eyebrow        "▸ Coaching d'élite StarCraft II"
    :hero/title-1        "Grimpez le"
    :hero/title-2        "Ladder avec"
    :hero/sub            "Stratégie personnalisée, analyses de replays et la communauté qu'il vous faut pour passer au niveau supérieur — toutes races, tous niveaux."
    :hero/cta-book       "Réserver une Session"
    :hero/cta-youtube    "Voir sur YouTube"

    ;; Divider
    :divider/label       "TOUTES LES RACES · TOUTES LES LIGUES · UN COACH"

    ;; Cards section
    :cards/title         "Choisissez Votre Voie"
    :cards/subtitle      "Du coaching en direct aux guides gratuits — tout ce qu'il faut pour dominer le ladder."

    ;; Card: Coaching
    :card.coaching/label    "Coaching"
    :card.coaching/title    "Analyse de Replay 1-on-1"
    :card.coaching/desc     "Faites analyser vos replays par DnS. On identifiera les erreurs exactes qui vous freinent et on construira une feuille de route personnalisée pour progresser."
    :card.coaching/f1       "Analyse de replays"
    :card.coaching/f2       "Builds personnalisés pour vous"
    :card.coaching/f3       "Coaching en direct pendant vos parties"
    :card.coaching/f4       "Travail de gestion des émotions"
    :card.coaching/btn      "▸ Réserver une Session"

    ;; Card: Discord
    :card.discord/label     "Communauté"
    :card.discord/title     "Serveur Discord"
    :card.discord/desc      "Rejoignez une communauté active de joueurs StarCraft II. Partagez vos replays, posez vos questions, trouvez des partenaires d'entraînement et recevez des conseils hebdomadaires de DnS."
    :card.discord/f1        "Onglet dédiés aux replays"
    :card.discord/f2        "Sessions de coaching en groupe"
    :card.discord/f3        "Conseils pour progresser"
    :card.discord/btn       "▸ Rejoindre le Discord"

    ;; Card: YouTube
    :card.youtube/label     "Gratuit"
    :card.youtube/title     "Chaîne YouTube"
    :card.youtube/desc      "Contenu éducatif gratuit — build orders, masterclasses, parties pro décortiquées par DnS et bien plus encore."
    :card.youtube/f1        "Guides approfondis chaque semaine"
    :card.youtube/f2        "Masterclasses — Une approche plus sage de SC2"
    :card.youtube/f3        "Contenu adapté à tous les niveaux"
    :card.youtube/btn       "▸ Regarder"

    ;; Stats
    :stat/gm             "10+"
    :stat/gm-label       "Ans de très haut niveau"
    :stat/years          "5+"
    :stat/years-label    "Ans de Coaching"
    :stat/games          "100+"
    :stat/games-label    "Replays d'élèves analysés"

    ;; Testimonials
    :testimonial.1/quote "DnS m'a aidé à sortir du Diamant après deux ans de stagnation. Les analyses de replays sont incroyablement détaillées — il voit des choses que je n'aurais jamais remarquées seul."
    :testimonial.1/author "— Icemanmelting, Diamant → Maître"
    :testimonial.2/quote "J'ai vraiment beaucoup appris et je sais que je continuerai par la suite avec lui afin potentiellement tenter le rang GM. Si tu joues protoss et que tu veux évoluer DNS te sera d'une grande aide, car il sait écouter et s'adapter au niveau de chacun."
    :testimonial.2/author "— Caporal-chef d’Umbrella, Or → Diamant"
    :testimonial.3/quote "Les retours étaient clairs, pratiques et applicables immédiatement en ladder. Globalement, c’était du coaching de très haut niveau, efficace qui m’a apporté de la vraie valeur. Je recommande vivement."
    :testimonial.3/author "— CJ, Master 3 → Master 1"

    ;; Modal
    :modal/title-1       "Choisissez Votre "
    :modal/title-2       "Voie"
    :modal/subtitle      "Sessions de coaching personnalisées avec analyse de replays, builds sur mesure, entraînement mental et tout ce qu'il faut pour dominer le ladder."
    :modal/single-name   "Session Unique"
    :modal/single-price  "40 $"
    :modal/single-detail "par heure"
    :modal/single-f1     "Analyse approfondie de replays"
    :modal/single-f2     "Build orders personnalisés"
    :modal/single-f3     "Prise de décision stratégique"
    :modal/single-f4     "Coaching mental"
    :modal/single-f5     "Plan d'amélioration sur mesure"
    :modal/single-f6     "Notes post-session"
    :modal/single-btn    "Réserver une Session"
    :modal/pack-badge    "Le Plus Populaire"
    :modal/pack-name     "Pack Puissance"
    :modal/pack-price    "150 $"
    :modal/pack-detail   "5 heures de coaching"
    :modal/pack-detail2  "Réparties selon vos besoins"
    :modal/pack-save     "Économisez 50 $"
    :modal/pack-f1       "Planning flexible — utilisez quand vous voulez"
    :modal/pack-f2       "Analyses de replays avancées"
    :modal/pack-f3       "Optimisation de plusieurs build orders"
    :modal/pack-f4       "Maîtrise du mental"
    :modal/pack-f5       "Système de suivi de progression"
    :modal/pack-btn      "Commencer"

    ;; Footer
    :footer/copy         "© 2026 DnS SC2 Coaching · Tous Droits Réservés"}})

;; ═══════════════════════════════════════════════════════════════════
;; DB
;; ═══════════════════════════════════════════════════════════════════

(def default-db
  {:lang                :en
   :active-testimonial  0
   :booking-modal-open? false})

;; ═══════════════════════════════════════════════════════════════════
;; EVENTS
;; ═══════════════════════════════════════════════════════════════════

(rf/reg-event-db ::initialize-db       (fn [_ _] default-db))
(rf/reg-event-db ::set-lang            (fn [db [_ lang]] (assoc db :lang lang)))
(rf/reg-event-db ::open-booking-modal  (fn [db _] (assoc db :booking-modal-open? true)))
(rf/reg-event-db ::close-booking-modal (fn [db _] (assoc db :booking-modal-open? false)))
(rf/reg-event-db ::set-active-testimonial (fn [db [_ idx]] (assoc db :active-testimonial idx)))

(rf/reg-event-db
 ::next-testimonial
 (fn [db _]
   (let [next-idx (mod (inc (:active-testimonial db)) 3)]
     (assoc db :active-testimonial next-idx))))

;; ═══════════════════════════════════════════════════════════════════
;; SUBS
;; ═══════════════════════════════════════════════════════════════════

(rf/reg-sub ::lang                (fn [db _] (:lang db)))
(rf/reg-sub ::booking-modal-open? (fn [db _] (:booking-modal-open? db)))
(rf/reg-sub ::active-testimonial  (fn [db _] (:active-testimonial db)))

;; ═══════════════════════════════════════════════════════════════════
;; I18N HELPER
;; ═══════════════════════════════════════════════════════════════════

(defn t
  "Translate a key using the current language."
  [k]
  (let [lang @(rf/subscribe [::lang])]
    (get-in translations [lang k] (str k))))

;; ═══════════════════════════════════════════════════════════════════
;; VIEWS
;; ═══════════════════════════════════════════════════════════════════

;; ─── Language Switcher ──────────────────────────────────────────

(defn lang-switcher []
  (let [lang @(rf/subscribe [::lang])]
    [:div.lang-switcher
     [:button.lang-btn
      {:class    (when (= lang :en) "lang-active")
       :on-click #(rf/dispatch [::set-lang :en])}
      "EN"]
     [:span.lang-sep "│"]
     [:button.lang-btn
      {:class    (when (= lang :fr) "lang-active")
       :on-click #(rf/dispatch [::set-lang :fr])}
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
     [:a {:href "#discord"}  (t :nav/community)]
     [:a {:href "#youtube"}  (t :nav/youtube)]]
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
     {:on-click #(rf/dispatch [::open-booking-modal])}
     (t :hero/cta-book)]
    [:a.btn.btn-outline {:href   "https://www.youtube.com/@DnS_SC2"
                         :target "_blank"
                         :rel    "noopener noreferrer"}
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
    [:p.card-desc  (t (keyword (str "card." (name id)) "desc"))]
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
                 {:on-click #(rf/dispatch [::open-booking-modal])}
                 (t :card.coaching/btn)]
      :discord  [:a.btn.btn-card {:href   "https://discord.gg/RnDY9hyKVA"
                                  :target "_blank"
                                  :rel    "noopener noreferrer"}
                 (t :card.discord/btn)]
      :youtube  [:a.btn.btn-card {:href   "https://www.youtube.com/@DnS_SC2"
                                  :target "_blank"
                                  :rel    "noopener noreferrer"}
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
    [card-component {:id :coaching :thumb "thumb-terran"  :badge "badge-terran"  :badge-icon "🎯"  :label-cls "label-coaching"}]
    [card-component {:id :discord  :thumb "thumb-discord" :badge "badge-discord" :badge-icon "🛡️" :label-cls "label-community"}]
    [card-component {:id :youtube  :thumb "thumb-youtube" :badge "badge-yt"      :badge-icon "▶"   :label-cls "label-free"}]]])

;; ─── Stats Strip ────────────────────────────────────────────────

(defn stats-strip []
  [:div.stats-strip
   (doall
     (for [[num-k lbl-k] [[:stat/gm    :stat/gm-label]
                          [:stat/years :stat/years-label]
                          [:stat/games :stat/games-label]]]
       ^{:key lbl-k}
       [:div.stat
        [:span.stat-number (t num-k)]
        [:span.stat-label  (t lbl-k)]]))])

;; ─── Testimonials (rotating with fade) ──────────────────────────

(defn testimonial-section []
  (let [timer-ref   (r/atom nil)
        start-timer (fn []
                      (when-let [tm @timer-ref]
                        (js/clearInterval tm))
                      (reset! timer-ref
                              (js/setInterval
                               #(rf/dispatch [::next-testimonial])
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
        (let [active-idx @(rf/subscribe [::active-testimonial])
              items      [[1 :testimonial.1/quote :testimonial.1/author]
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
                 {:class    (when (= i active-idx) "dot-active")
                  :on-click (fn []
                              (rf/dispatch [::set-active-testimonial i])
                              (start-timer))}]))]]))})))

;; ─── Footer ─────────────────────────────────────────────────────

(defn footer-component []
  [:footer
   [:div.footer-logo "DnS Coaching"]
   [:p.footer-copy (t :footer/copy)]])

;; ─── Booking Modal ──────────────────────────────────────────────

(defn booking-modal []
  (let [open? @(rf/subscribe [::booking-modal-open?])]
    (when open?
      [:div.modal-overlay
       {:on-click (fn [e]
                    (when (= (.-target e) (.-currentTarget e))
                      (rf/dispatch [::close-booking-modal])))}
       [:div.modal-content
        [:button.modal-close
         {:on-click #(rf/dispatch [::close-booking-modal])}
         "✕"]
        [:h2.modal-title (t :modal/title-1) [:em (t :modal/title-2)]]
        [:p.modal-subtitle (t :modal/subtitle)]
        [:div.pricing-grid
         ;; Single Session
         [:div.pricing-card
          [:p.pricing-name   (t :modal/single-name)]
          [:p.pricing-price  (t :modal/single-price)]
          [:p.pricing-detail (t :modal/single-detail)]
          [:ul.pricing-features
           [:li (t :modal/single-f1)]
           [:li (t :modal/single-f2)]
           [:li (t :modal/single-f3)]
           [:li (t :modal/single-f4)]
           [:li (t :modal/single-f5)]
           [:li (t :modal/single-f6)]]
          [:a.btn.btn-card.pricing-btn
           {:href   "https://buy.stripe.com/eVq8wIcZLcyNbfMdDn63K00"
            :target "_blank"
            :rel    "noopener noreferrer"}
           (t :modal/single-btn)]]
         ;; Power Package
         [:div.pricing-card.pricing-featured
          [:span.pricing-badge (t :modal/pack-badge)]
          [:p.pricing-name (t :modal/pack-name)]
          [:p.pricing-price.pricing-price-gold (t :modal/pack-price)]
          [:p.pricing-detail     (t :modal/pack-detail)]
          [:p.pricing-detail-sub (t :modal/pack-detail2)]
          [:p.pricing-save       (t :modal/pack-save)]
          [:ul.pricing-features
           [:li (t :modal/pack-f1)]
           [:li (t :modal/pack-f2)]
           [:li (t :modal/pack-f3)]
           [:li (t :modal/pack-f4)]
           [:li (t :modal/pack-f5)]]
          [:a.btn.btn-gold.pricing-btn
           {:href   "https://buy.stripe.com/cNicMY4tf42hgA68j363K01"
            :target "_blank"
            :rel    "noopener noreferrer"}
           (t :modal/pack-btn)]]]]])))

;; ─── App Root ───────────────────────────────────────────────────

(defn app []
  [:<>
   [header]
   [hero]
   [divider]
   [cards-section]
   [stats-strip]
   [testimonial-section]
   [footer-component]
   [booking-modal]])

;; ═══════════════════════════════════════════════════════════════════
;; INIT
;; ═══════════════════════════════════════════════════════════════════

(defonce root (rdc/create-root (.getElementById js/document "app")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (rdc/render root [app]))

(defn ^:export init []
  (rf/dispatch-sync [::initialize-db])
  (mount-root))
