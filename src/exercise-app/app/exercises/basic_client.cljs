(ns app.exercises.basic-client
  (:require [om.dom :as dom]
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]
            [om.next :as om :refer [defui]]))

(defui ^:once Root
  static uc/InitialAppState
  (initial-state [this params] {})
  Object
  (render [this]
    (let [{:keys [ui/react-key]} (om/props this)]
      (dom/div #js {:key react-key} "TODO"))))
