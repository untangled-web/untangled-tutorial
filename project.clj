(defproject untangled-devguide "0.1.0-SNAPSHOT"
  :description "A Developers Guide for the Untangled Web Framework"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.456"]
                 [org.omcljs/om "1.0.0-alpha47"]
                 [com.datomic/datomic-free "0.9.5404" :exclusions [org.clojure/tools.cli]]
                 [com.google.guava/guava "20.0"]
                 [commons-codec "1.10"]
                 [commons-io "2.5"]
                 [org.clojure/core.async "0.2.391"]
                 [http-kit "2.2.0"]
                 [bidi "2.0.16"]
                 [clj-time "0.11.0"]
                 [lein-doo "0.1.7" :scope "test"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/core.cache "0.6.5"]
                 [navis/untangled-client "0.7.0"]
                 [cljsjs/d3 "3.5.7-1"]
                 [cljsjs/victory "0.9.0-0"]
                 [ring/ring-codec "1.0.1"]
                 [ring/ring-core "1.5.0"]
                 [navis/untangled-server "0.6.2"]
                 [navis/untangled-spec "0.3.9" :scope "test"]
                 [navis/untangled-datomic "0.4.11"]]

  ; server source paths
  :source-paths ["src/server" "src/shared" "test/server" "test/shared" "src/devguide"]
  :test-paths ["test/server" "test/shared"]

  :plugins [[lein-cljsbuild "1.1.5"]

            ; Run server side tests with spec output
            [com.jakemccrary/lein-test-refresh "0.18.0"]

            ; Used for running CI (command line) client tests
            [lein-doo "0.1.7" :exclusions [org.clojure/tools.reader]]

            ; Internationalization extraction/generation
            [navis/untangled-lein-i18n "0.2.0-SNAPSHOT" :exclusions [org.clojure/clojure org.apache.maven.wagon/wagon-provider-api
                                                                     org.clojure/tools.cli org.codehaus.plexus/plexus-utils]]]

  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :doo {:build "automated-tests"
        :paths {:karma "node_modules/karma/bin/karma"}}

  ; Configure test-refresh to show custom spec outline
  :test-refresh {:report       untangled-spec.reporters.terminal/untangled-report
                 :changes-only true
                 :with-repl    true}

  ; i18n lein plugin config
  :untangled-i18n {:translation-namespace "app.i18n"
                   :source-folder         "src/devguide"
                   :translation-build     "i18n"
                   :po-files              "msgs"
                   :production-build      "pages"}

  :cljsbuild {:builds [{:id           "test"
                        :figwheel     true
                        :source-paths ["src/shared" "test/client" "test/shared"]
                        :compiler     {:main           app.suite
                                       :asset-path     "js/specs"
                                       :output-to      "resources/public/js/specs.js"
                                       :output-dir     "resources/public/js/specs"
                                       :parallel-build true
                                       }}
                       {:id           "i18n"
                        :source-paths ["src/devguide" "src/shared"]
                        :compiler     {:output-to     "resources/public/js/i18n.js"
                                       :output-dir    "resources/public/js/i18n"
                                       :main          untanged-devguide.guide
                                       :optimizations :whitespace
                                       :foreign-libs  [{:provides ["cljsjs.codemirror.addons.closebrackets"]
                                                        :requires ["cljsjs.codemirror"]
                                                        :file     "resources/public/codemirror/closebrackets-min.js"}
                                                       {:provides ["cljsjs.codemirror.addons.matchbrackets"]
                                                        :requires ["cljsjs.codemirror"]
                                                        :file     "resources/public/codemirror/matchbrackets-min.js"}]}}
                       {:id           "app"
                        :figwheel     true
                        :source-paths ["src/exercise-app" "dev/client"]
                        :compiler     {:main           cljs.user
                                       :asset-path     "js/exercise-app"
                                       :output-to      "resources/public/js/exercise-app.js"
                                       :output-dir     "resources/public/js/exercise-app"
                                       :parallel-build true
                                       }}
                       {:id           "automated-tests"
                        :source-paths ["test/shared" "test/client" "src/shared"]
                        :compiler     {:output-to     "resources/private/js/unit-tests.js"
                                       :main          app.all-tests
                                       :asset-path    "js"
                                       :output-dir    "resources/private/js"
                                       :optimizations :none
                                       }}
                       {:id           "devguide"
                        :figwheel     {:devcards true}
                        :source-paths ["src/devguide" "src/shared"]
                        :compiler     {
                                       :main           untangled-devguide.guide
                                       :asset-path     "js/devguide"
                                       :output-to      "resources/public/js/devguide.js"
                                       :output-dir     "resources/public/js/devguide"
                                       :parallel-build true
                                       :foreign-libs   [{:provides ["cljsjs.codemirror.addons.closebrackets"]
                                                         :requires ["cljsjs.codemirror"]
                                                         :file     "resources/public/codemirror/closebrackets-min.js"}
                                                        {:provides ["cljsjs.codemirror.addons.matchbrackets"]
                                                         :requires ["cljsjs.codemirror"]
                                                         :file     "resources/public/codemirror/matchbrackets-min.js"}]}}
                       {:id           "pages"
                        :source-paths ["src/devguide" "src/pages" "src/shared"]
                        :compiler     {:devcards      true
                                       :asset-path    "js"
                                       :output-dir    "resources/public/js"
                                       :output-to     "resources/public/js/pages.js"
                                       :optimizations :advanced
                                       :foreign-libs  [{:provides ["cljsjs.codemirror.addons.closebrackets"]
                                                        :requires ["cljsjs.codemirror"]
                                                        :file     "resources/public/codemirror/closebrackets-min.js"}
                                                       {:provides ["cljsjs.codemirror.addons.matchbrackets"]
                                                        :requires ["cljsjs.codemirror"]
                                                        :file     "resources/public/codemirror/matchbrackets-min.js"}]}}]}

  :profiles {
             :dev {
                   :dependencies [[devcards "0.2.2"]
                                  [figwheel-sidecar "0.5.9"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [binaryage/devtools "0.6.1"]
                                  [cljsjs/codemirror "5.8.0-0"]]
                   :source-paths ["dev/server" "src/server" "src/shared"]
                   :repl-options {:init-ns          user
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                                  :port             7001}}})
