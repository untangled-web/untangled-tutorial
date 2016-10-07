(ns untangled-tutorial.putting-together.soln-ex-2
  (:require-macros [cljs.test :refer [is]]
                   [untangled-tutorial.tutmacros :refer [untangled-app]])
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [devcards.core :as dc :refer-macros [defcard defcard-doc]]
            [untangled.client.core :as uc]
            [untangled.client.data-fetch :as df]
            [untangled.client.mutations :as m]))

(defmethod m/mutate 'pit-soln/toggle-done [{:keys [state]} k {:keys [id]}]
  {:action (fn [] (swap! state update-in [:items/by-id id :item/done] not))})

(defui ^:once TodoItem
  static uc/InitialAppState
  (initial-state [this {:keys [id label complete]}] {:item/id id :item/label label :item/done complete})
  static om/IQuery
  (query [this] [:item/id :item/label :item/done])
  static om/Ident
  (ident [this props] [:items/by-id (:item/id props)])
  Object
  (render [this]
    (let [{:keys [item/id item/label item/done]} (om/props this)]
      (dom/li nil
              (dom/input #js {:type "checkbox" :onChange #(om/transact! this `[(pit-soln/toggle-done ~{:id id})]) :checked done})
              label))))

(def ui-item (om/factory TodoItem))

(defui ^:once ItemList
  static uc/InitialAppState
  (initial-state [this params] {:list/title "My List" :list/items [(uc/initial-state TodoItem {:id 1 :label "A" :complete false})
                                                                   (uc/initial-state TodoItem {:id 2 :label "B" :complete false})
                                                                   (uc/initial-state TodoItem {:id 3 :label "C" :complete true})
                                                                   (uc/initial-state TodoItem {:id 4 :label "D" :complete false})]})
  static om/IQuery
  (query [this] [:list/title {:list/items (om/get-query TodoItem)}])
  static om/Ident
  (ident [this props] [:lists/by-title (:list/title props)])
  Object
  (render [this]
    (let [{:keys [list/title list/items]} (om/props this)]
      (dom/div nil
        (dom/h4 nil title)
        (dom/ol nil (map ui-item items))))))

(def ui-item-list (om/factory ItemList))

(defui ^:once TodoList
  static uc/InitialAppState
  (initial-state [this params] {:ui/react-key "A" :item-list (uc/initial-state ItemList nil)})
  static om/IQuery
  (query [this] [:ui/react-key {:item-list (om/get-query ItemList)}])
  Object
  (render [this]
    (let [{:keys [ui/react-key item-list]} (om/props this)]
      (dom/div #js {:key react-key}
        (ui-item-list item-list)))))
