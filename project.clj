(defproject whereis "0.1.0-SNAPSHOT"

  :description "location tracking"
  :url "https://github.com/vigevenoj/whereis"

  :dependencies [[baking-soda "0.2.0" :exclusions [cljsjs/react-bootstrap]]
                 [buddy "2.0.0"]
                 [clj-time "0.15.2"]
                 [cljs-ajax "0.8.0"]
                 [cljsjs/react-popper "1.0.2-0"]
                 [cljsjs/react-transition-group "4.3.0-0"]
                 [clojurewerkz/machine_head "1.0.0" :exclusions [com.google.guava/guava]]
                 [com.cognitect/transit-java "1.0.343"]
                 [com.fasterxml.jackson.core/jackson-core "2.10.3"]
                 [com.fasterxml.jackson.datatype/jackson-datatype-joda "2.10.3"]
                 [compojure "1.6.1"]
                 [cprop "0.1.16"]
                 [funcool/struct "1.4.0"]
                 [luminus-immutant "0.2.5"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [markdown-clj "1.10.2"]
                 [metosin/jsonista "0.2.5"]
                 [metosin/muuntaja "0.6.6"]
                 [metosin/reitit "0.4.2"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.6.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597" :scope "provided"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.0.0"]
                 [org.webjars.bower/tether "2.0.0-beta.5"]
                 [org.webjars/bootstrap "4.4.1-1"]
                 [org.webjars/font-awesome "5.12.0"]
                 [org.webjars/webjars-locator "0.39"]
                 [re-frame "0.12.0"]
                 [reagent "0.10.0"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.8.0"]
                 [ring/ring-defaults "0.3.2"]
                 [secretary "1.2.3"]
                 [selmer "1.12.18"]]

  :min-lein-version "2.0.0"
  
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot whereis.core

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-immutant "2.1.0"]]
  :clean-targets ^{:protect false}
  [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :figwheel
  {:http-server-root "public"
   :server-logfile "log/figwheel-logfile.log"
   :nrepl-port 7002
   :css-dirs ["resources/public/css"]
   :nrepl-middleware
   [cider/wrap-cljs-repl cider.piggieback/wrap-cljs-repl]}
  

  :profiles
  {:prod { :dependencies [[day8.re-frame/tracing-stubs "0.5.3"]]}
   :uberjar {:omit-source true
             :dependencies [[day8.re-frame/tracing-stubs "0.5.3"]]
             :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
             :cljsbuild
             {:builds
              {:min
               {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                :compiler
                {:output-dir "target/cljsbuild/public/js"
                 :output-to "target/cljsbuild/public/js/app.js"
                 :source-map "target/cljsbuild/public/js/app.js.map"
                 :optimizations :advanced
                 :pretty-print false
                 :infer-externs true
                 :closure-warnings
                 {:externs-validation :off :non-standard-jsdoc :off}
                 :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true
                                    "day8.re_frame.tracing.trace_enabled_QMARK_" true}
                 :externs ["react/externs/react.js"]}}}}
             
             
             :aot :all
             :uberjar-name "whereis.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]
;-Djavax.net.ssl.trustStore=/Users/vigevenoj/code/LocationServer/src/main/resources/sbe-mqtt.keystore -Djavax.net.ssl.trustStorePassword=changeit
   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[binaryage/devtools "1.0.0"]
                                 [cheshire "5.10.0"]
                                 [cider/piggieback "0.4.2"]
                                 [day8.re-frame/re-frame-10x "0.6.0"]
                                 [day8.re-frame/tracing "0.5.3"]
                                 [doo "0.1.11"]
                                 [expound "0.8.4"]
                                 [figwheel-sidecar "0.5.19"]
                                 [pjstadig/humane-test-output "0.10.0"]
                                 [prone "2020-01-17"]
                                 [ring/ring-devel "1.8.0"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.23.0"]
                                 [lein-doo "0.1.10"]
                                 [lein-figwheel "0.5.18"]]
                  :cljsbuild
                  {:builds
                   {:app
                    {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                     :figwheel {:on-jsload "whereis.core/mount-components"}
                     :compiler
                     {:main "whereis.app"
                      :asset-path "/js/out"
                      :output-to "target/cljsbuild/public/js/app.js"
                      :output-dir "target/cljsbuild/public/js/out"
                      :source-map true
                      :optimizations :none
                      :pretty-print true
                      :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}
                      :preloads [day8.re-frame-10x.preload]}}}}
                  
                  
                  
                  :doo {:build "test"}
                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]
                  :cljsbuild
                  {:builds
                   {:test
                    {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
                     :compiler
                     {:output-to "target/test.js"
                      :main "whereis.doo-runner"
                      :optimizations :whitespace
                      :pretty-print true}}}}
                  
                  }
   :profiles/dev {}
   :profiles/test {}})
