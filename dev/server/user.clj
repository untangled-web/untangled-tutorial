(ns user
  (:require
    [clojure.pprint :refer (pprint)]
    [clojure.stacktrace :refer (print-stack-trace)]
    [clojure.tools.namespace.repl :refer [disable-reload! refresh clear set-refresh-dirs]]
    [clojure.tools.nrepl.server :as nrepl]
    [com.stuartsierra.component :as component]
    [datomic-helpers :refer [to-transaction to-schema-transaction ext]]
    [datomic.api :as d]
    [figwheel-sidecar.system :as sys]
    [taoensso.timbre :refer [info set-level!]]
    [untangled.datomic.schema :refer [dump-schema dump-entity]]))

;;FIGWHEEL
(def figwheel-config (sys/fetch-config))
(def figwheel (atom nil))

(defn start-figwheel
  "Start Figwheel on the given builds, or defaults to build-ids in `figwheel-config`."
  ([]
   (let [props (System/getProperties)
         all-builds (->> figwheel-config :data :all-builds (mapv :id))]
     (start-figwheel (keys (select-keys props all-builds)))))
  ([build-ids]
   (let [default-build-ids (-> figwheel-config :data :build-ids)
         build-ids (if (empty? build-ids) default-build-ids build-ids)
         preferred-config (assoc-in figwheel-config [:data :build-ids] build-ids)]
     (reset! figwheel (component/system-map :figwheel-system (sys/figwheel-system preferred-config)))
     (println "STARTING FIGWHEEL ON BUILDS: " build-ids)
     (swap! figwheel component/start)
     (sys/cljs-repl (:figwheel-system @figwheel)))))
