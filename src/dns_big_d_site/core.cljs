(ns dns-big-d-site.core
  (:require
    [clojure.string :as str]
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

    ;; Modal - missing keys
    :modal/single-f6 "Session notes"
    :modal/pack-badge "Most Popular"
    :modal/pack-name "Power Package"
    :modal/pack-price "$150"
    :modal/pack-detail "5 hours of coaching"
    :modal/pack-detail2 "Split however you need"
    :modal/pack-save "Save $50"
    :modal/pack-f1 "Flexible scheduling — use whenever you want"
    :modal/pack-f2 "Advanced replay analysis"
    :modal/pack-f3 "Multiple build order optimization"
    :modal/pack-f4 "Mental game mastery"
    :modal/pack-f5 "Progress tracking system"
    :modal/pack-btn "Get Started"

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
;; FIX: The outer [:div ...] was closed before its children were listed.
;;      All children must be inside the vector, not siblings of it.

(defn lang-switcher []
  (let [lang @(rf/subscribe [:lang])]
    [:div.flex.items-center.gap-1.rounded-full.p-1.border.border-gray-700
     [:button.px-3.py-1.rounded-full.text-sm.font-medium.transition-all.duration-200
      {:class (if (= lang :en) "bg-amber-500 text-white shadow-sm" "text-gray-400 hover:text-white")
       :on-click #(rf/dispatch [:set-lang :en])}
      "EN"]
     [:span.text-gray-600 "|"]
     [:button.px-3.py-1.rounded-full.text-sm.font-medium.transition-all.duration-200
      {:class (if (= lang :fr) "bg-amber-500 text-white shadow-sm" "text-gray-400 hover:text-white")
       :on-click #(rf/dispatch [:set-lang :fr])}
      "FR"]]))

;; ─── Header ─────────────────────────────────────────────────────

(defn header []
  [:header.flex.items-center.justify-between.px-6.py-4.bg-gray-900.backdrop-blur-md.border-b.border-gray-800.sticky.top-0.z-40
   [:div.flex.items-center.gap-2.font-bold.text-xl.text-white.select-none
    "DnS" [:span.text-gray-400.font-normal " "] "SC2 Coaching"]
   [:div.flex.items-center.gap-6
    [:nav.flex.items-center.gap-5.text-sm.font-medium.text-gray-300
     [:a.hover:text-amber-400.transition-colors {:href "#programs"} (t :nav/programs)]
     [:a.hover:text-amber-400.transition-colors {:href "#discord"} (t :nav/community)]
     [:a.hover:text-amber-400.transition-colors {:href "#youtube"} (t :nav/youtube)]
     [:a.hover:text-amber-400.transition-colors
      {:href "#build-orders"
       :on-click #(do (.preventDefault %)
                      (js/history.pushState nil "" "/build-orders")
                      (rf/dispatch [:navigate {:route :build-orders-list :id nil}]))}
      (t :nav/build-orders)]]
    [lang-switcher]]])

;; ─── Hero ───────────────────────────────────────────────────────
;; FIX: Text content was placed as siblings to the element vectors instead of inside them.
;;      e.g. [:p ...] (t :key) → [:p ... (t :key)]

(defn hero []
  [:section.relative.flex.flex-col.items-center.justify-center.text-center.px-6.py-24.md:py-32.overflow-hidden
   {:style {:background "linear-gradient(to bottom, #111827, #030712)"}}
   [:div.absolute.inset-0.pointer-events-none
    {:style {:background "radial-gradient(circle at center, rgba(245,158,11,0.1) 0%, transparent 70%)"}}]
   [:p.text-amber-400.font-semibold.mb-4.uppercase.tracking-widest.text-sm (t :hero/eyebrow)]
   [:h1.text-4xl.md:text-6xl.font-bold.text-white.mb-6.leading-tight
    (t :hero/title-1) [:br] (t :hero/title-2) [:br] [:em.text-amber-400 "DnS"]]
   [:p.text-gray-400.max-w-2xl.mx-auto.mb-10.text-lg.leading-relaxed (t :hero/sub)]
   [:div.flex.flex-col.sm:flex-row.gap-4.justify-center
    [:button.px-6.py-3.bg-amber-500.hover:bg-amber-600.text-white.font-semibold.rounded-lg.transition-all.shadow-lg
     {:on-click #(rf/dispatch [:open-booking-modal])}
     (t :hero/cta-book)]
    [:a.px-6.py-3.border.border-gray-600.hover:border-amber-400.text-white.font-semibold.rounded-lg.transition-all
     {:href "https://www.youtube.com/@DnS_SC2" :target "_blank" :rel "noopener noreferrer"}
     (t :hero/cta-youtube)]]])

;; ─── Divider ────────────────────────────────────────────────────
;; FIX: Text was a sibling of the element, not inside it.

(defn divider []
  [:div.flex.items-center.justify-center.py-8.gap-4.text-gray-500.font-medium.uppercase.tracking-widest
   [:span.text-xs (t :divider/label)]])

;; ─── Card ───────────────────────────────────────────────────────
;; FIX: Multiple [:p ...] and [:li ...] had text content as siblings instead of children.
;;      Also fixed [:ul] children — each [:li ...] must contain its text inline.

(defn card-component [{:keys [id badge-icon bg-gradient]}]
  [:div.border.border-gray-700.rounded-xl.overflow-hidden.transition-all.duration-300.flex.flex-col
   {:class "bg-gray-800/50 hover:border-amber-500/50 hover:shadow-lg hover:shadow-amber-500/10"
    :id (when (#{:discord :youtube} id) (name id))}
   ;; Thumbnail
   [:div.relative.h-48.flex.items-center.justify-center.overflow-hidden
    {:style {:background bg-gradient}}
    ;; Label badge - top right
    [:span.absolute.top-3.right-3.px-2.py-1.text-white.text-xs.font-mono.font-bold.rounded.border.border-white-20.tracking-widest
     {:style {:background "rgba(0,0,0,0.45)" :letter-spacing "0.1em"}}
     (str/upper-case (t (keyword (str "card." (name id)) "label")))]
    ;; Icon box
    [:div.flex.items-center.justify-center.rounded-2xl.border.border-white-20
     {:style {:width "96px" :height "96px" :background "rgba(255,255,255,0.08)" :font-size "3rem"}}
     badge-icon]]
   ;; Body
   [:div.p-6.flex.flex-col.flex-1.gap-3
    [:p.text-xl.font-bold.text-white.mb-1 (t (keyword (str "card." (name id)) "title"))]
    [:p.text-gray-400.text-sm.leading-relaxed (t (keyword (str "card." (name id)) "desc"))]
    [:ul.list-none.p-0.mt-2.pt-4.border-t.border-gray-700.flex.flex-col.gap-1
     (doall
       (for [i (range 1 10)
             :let [k (keyword (str "card." (name id)) (str "f" i))
                   v (get-in translations [:en k])]
             :when v]
         ^{:key i}
         [:li.flex.items-center.gap-2.text-sm.text-gray-300
          [:span.text-amber-500 "▸"] (t k)]))]
    ;; Button pinned to bottom
    [:div.mt-auto.pt-4
     (case id
       :coaching
       [:button.w-full.py-2.5.bg-transparent.border.border-amber-500.text-amber-500.hover:bg-amber-500.hover:text-white.font-mono.font-bold.tracking-widest.rounded-lg.transition-all.text-sm
        {:on-click #(rf/dispatch [:open-booking-modal])}
        (str/upper-case (t :card.coaching/btn))]

       :discord
       [:a.w-full.py-2.5.bg-transparent.border.border-amber-500.text-amber-500.hover:bg-amber-500.hover:text-white.font-mono.font-bold.tracking-widest.rounded-lg.transition-all.text-sm.text-center.block
        {:href "https://discord.gg/RnDY9hyKVA" :target "_blank" :rel "noopener noreferrer"}
        (str/upper-case (t :card.discord/btn))]

       :youtube
       [:a.w-full.py-2.5.bg-transparent.border.border-amber-500.text-amber-500.hover:bg-amber-500.hover:text-white.font-mono.font-bold.tracking-widest.rounded-lg.transition-all.text-sm.text-center.block
        {:href "https://www.youtube.com/@DnS_SC2" :target "_blank" :rel "noopener noreferrer"}
        (str/upper-case (t :card.youtube/btn))])]]])

(defn cards-section []
  [:<>
   [:div.text-center.mb-12 {:id "programs"}
    [:h2.text-3xl.md:text-4xl.font-bold.text-white.mb-3 (t :cards/title)]
    [:p.text-gray-400.max-w-xl.mx-auto (t :cards/subtitle)]]
   [:div.grid.grid-cols-1.md:grid-cols-2.lg:grid-cols-3.gap-6.px-6
    [card-component {:id :coaching :badge-icon "🎯"
                     :bg-gradient "linear-gradient(135deg, #0f2444, #1a3a6e)"}]
    [card-component {:id :discord :badge-icon "🛡️"
                     :bg-gradient "linear-gradient(135deg, #1e0a3c, #4a1a8c)"}]
    [card-component {:id :youtube :badge-icon "▶"
                     :bg-gradient "linear-gradient(135deg, #2d0a0a, #8b1a1a)"}]]])

;; ─── Stats Strip ────────────────────────────────────────────────
;; FIX: Missing closing paren for the enclosing [:div ...].

(defn stats-strip []
  [:div.grid.grid-cols-1.md:grid-cols-3.gap-6.px-6.py-12.border-y.border-gray-800
   {:style {:background "rgba(17,24,39,0.5)"}}
   (doall
     (for [[num-k lbl-k] [[:stat/gm :stat/gm-label]
                          [:stat/years :stat/years-label]
                          [:stat/games :stat/games-label]]]
       ^{:key lbl-k}
       [:div.text-center
        [:span.block.text-3xl.md:text-4xl.font-bold.text-amber-400.mb-1 (t num-k)]
        [:span.block.text-sm.text-gray-400.uppercase.tracking-wider (t lbl-k)]]))])

;; ─── Testimonials ───────────────────────────────────────────────

(defn testimonial-section []
  (let [timer-ref (r/atom nil)
        start-timer (fn []
                      (when-let [tm @timer-ref] (js/clearInterval tm))
                      (reset! timer-ref
                              (js/setInterval #(rf/dispatch [:next-testimonial]) 6000)))]
    (r/create-class
      {:component-did-mount (fn [_] (start-timer))
       :component-will-unmount (fn [_] (when-let [tm @timer-ref] (js/clearInterval tm)))
       :reagent-render
       (fn []
         (let [active-idx @(rf/subscribe [:active-testimonial])
               items [[1 :testimonial.1/quote :testimonial.1/author]
                      [2 :testimonial.2/quote :testimonial.2/author]
                      [3 :testimonial.3/quote :testimonial.3/author]]]
           [:div.relative.py-16.px-6.max-w-4xl.mx-auto.text-center
            [:div.relative {:style {:min-height "180px"}}
             (doall
               (for [[idx qk ak] items]
                 ^{:key idx}
                 [:div.absolute.inset-0.transition-opacity.duration-500.flex.flex-col.items-center.justify-center
                  {:class (if (= (dec idx) active-idx) "opacity-100 z-10" "opacity-0 z-0 pointer-events-none")}
                  [:blockquote.text-xl.md:text-2xl.text-gray-200.font-medium.italic.mb-4 (t qk)]
                  [:p.text-sm.text-gray-500.font-semibold (t ak)]]))]
            [:div.flex.gap-2.justify-center.mt-8
             (doall
               (for [i (range 3)]
                 ^{:key i}
                 [:button.w-3.h-3.rounded-full.bg-gray-600.transition-all.duration-200.hover:bg-gray-500
                  {:class (when (= i active-idx) "bg-amber-400 w-8")
                   :on-click (fn []
                               (rf/dispatch [:set-active-testimonial i])
                               (start-timer))}]))]]))})))

;; ─── Footer ─────────────────────────────────────────────────────

(defn footer-component []
  (let [click-count (r/atom 0)
        timer (r/atom nil)]
    (fn []
      [:footer.py-12.px-6.border-t.border-gray-800.text-center
       {:style {:background "#030712"}}
       [:div.mb-4.font-bold.text-lg.text-white "DnS Coaching"]
       [:p.cursor-pointer.text-gray-500.text-sm.hover:text-gray-300.transition-colors
        {:on-click (fn []
                     (reset! click-count (inc @click-count))
                     (when-let [tm @timer] (js/clearTimeout tm))
                     (reset! timer
                             (js/setTimeout #(reset! click-count 0) 2000))
                     (when (= @click-count 3)
                       (rf/dispatch [:open-login-modal])
                       (reset! click-count 0)))}
        (t :footer/copy)]])))

;; ─── Login Modal ────────────────────────────────────────────────
;; FIX: [:h2 ...], [:form ...], [:div ...] etc. had text as siblings, not children.
;;      Also fixed (when error ...) placement — must be inside the form vector.

(defn login-modal []
  (let [open? @(rf/subscribe [:login-modal-open?])
        username @(rf/subscribe [:login-username])
        password @(rf/subscribe [:login-password])
        error @(rf/subscribe [:login-error])]
    (when open?
      [:div.fixed.inset-0.z-50.flex.items-center.justify-center.backdrop-blur-sm.p-4
       {:style {:background "rgba(0,0,0,0.7)"}
        :on-click (fn [e]
                    (when (= (.-target e) (.-currentTarget e))
                      (rf/dispatch [:close-login-modal])))}
       [:div.border.border-gray-700.rounded-xl.p-8.w-full.max-w-md.shadow-2xl.relative
        {:style {:background "#111827"}}
        [:button.absolute.top-4.right-4.text-gray-500.hover:text-white.transition-colors
         {:on-click #(rf/dispatch [:close-login-modal])} "✕"]
        [:div.flex.items-center.gap-2.mb-6.font-bold.text-xl.select-none
         [:span.text-white "DnS"] [:span.text-gray-400.font-normal " BACKOFFICE"]]
        [:h2.text-xl.font-bold.text-white.mb-6 (t :login/title)]
        [:form.flex.flex-col.gap-4
         {:on-submit #(do (.preventDefault %)
                          (rf/dispatch [:login {:username username :password password}]))}
         [:div.flex.flex-col.gap-1
          [:label.text-sm.font-medium.text-gray-300 (t :login/username)]
          [:input.w-full.px-3.py-2.border.border-gray-700.rounded-lg.text-white.placeholder-gray-500.focus:outline-none.focus:border-amber-500
           {:style {:background "#1f2937"}
            :type "text" :value username :placeholder (t :login/username-ph) :auto-focus true
            :on-change #(rf/dispatch [:set-login-username (-> % .-target .-value)])}]]
         [:div.flex.flex-col.gap-1
          [:label.text-sm.font-medium.text-gray-300 (t :login/password)]
          [:input.w-full.px-3.py-2.border.border-gray-700.rounded-lg.text-white.placeholder-gray-500.focus:outline-none.focus:border-amber-500
           {:style {:background "#1f2937"}
            :type "password" :value password :placeholder (t :login/password-ph)
            :on-change #(rf/dispatch [:set-login-password (-> % .-target .-value)])}]]
         [:div.flex.items-center.justify-between.text-sm
          [:label.flex.items-center.gap-2.cursor-pointer.text-gray-400.hover:text-white
           [:input.w-4.h-4.border-gray-600.rounded {:type "checkbox"}]
           [:span (t :login/remember)]]
          [:a.text-amber-400.hover:text-amber-300.transition-colors {:href "#"} (t :login/forgot)]]
         (when error
           [:div.text-red-400.text-sm.border.border-red-800.rounded-lg.p-3.mb-2
            {:style {:background "rgba(127,29,29,0.2)"}}
            error])
         [:button.w-full.py-3.bg-amber-500.hover:bg-amber-600.text-white.font-semibold.rounded-lg.transition-all.shadow-lg
          {:type "submit"} (t :login/signin)]]
        [:div.my-6.border-t.border-gray-800]
        [:p.text-xs.text-gray-500.text-center.leading-relaxed (t :login/hint)]]])))

;; ─── Booking Modal ──────────────────────────────────────────────
;; FIX: The grid div and card divs were placed outside the outer container div.
;;      Restructured so everything is properly nested inside one root container.

(defn booking-modal []
  (let [open? @(rf/subscribe [:booking-modal-open?])]
    (when open?
      [:div.fixed.inset-0.z-50.flex.items-center.justify-center.backdrop-blur-sm.p-4
       {:style {:background "rgba(0,0,0,0.7)"}
        :on-click (fn [e]
                    (when (= (.-target e) (.-currentTarget e))
                      (rf/dispatch [:close-booking-modal])))}
       [:div.border.border-gray-700.rounded-xl.p-6.md:p-8.w-full.max-w-3xl.shadow-2xl.relative.overflow-y-auto
        {:style {:background "#111827" :max-height "90vh"}}
        [:button.absolute.top-4.right-4.text-gray-500.hover:text-white.transition-colors
         {:on-click #(rf/dispatch [:close-booking-modal])} "✕"]
        [:h2.text-2xl.md:text-3xl.font-bold.text-white.mb-2
         (t :modal/title-1) [:em.text-amber-400 (t :modal/title-2)]]
        [:p.text-gray-400.max-w-xl.mb-8 (t :modal/subtitle)]
        [:div.grid.grid-cols-1.md:grid-cols-2.gap-6
         ;; Single Session
         [:div.border.border-gray-700.rounded-xl.p-6.relative.flex.flex-col
          {:style {:background "rgba(31,41,55,0.5)"}}
          [:div {:style {:min-height "160px"}}
           [:p.text-lg.font-bold.text-white.mb-1 (t :modal/single-name)]
           [:p.text-3xl.font-bold.text-white.mb-1 (t :modal/single-price)]
           [:p.text-sm.text-gray-400.mb-6 (t :modal/single-detail)]]
          [:ul.list-none.p-0.pt-4.border-t.border-gray-700.mb-6
           [:li.flex.items-center.gap-2.text-sm.text-gray-300.mb-1 (t :modal/single-f1)]
           [:li.flex.items-center.gap-2.text-sm.text-gray-300.mb-1 (t :modal/single-f2)]
           [:li.flex.items-center.gap-2.text-sm.text-gray-300.mb-1 (t :modal/single-f3)]
           [:li.flex.items-center.gap-2.text-sm.text-gray-300.mb-1 (t :modal/single-f4)]
           [:li.flex.items-center.gap-2.text-sm.text-gray-300.mb-1 (t :modal/single-f5)]
           [:li.flex.items-center.gap-2.text-sm.text-gray-300.mb-1 (t :modal/single-f6)]]
          [:a.w-full.py-3.mt-auto.bg-gray-700.hover:bg-amber-500.text-white.font-semibold.rounded-lg.transition-all.text-center.block
           {:href "https://buy.stripe.com/eVq8wIcZLcyNbfMdDn63K00" :target "_blank" :rel "noopener noreferrer"}
           (t :modal/single-btn)]]
         ;; Power Package
         [:div.border.rounded-xl.p-6.relative.flex.flex-col.shadow-lg
          {:style {:background "rgba(31,41,55,0.5)"
                   :border-color "rgba(245,158,11,0.5)"
                   :box-shadow "0 10px 40px rgba(245,158,11,0.1)"}}
          [:span.absolute.top-4.right-4.px-2.py-1.bg-amber-500.text-white.text-xs.font-bold.rounded-full
           (t :modal/pack-badge)]
          [:div {:style {:min-height "160px"}}
           [:p.text-lg.font-bold.text-white.mb-1 (t :modal/pack-name)]
           [:p.text-3xl.font-bold.text-amber-400.mb-1 (t :modal/pack-price)]
           [:p.text-sm.text-gray-400.mb-1 (t :modal/pack-detail)]
           [:p.text-sm.text-gray-400.mb-1 (t :modal/pack-detail2)]
           [:p.text-sm.font-semibold.text-green-400.mb-6 (t :modal/pack-save)]]
          [:ul.list-none.p-0.pt-4.border-t.border-gray-700.mb-6
           [:li.flex.items-center.gap-2.text-sm.text-gray-300.mb-1 (t :modal/pack-f1)]
           [:li.flex.items-center.gap-2.text-sm.text-gray-300.mb-1 (t :modal/pack-f2)]
           [:li.flex.items-center.gap-2.text-sm.text-gray-300.mb-1 (t :modal/pack-f3)]
           [:li.flex.items-center.gap-2.text-sm.text-gray-300.mb-1 (t :modal/pack-f4)]
           [:li.flex.items-center.gap-2.text-sm.text-gray-300.mb-1 (t :modal/pack-f5)]]
          [:a.w-full.py-3.mt-auto.bg-amber-500.hover:bg-amber-600.text-white.font-semibold.rounded-lg.transition-all.shadow-lg.text-center.block
           {:href "https://buy.stripe.com/cNicMY4tf42hgA68j363K01" :target "_blank" :rel "noopener noreferrer"}
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
  [:div.flex.items-center.gap-2
   [:img.w-8.h-8.object-cover.rounded-md
    {:style {:background "#1f2937"}
     :src (get-icon-path action-name)
     :alt action-name
     #_#_:on-error #(set! (.. % -target -src) "/img/placeholder.jpg")}]
   [:span.text-sm.font-medium.text-gray-300
    (clojure.string/upper-case action-name)]])

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
  [:div.border.border-gray-700.rounded-lg.p-4
   {:style {:background "rgba(31,41,55,0.3)"}}
   [:span.block.text-xs.font-semibold.text-gray-500.uppercase.tracking-wider.mb-1 label]
   [:p.text-white.font-medium (or value "—")]])

;; ─── Build Order List ───────────────────────────────────────────
;; FIX: Second top-level [:div ...] was outside the outer [:div ...] — merged into one root.
;;      Also removed stray closing bracket after the grid div.

(defn- build-order-list []
  (fn []
    (let [orders @(rf/subscribe [:build-orders])
          logged-in? @(rf/subscribe [:is-logged-in?])]
      [:div.flex.flex-col.min-h-screen.text-white
       {:style {:background "#030712"}} ; Dark background matching screenshot

       ;; Header Section
       [:div.flex.items-center.justify-between.px-6.py-8.border-b.border-gray-800
        {:style {:background "rgba(17,24,39,0.5)"}}
        [:h1.text-2xl.font-bold.text-white "Build Orders"]
        (when logged-in?
          [:button.px-4.py-2.bg-amber-500.hover:bg-amber-600.text-white.font-semibold.rounded-lg.transition-all.shadow-md
           {:on-click #(rf/dispatch [:open-upload-modal])}
           "Upload Replay"])]

       ;; Grid Section
       [:div.grid.grid-cols-1.md:grid-cols-2.lg:grid-cols-3.gap-6.px-6.py-8
        (if (empty? orders)
          [:div.col-span-full.text-center.py-12.text-gray-500 "No build orders yet."]
          (doall
            (for [bo orders]
              ^{:key (:build_orders/id bo)}

              ;; CARD CONTAINER
              [:div.relative.border-2.border-amber-500-80.rounded-xl.p-6.cursor-pointer.transition-all
               {:class "bg-slate-900 hover:border-amber-400 hover:shadow-[0_0_15px_rgba(245,158,11,0.3)]"
                :on-click #(do
                             (.stopPropagation %)
                             (js/history.pushState nil "" (str "/build-orders/" (:build_orders/id bo)))
                             (rf/dispatch [:set-selected-build-order bo])
                             (rf/dispatch [:navigate {:route :build-order-detail :id (str (:build_orders/id bo))}]))}

               ;; Delete Button (Top Right)
               (when logged-in?
                 [:div.absolute.top-4.right-4.cursor-pointer.text-gray-500.hover:text-red-500.transition-colors.z-10
                  {:on-click #(do (.stopPropagation %)
                                  (rf/dispatch [:set-delete-confirm {:type :bo :id (:build_orders/id bo)}]))}
                  "🗑"])

               ;; Title (Amber/Gold color)
               [:h3.text-xl.font-bold.text-amber-400.mb-2.line-clamp-1
                (:build_orders/name bo)]

               ;; Tags / Playstyle (Pill shape, dark bg)
               [:div.flex.flex-wrap.gap-2.mb-4
                (when (:build_orders/tags bo) ;; Assuming tags are stored in a vector :tags or similar
                  (for [tag (:build_orders/tags bo)]
                    [:span.inline-block.px-2.py-1.bg-gray-800.text-xs.font-medium.rounded-md.text-gray-300
                     tag]))]

               ;; Description (Gray text, truncated with ...)
               [:p.text-sm.text-gray-400.line-clamp-3.mb-6.leading-relaxed
                (:build_orders/play_style bo)]

               ;; Footer Area (Author + View Link)
               [:div.flex.items-center.justify-between.mt-auto.pt-4.border-t.border-gray-800
                [:div.text-sm.text-gray-500
                 "By "
                 [:span.text-white.font-medium (:build_orders/author bo)]]

                ;; "View Build Order" Link with Play Icon
                [:div.flex.items-center.gap-2.text-amber-500.font-semibold.cursor-pointer.hover:text-amber-400.transition-colors
                 [:svg.w-4.h-4.fill-current {:viewBox "0 0 24 24"}
                  [:path {:d "M8 5v14l11-7z"}]]
                 [:span.text-sm "View Build Order"]]]])))]])))



(defn- tag-badge [label]
  [:span.px-3.py-1.border.border-gray-600.rounded-full.text-xs.font-semibold.text-gray-300.uppercase.tracking-wider
   label])

(defn- info-card-styled [{:keys [label value color icon bullet-list?]}]
  (let [border-color (case color
                       :amber "border-amber-500/40"
                       :teal  "border-teal-500/40"
                       :red   "border-red-500/40"
                       "border-gray-700")
        bg-color (case color
                   :amber "rgba(120,53,15,0.15)"
                   :teal  "rgba(20,184,166,0.08)"
                   :red   "rgba(185,28,28,0.12)"
                   "rgba(31,41,55,0.3)")
        label-color (case color
                      :amber "text-amber-400"
                      :teal  "text-teal-400"
                      :red   "text-red-400"
                      "text-gray-400")]
    [:div.border.rounded-xl.p-5
     {:style {:background bg-color :border-color border-color}
      :class border-color}
     [:div.flex.items-center.gap-2.mb-3
      (when icon [:span {:class label-color} icon])
      [:span.text-xs.font-bold.uppercase.tracking-widest {:class label-color} label]]
     (if bullet-list?
       [:ul.space-y-1
        (for [item (clojure.string/split-lines (or value ""))]
          (when (not (clojure.string/blank? item))
            ^{:key item}
            [:li.flex.items-start.gap-2.text-sm.text-gray-300
             [:span {:class label-color} "•"] item]))]
       [:p.text-sm.text-gray-300.leading-relaxed (or value "—")])]))

(defn- build-order-detail []
  (fn []
    (let [bo @(rf/subscribe [:selected-build-order])
          logged-in? @(rf/subscribe [:is-logged-in?])
          edit-bo-id @(rf/subscribe [:edit-bo-id])]
      (if-not bo
        [:div.flex.items-center.justify-center.min-h-screen.text-white
         {:style {:background "#030712"}}
         [:h1.text-2xl.font-bold.text-white "Build Order Not Found"]]

        [:div.flex.flex-col.min-h-screen.text-white
         {:style {:background "#030712"}}

         ;; ── Hero / Header card ────────────────────────────────────────────
         [:div.px-6.py-8.mb-6
          {:style {:background "rgba(17,24,39,0.8)"
                   :border-bottom "1px solid rgba(75,85,99,0.4)"}}

          [:div.max-w-4xl.mx-auto   ;; <-- centered wrapper

           ;; Title row
           [:div.flex.items-start.justify-between.mb-4
            (if (= (:id bo) edit-bo-id)
              [:input.w-full.max-w-xl.px-3.py-2.border.border-gray-700.rounded-lg.text-white.text-3xl.font-bold.focus:outline-none.focus:border-amber-500
               {:style {:background "#1f2937"}
                :value (:name bo)
                :on-change #(rf/dispatch [:update-selected-bo-field :name (-> % .-target .-value)])}]
              [:h1.text-3xl.font-bold.text-white.leading-tight (:name bo)])
            (when logged-in?
              [:div.flex.items-center.gap-3.ml-6.flex-shrink-0
               [:button.px-4.py-2.border.border-gray-600.hover:border-amber-400.text-white.font-medium.rounded-lg.transition-all.text-sm
                {:on-click #(rf/dispatch [:navigate {:route :build-orders-list :id nil}])}
                "← Back"]
               (if (= (:id bo) edit-bo-id)
                 [:<>
                  [:button.px-4.py-2.bg-amber-500.hover:bg-amber-600.text-white.font-semibold.rounded-lg.transition-all.text-sm
                   {:on-click #(rf/dispatch [:update-build-order-api (:id bo)
                                             {:name (:name bo)
                                              :author (:author bo)
                                              :play_style (:play_style bo)
                                              :youtube_url (:youtube_url bo)
                                              :strategic_goals (:strategic_goals bo)
                                              :counters (:counters bo)
                                              :weaknesses (:weaknesses bo)
                                              :transition_plan (:transition_plan bo)}])}
                   "Save"]
                  [:button.px-4.py-2.border.border-gray-600.hover:border-amber-400.text-white.font-medium.rounded-lg.transition-all.text-sm
                   {:on-click #(rf/dispatch [:set-edit-bo-id nil])}
                   "Cancel"]]
                 [:button.px-4.py-2.border.border-gray-600.hover:border-amber-400.text-white.font-medium.rounded-lg.transition-all.text-sm
                  {:on-click #(rf/dispatch [:set-edit-bo-id (:id bo)])}
                  "Edit"])])]

           ;; Author
           (if (= (:id bo) edit-bo-id)
             [:input.w-full.max-w-md.px-3.py-2.border.border-gray-700.rounded-lg.text-white.focus:outline-none.focus:border-amber-500.mb-4
              {:style {:background "#1f2937"}
               :value (:author bo)
               :on-change #(rf/dispatch [:update-selected-bo-field :author (-> % .-target .-value)])}]
             [:p.text-sm.mb-5
              [:span.text-gray-400 "By "]
              [:span.text-amber-400.font-semibold (:author bo)]])

           ;; Video / action buttons
           [:div.flex.flex-wrap.gap-3
            (when (:youtube_url bo)
              [:a.flex.items-center.gap-2.px-4.py-2.border.border-gray-600.hover:border-amber-400.text-white.text-sm.font-medium.rounded-lg.transition-all
               {:href (:youtube_url bo) :target "_blank"}
               "↗ Watch Video Guide"])
            (when (= (:id bo) edit-bo-id)
              [:div.border.border-gray-700.rounded-lg.p-3 {:style {:background "rgba(31,41,55,0.3)"}}
               [:span.block.text-xs.font-semibold.text-gray-500.uppercase.tracking-wider.mb-1 "YouTube URL"]
               [:input.w-full.max-w-sm.px-3.py-2.border.border-gray-600.rounded-lg.text-white.text-sm.focus:outline-none.focus:border-amber-500
                {:style {:background "#1f2937"}
                 :value (:youtube_url bo) :placeholder "https://www.youtube.com/watch?v=..."
                 :on-change #(rf/dispatch [:update-selected-bo-field :youtube_url (-> % .-target .-value)])}]])]]]

         ;; ── Info cards ───────────────────────────────────────────────────
         [:div.px-6.mb-8
          [:div.max-w-4xl.mx-auto   ;; <-- centered wrapper

           (if (= (:id bo) edit-bo-id)
             [:div.grid.grid-cols-1.md:grid-cols-2.lg:grid-cols-3.gap-4
              [:div.border.border-gray-700.rounded-xl.p-4 {:style {:background "rgba(31,41,55,0.3)"}}
               [:span.block.text-xs.font-bold.uppercase.tracking-widest.text-amber-400.mb-2 "Play Style"]
               [:textarea.w-full.px-3.py-2.border.border-gray-600.rounded-lg.text-white.focus:outline-none.focus:border-amber-500.text-sm
                {:style {:background "#1f2937" :min-height "120px" :resize "vertical"}
                 :value (:play_style bo) :placeholder "e.g. PvZ aggressive opener"
                 :on-change #(rf/dispatch [:update-selected-bo-field :play_style (-> % .-target .-value)])}]]
              [:div.border.border-gray-700.rounded-xl.p-4 {:style {:background "rgba(31,41,55,0.3)"}}
               [:span.block.text-xs.font-bold.uppercase.tracking-widest.text-amber-400.mb-2 "Strategic Goals"]
               [:textarea.w-full.px-3.py-2.border.border-gray-600.rounded-lg.text-white.focus:outline-none.focus:border-amber-500.text-sm
                {:style {:background "#1f2937" :min-height "120px" :resize "vertical"}
                 :value (:strategic_goals bo) :placeholder "Main goals of this build"
                 :on-change #(rf/dispatch [:update-selected-bo-field :strategic_goals (-> % .-target .-value)])}]]
              [:div.border.border-gray-700.rounded-xl.p-4 {:style {:background "rgba(31,41,55,0.3)"}}
               [:span.block.text-xs.font-bold.uppercase.tracking-widest.text-teal-400.mb-2 "Counters"]
               [:textarea.w-full.px-3.py-2.border.border-gray-600.rounded-lg.text-white.focus:outline-none.focus:border-amber-500.text-sm
                {:style {:background "#1f2937" :min-height "120px" :resize "vertical"}
                 :value (:counters bo) :placeholder "What this build counters"
                 :on-change #(rf/dispatch [:update-selected-bo-field :counters (-> % .-target .-value)])}]]
              [:div.border.border-gray-700.rounded-xl.p-4 {:style {:background "rgba(31,41,55,0.3)"}}
               [:span.block.text-xs.font-bold.uppercase.tracking-widest.text-red-400.mb-2 "Weaknesses"]
               [:textarea.w-full.px-3.py-2.border.border-gray-600.rounded-lg.text-white.focus:outline-none.focus:border-amber-500.text-sm
                {:style {:background "#1f2937" :min-height "120px" :resize "vertical"}
                 :value (:weaknesses bo) :placeholder "Build weaknesses"
                 :on-change #(rf/dispatch [:update-selected-bo-field :weaknesses (-> % .-target .-value)])}]]
              [:div.border.border-gray-700.rounded-xl.p-4 {:style {:background "rgba(31,41,55,0.3)"}}
               [:span.block.text-xs.font-bold.uppercase.tracking-widest.text-amber-400.mb-2 "Transition Plan"]
               [:textarea.w-full.px-3.py-2.border.border-gray-600.rounded-lg.text-white.focus:outline-none.focus:border-amber-500.text-sm
                {:style {:background "#1f2937" :min-height "120px" :resize "vertical"}
                 :value (:transition_plan bo) :placeholder "What to do if build doesn't win"
                 :on-change #(rf/dispatch [:update-selected-bo-field :transition_plan (-> % .-target .-value)])}]]]

             [:<>
              [:div.grid.grid-cols-1.md:grid-cols-2.gap-4.mb-4
               [info-card-styled {:label "Play Style" :value (:play_style bo) :color :amber :icon "◈"}]
               [info-card-styled {:label "Strategic Goals" :value (:strategic_goals bo) :color :amber :icon "◎" :bullet-list? true}]]
              [:div.grid.grid-cols-1.md:grid-cols-2.gap-4.mb-4
               [info-card-styled {:label "Counters" :value (:counters bo) :color :teal :icon "✕" :bullet-list? true}]
               [info-card-styled {:label "Weaknesses" :value (:weaknesses bo) :color :red :icon "⚠" :bullet-list? true}]]
              [:div.rounded-xl.p-5.border-l-4
               {:style {:background "rgba(17,24,39,0.6)"
                        :border-color "#f59e0b"
                        :border-top "1px solid rgba(75,85,99,0.3)"
                        :border-right "1px solid rgba(75,85,99,0.3)"
                        :border-bottom "1px solid rgba(75,85,99,0.3)"}}
               [:div.flex.items-center.gap-2.mb-3
                [:span.text-amber-400 "↻"]
                [:span.text-xs.font-bold.uppercase.tracking-widest.text-amber-400 "Transition Plan"]]
               [:p.text-sm.text-gray-300.leading-relaxed (or (:transition_plan bo) "—")]]])]]

         ;; ── Embedded video ───────────────────────────────────────────────
         (when (and (not= (:id bo) edit-bo-id) (:youtube_url bo))
           (let [video-id (when-let [url (:youtube_url bo)]
                            (when-let [[_ id] (re-matches #".*(?:v=|youtu\.be/)([a-zA-Z0-9_-]{11}).*" url)]
                              id))]
             (when video-id
               [:div.px-6.mb-8
                [:div.max-w-4xl.mx-auto   ;; <-- centered wrapper
                 [:h2.text-lg.font-bold.text-white.mb-3 "Video Guide"]
                 [:div.relative.w-full.rounded-xl.overflow-hidden.shadow-lg
                  {:style {:aspect-ratio "16/9" :background "#111827"}}
                  [:iframe.w-full.h-full
                   {:src (str "https://www.youtube.com/embed/" video-id)
                    :frameBorder "0"
                    :allow "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    :allowFullScreen true}]]]])))

         ;; ── Build Steps ──────────────────────────────────────────────────
         [:div.px-6.pb-12
          [:div.max-w-4xl.mx-auto   ;; <-- centered wrapper

           [:h2.text-xl.font-bold.text-amber-400.mb-4 "Build Order Steps"]

           (when logged-in?
             [:button.px-3.py-1.5.border.border-gray-600.hover:border-amber-400.text-white.font-medium.rounded-lg.transition-all.mb-4.text-sm
              {:on-click #(rf/dispatch [:add-step-api (:id bo)
                                        {:supply 0 :time-seconds 0 :action-name "" :notes "" :sort-order (count (:steps bo))}])}
              "+ Add Step"])

           [:div.rounded-xl.overflow-hidden
            {:style {:border "1px solid rgba(75,85,99,0.3)"
                     :background "rgba(17,24,39,0.5)"}}

            [:div.grid.gap-4.items-center.px-4.py-3.font-semibold.text-gray-400.text-xs.uppercase.tracking-wider
             {:style {:grid-template-columns "80px 80px 1fr 1fr auto"
                      :border-bottom "1px solid rgba(75,85,99,0.4)"
                      :background "rgba(31,41,55,0.5)"}}
             [:div "Supply"] [:div "Time"] [:div "Action"] [:div "Notes"]
             (when logged-in? [:div])]

            (doall
              (for [step (:steps bo)
                    :let [edit-mode? (and logged-in? (= (:edit-step-id bo) (:id step)))]]
                ^{:key (:id step)}
                [:div.grid.gap-4.items-center.px-4.py-3.transition-colors
                 {:style {:grid-template-columns "80px 80px 1fr 1fr auto"
                          :border-bottom "1px solid rgba(75,85,99,0.25)"
                          :background (if edit-mode? "rgba(120,53,15,0.12)" "transparent")}
                  :class "hover:bg-white/5"}

                 [:div
                  (if edit-mode?
                    [:input.w-full.px-2.py-1.border.border-gray-600.rounded.text-white.focus:outline-none.focus:border-amber-500.text-sm
                     {:style {:background "#1f2937"}
                      :type "number" :value (:supply step)
                      :on-change #(rf/dispatch [:update-step-in-selected-bo (:id step) (assoc step :supply (parse-long (-> % .-target .-value)))])}]
                    [:span.font-bold.text-amber-400.text-lg (:supply step)])]

                 [:div
                  (if edit-mode?
                    [:input.w-full.px-2.py-1.border.border-gray-600.rounded.text-white.focus:outline-none.focus:border-amber-500.text-sm
                     {:style {:background "#1f2937"}
                      :type "text" :value (format-time (:time_seconds step))
                      :on-change #(rf/dispatch [:update-step-in-selected-bo (:id step) (assoc step :time_seconds (parse-time-to-seconds (-> % .-target .-value)))])}]
                    [:span.text-gray-300.font-mono.text-sm (format-time (:time_seconds step))])]

                 [:div.flex.items-center.gap-2
                  (when-let [action-name (:action_name step)]
                    (action-icon action-name))
                  (if edit-mode?
                    [:input.w-full.px-2.py-1.border.border-gray-600.rounded.text-white.focus:outline-none.focus:border-amber-500.text-sm
                     {:style {:background "#1f2937"}
                      :on-change #(rf/dispatch [:update-step-in-selected-bo (:id step) (assoc step :action_name (-> % .-target .-value))])}]
                    #_[:span.text-white.text-sm (:action_name step)])]

                 [:div
                  (if edit-mode?
                    [:input.w-full.px-2.py-1.border.border-gray-600.rounded.text-white.focus:outline-none.focus:border-amber-500.text-sm
                     {:style {:background "#1f2937"}
                      :value (:notes step) :placeholder "notes"
                      :on-change #(rf/dispatch [:update-step-in-selected-bo (:id step) (assoc step :notes (-> % .-target .-value))])}]
                    [:span.text-gray-400.text-sm (or (:notes step) "—")])]

                 (when logged-in?
                   [:div.flex.items-center.gap-2.justify-end
                    (if edit-mode?
                      [:<>
                       [:button.px-2.py-1.bg-green-600.hover:bg-green-700.text-white.text-xs.font-semibold.rounded.transition-all
                        {:on-click #(do (rf/dispatch [:update-step-api (:id step) (dissoc step :edit-step-id)])
                                        (rf/dispatch [:update-selected-bo-field :edit-step-id nil]))}
                        "Save"]
                       [:button.px-2.py-1.bg-gray-700.hover:bg-gray-600.text-white.text-xs.font-semibold.rounded.transition-all
                        {:on-click #(rf/dispatch [:update-selected-bo-field :edit-step-id nil])}
                        "Cancel"]]
                      [:<>
                       [:button.px-2.py-1.bg-gray-700.hover:bg-blue-600.text-white.text-xs.font-semibold.rounded.transition-all
                        {:on-click #(rf/dispatch [:update-selected-bo-field :edit-step-id (:id step)])}
                        "Edit"]
                       [:button.px-2.py-1.bg-gray-700.hover:bg-red-600.text-white.text-xs.font-semibold.rounded.transition-all
                        {:on-click #(rf/dispatch [:set-delete-confirm {:type :step :id (:id step)}])}
                        "Delete"]])])]))]]]]))))

;; ─── Replay Upload Modal ────────────────────────────────────────

(def file-input-ref (r/atom nil))
(def selected-file (r/atom nil))

(defn- replay-upload-modal []
  (let [open? @(rf/subscribe [:upload-modal-open?])
        uploading? @(rf/subscribe [:replay-uploading?])
        error @(rf/subscribe [:upload-error])]
    (when open?
      (let [close! #(do (rf/dispatch [:close-upload-modal])
                        (reset! selected-file nil)
                        (when @file-input-ref (set! (.-value @file-input-ref) "")))]
        [:div.fixed.inset-0.z-50.flex.items-center.justify-center.backdrop-blur-sm.p-4
         {:style {:background "rgba(0,0,0,0.7)"}
          :on-click #(when (= (.-target %) (.-currentTarget %)) (close!))}
         [:div.border.border-gray-700.rounded-xl.p-6.w-full.max-w-4xl.shadow-2xl.relative
          {:style {:background "#111827"}
           :on-click #(.stopPropagation %)}

          ;; Header
          [:button.absolute.top-4.right-4.text-gray-500.hover:text-white.transition-colors
           {:on-click close!} "✕"]
          [:h2.text-xl.font-bold.text-white.mb-1 "Upload Replay"]
          [:p.text-gray-400.text-sm.mb-6
           "Upload a .SC2Replay file and we'll extract the build order automatically."]

          ;; File drop zone
          [:input.hidden
           {:type "file" :accept ".SC2Replay"
            :ref #(reset! file-input-ref %)
            :on-change #(let [files (.. % -target -files)]
                          (when (and files (> (.-length files) 0))
                            (reset! selected-file (aget files 0))))}]
          [:div.border-2.border-dashed.rounded-lg.p-4.text-center.cursor-pointer.transition-colors.mb-6
           {:class (if @selected-file
                     "border-amber-500 bg-amber-900/10"
                     "border-gray-700 bg-gray-800/30 hover:border-amber-500/50")
            :on-drag-over #(.preventDefault %)
            :on-drop #(do (.preventDefault %)
                          (let [files (.. % -dataTransfer -files)]
                            (when (and files (> (.-length files) 0))
                              (reset! selected-file (aget files 0)))))}
           [:button.px-4.py-2.bg-gray-700.hover:bg-gray-600.text-white.font-medium.rounded-lg.transition-all
            {:on-click #(when @file-input-ref (.click @file-input-ref))}
            "Browse Files"]
           (if @selected-file
             [:p.mt-2.text-sm.text-amber-400.font-mono (.-name @selected-file)]
             [:p.mt-2.text-sm.text-gray-500 "or drag & drop a .SC2Replay file here"])]

          ;; Two-column grid
          [:div.grid.gap-4
           {:style {:grid-template-columns "1fr 1fr"}}

           ;; Left column
           [:div.flex.flex-col.gap-4
            [:div.flex.flex-col.gap-1
             [:label.text-sm.font-medium.text-gray-300 "Build Order Name"]
             [:input.w-full.px-3.py-2.border.border-gray-700.rounded-lg.text-white.placeholder-gray-500.focus:outline-none.focus:border-amber-500
              {:style {:background "#1f2937"}
               :type "text" :placeholder "e.g. 14 Pylon Expand"
               :value @(rf/subscribe [:upload-name])
               :on-change #(rf/dispatch [:set-upload-name (-> % .-target .-value)])}]]
            [:div.flex.flex-col.gap-1
             [:label.text-sm.font-medium.text-gray-300 "Author"]
             [:input.w-full.px-3.py-2.border.border-gray-700.rounded-lg.text-white.placeholder-gray-500.focus:outline-none.focus:border-amber-500
              {:style {:background "#1f2937"}
               :type "text" :placeholder "Author name"
               :value @(rf/subscribe [:upload-author])
               :on-change #(rf/dispatch [:set-upload-author (-> % .-target .-value)])}]]
            [:div.flex.flex-col.gap-1
             [:label.text-sm.font-medium.text-gray-300 "Play Style"]
             [:input.w-full.px-3.py-2.border.border-gray-700.rounded-lg.text-white.placeholder-gray-500.focus:outline-none.focus:border-amber-500
              {:style {:background "#1f2937"}
               :type "text" :placeholder "e.g. Protoss, Terran, Zerg"
               :value @(rf/subscribe [:upload-playstyle])
               :on-change #(rf/dispatch [:set-upload-playstyle (-> % .-target .-value)])}]]
            [:div.flex.flex-col.gap-1
             [:label.text-sm.font-medium.text-gray-300 "YouTube URL (optional)"]
             [:input.w-full.px-3.py-2.border.border-gray-700.rounded-lg.text-white.placeholder-gray-500.focus:outline-none.focus:border-amber-500
              {:style {:background "#1f2937"}
               :type "text" :placeholder "https://www.youtube.com/watch?v=..."
               :value @(rf/subscribe [:upload-youtube-url])
               :on-change #(rf/dispatch [:set-upload-youtube-url (-> % .-target .-value)])}]]]

           ;; Right column
           [:div.flex.flex-col.gap-4
            [:div.flex.flex-col.gap-1
             [:label.text-sm.font-medium.text-gray-300 "Strategic Goals"]
             [:textarea.w-full.px-3.py-2.border.border-gray-700.rounded-lg.text-white.placeholder-gray-500.focus:outline-none.focus:border-amber-500
              {:style {:background "#1f2937" :min-height "80px"}
               :placeholder "Main goals of this build"
               :value @(rf/subscribe [:upload-strategic-goals])
               :on-change #(rf/dispatch [:set-upload-strategic-goals (-> % .-target .-value)])}]]
            [:div.flex.flex-col.gap-1
             [:label.text-sm.font-medium.text-gray-300 "Counters"]
             [:textarea.w-full.px-3.py-2.border.border-gray-700.rounded-lg.text-white.placeholder-gray-500.focus:outline-none.focus:border-amber-500
              {:style {:background "#1f2937" :min-height "80px"}
               :placeholder "What this build counters"
               :value @(rf/subscribe [:upload-counters])
               :on-change #(rf/dispatch [:set-upload-counters (-> % .-target .-value)])}]]
            [:div.flex.flex-col.gap-1
             [:label.text-sm.font-medium.text-gray-300 "Weaknesses"]
             [:textarea.w-full.px-3.py-2.border.border-gray-700.rounded-lg.text-white.placeholder-gray-500.focus:outline-none.focus:border-amber-500
              {:style {:background "#1f2937" :min-height "80px"}
               :placeholder "Build weaknesses"
               :value @(rf/subscribe [:upload-weaknesses])
               :on-change #(rf/dispatch [:set-upload-weaknesses (-> % .-target .-value)])}]]
            [:div.flex.flex-col.gap-1
             [:label.text-sm.font-medium.text-gray-300 "Transition Plan"]
             [:textarea.w-full.px-3.py-2.border.border-gray-700.rounded-lg.text-white.placeholder-gray-500.focus:outline-none.focus:border-amber-500
              {:style {:background "#1f2937" :min-height "80px"}
               :placeholder "What to do if build doesn't win"
               :value @(rf/subscribe [:upload-transition-plan])
               :on-change #(rf/dispatch [:set-upload-transition-plan (-> % .-target .-value)])}]]]]

          ;; Error
          (when error
            [:div.text-red-400.text-sm.border.border-red-800.rounded-lg.p-3.mt-4
             {:style {:background "rgba(127,29,29,0.2)"}}
             error])

          ;; Uploading indicator
          (when uploading?
            [:div.text-amber-400.text-sm.mt-4.flex.items-center.gap-2
             "⏳ Parsing replay... This may take a moment."])

          ;; Action buttons
          [:div.flex.gap-3.mt-6
           [:button.flex-1.py-3.bg-gray-700.hover:bg-gray-600.text-white.font-semibold.rounded-lg.transition-all
            {:type "button" :on-click close!}
            "Cancel"]
           [:button.flex-1.py-3.bg-amber-500.hover:bg-amber-600.text-white.font-semibold.rounded-lg.transition-all.shadow-md
            {:type "button"
             :disabled uploading?
             :class (when uploading? "opacity-50 cursor-not-allowed")
             :on-click #(if-not @selected-file
                          (rf/dispatch [:set-upload-error "Please select a replay file"])
                          (do
                            (rf/dispatch [:set-replay-uploading true])
                            (rf/dispatch [:set-upload-error nil])
                            (let [file @selected-file
                                  bo-meta {:name @(rf/subscribe [:upload-name])
                                           :author @(rf/subscribe [:upload-author])
                                           :play_style @(rf/subscribe [:upload-playstyle])
                                           :strategic_goals @(rf/subscribe [:upload-strategic-goals])
                                           :counters @(rf/subscribe [:upload-counters])
                                           :weaknesses @(rf/subscribe [:upload-weaknesses])
                                           :transition_plan @(rf/subscribe [:upload-transition-plan])
                                           :youtube_url @(rf/subscribe [:upload-youtube-url])}]
                              (rf/dispatch [:upload-replay file bo-meta]))))}
            (if uploading? "Parsing..." "Upload & Parse")]]]]))))

;; ─── Delete Confirm Modal ───────────────────────────────────────
;; FIX: The confirm/cancel button div was outside the outer container div.

(defn- delete-confirm-modal []
  (let [confirm @(rf/subscribe [:delete-confirm])
        logged-in? @(rf/subscribe [:is-logged-in?])]
    (when (and confirm logged-in?)
      [:div.fixed.inset-0.z-50.flex.items-center.justify-center.backdrop-blur-sm.p-4
       {:style {:background "rgba(0,0,0,0.7)"}
        :on-click #(rf/dispatch [:clear-delete-confirm])}
       [:div.border.rounded-xl.p-6.w-full.max-w-sm.shadow-2xl.relative
        {:style {:background "#111827" :border-color "rgba(153,27,27,0.5)"}
         :on-click #(.stopPropagation %)}
        [:h2.text-xl.font-bold.text-white.mb-3 "Confirm Delete"]
        [:p.text-gray-400.text-sm.mb-6.leading-relaxed
         (if (= (:type confirm) :bo)
           "Are you sure you want to delete this build order? This cannot be undone."
           "Are you sure you want to delete this step?")]
        [:div.flex.justify-end.gap-3
         [:button.px-4.py-2.border.border-gray-600.hover:border-gray-500.text-white.font-medium.rounded-lg.transition-all
          {:on-click #(rf/dispatch [:clear-delete-confirm])}
          "Cancel"]
         [:button.px-4.py-2.bg-red-600.hover:bg-red-700.text-white.font-semibold.rounded-lg.transition-all
          {:on-click #(do
                        (if (= (:type confirm) :bo)
                          (rf/dispatch [:delete-build-order (:id confirm)])
                          (rf/dispatch [:delete-step-api (:id confirm)]))
                        (rf/dispatch [:clear-delete-confirm]))}
          "Delete"]]]])))

;; ─── App Root with Routing ──────────────────────────────────────

(defn- nav-pages [route]
  (case route
    :build-order-detail [build-order-detail]
    :build-orders-list [build-order-list]
    [:div.flex-grow.text-white {:style {:background "#030712"}}
     [hero]
     [divider]
     [cards-section]
     [stats-strip]
     [testimonial-section]]))

(defn app []
  (let [active-nav @(rf/subscribe [:current-route])]
    [:div.flex.flex-col.min-h-screen.text-white.font-sans
     {:style {:background "#030712"}}
     [header]
     [:div.flex-grow.relative.z-10
      [nav-pages active-nav]]
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