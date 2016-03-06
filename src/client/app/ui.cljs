(ns app.ui
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [untangled.i18n :refer-macros [tr trf]]
            [untangled.client.mutations :as m]
            [untangled.client.data-fetch :as df]))

(defui ^:once Item
  static om/IQuery
  (query [this] [:ui/checked :id :label])
  static om/Ident
  (ident [this props] [:item/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id label ui/checked]} (om/props this)]
      (dom/div #js {:className "item"}
               (dom/input #js {:type    "checkbox"
                               :checked checked
                               :onClick #(m/toggle! this :ui/checked)}) label))))

(def ui-item (om/factory Item {:keyfn :id}))

(defui ^:once Root
  static om/IQuery
  (query [this] [{:item (om/get-query Item)}])
  Object
  (render [this]
    (let [{:keys [item]} (om/props this)]
      (ui-item item))))


