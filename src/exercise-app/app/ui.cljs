(ns app.ui
  (:require [om.dom :as dom]
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]
            [om.next :as om :refer [defui]]))

(defmethod m/mutate 'exercise5/trigger [e k p]
  ; TODO: Note how we're triggering the remote:
  {:remote true})

(defui Root
  static uc/InitialAppState
  (initial-state [this params] {})
  Object
  (render [this]
    (dom/button #js {:onClick #(om/transact! this '[(exercise5/trigger)])} "Click Me")))

(defonce app (atom (uc/new-untangled-client)))
