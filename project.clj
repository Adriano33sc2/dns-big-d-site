(defproject dns-big-d-site "0.1.0-SNAPSHOT"
  :source-paths ["src"]
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
                                                             commons-codec]]]
  :profiles {:dev {:dependencies [[day8.re-frame/tracing "0.6.2"]
                                  [day8.re-frame/re-frame-10x "1.10.1"]]}})
