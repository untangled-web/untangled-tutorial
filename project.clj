(defproject untangled-tutorial "0.1.0-SNAPSHOT"
  :description "A Tutorial for the Untangled Web Framework"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.omcljs/om "1.0.0-alpha31"]
                 [com.datomic/datomic-free "0.9.5350" :exclusions [org.clojure/tools.cli]]
                 [commons-codec "1.10"]
                 [lein-doo "0.1.6" :scope "test"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [navis/untangled-client "0.4.7-SNAPSHOT"]
                 [cljsjs/d3 "3.5.7-1"]
                 [navis/untangled-server "0.4.5"]
                 [navis/untangled-spec "0.3.5"]
                 [navis/untangled-datomic "0.4.4"]]

  ; server source paths
  :source-paths ["src/server" "src/shared" "test/server" "test/shared"]
  :test-paths ["test/server" "test/shared"]

  :plugins [[lein-cljsbuild "1.1.2"]

            ; Run server side tests with spec output
            [com.jakemccrary/lein-test-refresh "0.14.0"]

            ; Used for running CI (command line) client tests
            [lein-doo "0.1.6" :exclusions [org.clojure/tools.reader]]

            ; Internationalization extraction/generation
            [navis/untangled-lein-i18n "0.1.2" :exclusions [org.apache.maven.wagon/wagon-provider-api org.codehaus.plexus/plexus-utils org.clojure/tools.cli]]]

  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :doo {:build "automated-tests"
        :paths {:karma "node_modules/karma/bin/karma"}}

  ; Configure test-refresh to show custom spec outline
  :test-refresh {:report       untangled-spec.reporters.terminal/untangled-report
                 :changes-only true
                 :with-repl    true}

  ; i18n lein plugin config
  :untangled-i18n {:default-locale        "en-US"
                   :translation-namespace "app.i18n"
                   :source-folder         "src/client"
                   :target-build          "i18n"}

  :cljsbuild {:builds
              [{:id           "client"
                :figwheel     true
                :source-paths ["dev/client" "src/client" "src/shared"]
                :compiler     {:main                 cljs.user
                               :asset-path           "js/main"
                               :output-to            "resources/public/js/main.js"
                               :output-dir           "resources/public/js/main"
                               :recompile-dependents true
                               :verbose              false}}
               {:id           "test"
                :figwheel     true
                :source-paths ["src/client" "src/shared" "test/client" "test/shared"]
                :compiler     {:main                 app.suite
                               :asset-path           "js/specs"
                               :output-to            "resources/public/js/specs.js"
                               :output-dir           "resources/public/js/specs"
                               :recompile-dependents true
                               }}
               {:id           "automated-tests"
                :source-paths ["test/client" "test/shared" "src/client" "src/shared"]
                :compiler     {:output-to     "resources/private/js/unit-tests.js"
                               :main          app.all-tests
                               :asset-path    "js"
                               :output-dir    "resources/private/js"
                               :optimizations :none
                               }}
               {:id           "tutorial"
                :figwheel     {:devcards true}
                :source-paths ["src/tutorial" "src/shared"]
                :compiler     {
                               :main                 untangled-tutorial.tutorial
                               :source-map-timestamp true
                               :asset-path           "js/tutorial"
                               :output-to            "resources/public/js/tutorial.js"
                               :output-dir           "resources/public/js/tutorial"
                               :recompile-dependents true
                               :verbose              false
                               :foreign-libs         [{:provides ["cljsjs.codemirror.addons.closebrackets"]
                                                       :requires ["cljsjs.codemirror"]
                                                       :file     "resources/public/codemirror/closebrackets-min.js"}
                                                      {:provides ["cljsjs.codemirror.addons.matchbrackets"]
                                                       :requires ["cljsjs.codemirror"]
                                                       :file     "resources/public/codemirror/matchbrackets-min.js"}]}}
               {:id           "pages"
                :source-paths ["src/tutorial" "src/pages" "src/shared"]
                :compiler     {
                               :main          core
                               :devcards      true
                               :asset-path    "js/pages"
                               :output-to     "resources/public/js/pages.js"
                               :output-dir    "resources/public/js/pages"
                               :optimizations :advanced
                               :foreign-libs  [{:provides ["cljsjs.codemirror.addons.closebrackets"]
                                                :requires ["cljsjs.codemirror"]
                                                :file     "resources/public/codemirror/closebrackets-min.js"}
                                               {:provides ["cljsjs.codemirror.addons.matchbrackets"]
                                                :requires ["cljsjs.codemirror"]
                                                :file     "resources/public/codemirror/matchbrackets-min.js"}]}}]}

  :profiles {
             :dev {
                   :dependencies [[devcards "0.2.1-6" :exclusions [org.omcljs/om]]
                                  [figwheel-sidecar "0.5.0-6" :exclusions [ring/ring-core commons-fileupload clj-time joda-time]]
                                  [binaryage/devtools "0.5.2" :exclusions [environ]]
                                  [cljsjs/codemirror "5.8.0-0"]]
                   :source-paths ["dev/server" "src/server" "src/shared"]
                   :repl-options {:init-ns user
                                  :port    7001}
                   }
             }
  )
