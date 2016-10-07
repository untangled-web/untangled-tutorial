(ns app.ui
  (:require [om.dom :as dom]
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]
            [app.exercises.advanced-server-topics :as adv]
            [app.exercises.basic-client :as basic]
            [om.next :as om :refer [defui]]))

(defonce app (atom (uc/new-untangled-client)))

;; TODO: Put the correct UI Root here and RELOAD the browser page:
(swap! app uc/mount basic/Root "app")

