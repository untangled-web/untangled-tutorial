(ns cljs.user
  (:require
    [untangled.client.core :as core]
    [cljs.pprint :refer [pprint]]
    [devtools.core :as devtools]
    [untangled.client.impl.util :as util]
    [app.ui :as ui]
    [om.next :as om]))

(enable-console-print!)
(devtools/install!)

(defn app-state [] (om/app-state (:reconciler @ui/app)))

(def log-app-state (partial util/log-app-state ui/app))
