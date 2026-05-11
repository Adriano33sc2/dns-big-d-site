(defn get-version [rev]
  (let [^java.util.Calendar calendar (java.util.Calendar/getInstance)
        week (.get calendar java.util.Calendar/WEEK_OF_YEAR)
        year (.get calendar java.util.Calendar/YEAR)]
    (str year "." week "." rev)))

(def current-week-revision 1)

(defproject dns-big-d-site (get-version current-week-revision)
  :main ^:skip-aot dns-big-d-site.backend.core
  :source-paths ["src" "backend/src"]
  :resource-paths ["public" "backend/resources"]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.slf4j/slf4j-api "2.0.7"]
                 [org.slf4j/slf4j-simple "2.0.7"]
                 [binaryage/devtools "0.9.10"]
                 [reagent "2.0.0-alpha2"]
                 [re-frame "1.4.3"]
                 [bidi "2.1.5"]
                 [kibu/pushy "0.3.8"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [cljs-ajax "0.8.4"]
                 [thheller/shadow-cljs "2.25.8" :exclusions [nrepl
                                                             com.cognitect/transit-clj
                                                             com.cognitect/transit-cljs
                                                             com.cognitect/transit-java
                                                             com.cognitect/transit-js
                                                             commons-codec]]
                 [ring/ring-core "1.15.4"]
                 [day8.re-frame/http-fx "0.2.4"]
                 [ring/ring-jetty-adapter "1.15.4"]
                 [ring-cors/ring-cors "0.1.13"]
                 [compojure "1.7.2"]
                 [ring/ring-json "0.5.1"]
                 [ring/ring-defaults "0.7.0"]
                 [buddy/buddy-hashers "2.0.167"]
                 [com.lambdaworks/scrypt "1.4.0"]
                 [com.github.seancorfield/next.jdbc "1.3.1093"]
                 [org.postgresql/postgresql "42.7.1"]]
                 :profiles {:dev {:dependencies [[day8.re-frame/tracing "0.6.2"]
                                                 [day8.re-frame/re-frame-10x "1.10.1"]]}
                            :uberjar {:aot :all}})
