(ns untangled-tutorial.A-Quick-Tour
  (:require-macros [cljs.test :refer [is]]
                   [untangled-tutorial.tutmacros :refer [untangled-app]])
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [untangled.client.core :as uc]
            [untangled.client.impl.network :as un]
            [devcards.core :as dc :refer-macros [defcard defcard-doc]]
            [untangled.client.mutations :as m]
            [untangled.client.logging :as log]
            [untangled.client.data-fetch :as df]))

(defui ^:once Counter
  static uc/InitialAppState
  (uc/initial-state [this {:keys [id start]
                           :or   {id 1 start 1}
                           :as   params}] {:counter/id id :counter/n start})
  static om/IQuery
  (query [this] [:counter/id :counter/n])
  static om/Ident
  (ident [this props] [:counter/by-id (:counter/id props)])
  Object
  (render [this]
    (let [{:keys [counter/id counter/n]} (om/props this)
          onClick (om/get-computed this :onClick)]
      (dom/div #js {:className "counter"}
        (dom/span #js {:className "counter-label"}
          (str "Current count for counter " id ":  "))
        (dom/span #js {:className "counter-value"} n)
        (dom/button #js {:onClick #(onClick id)} "Increment")))))

(def ui-counter (om/factory Counter {:keyfn :counter/id}))

(defmethod m/mutate 'counter/inc [{:keys [state] :as env} k {:keys [id] :as params}]
  {:remote true
   :action (fn [] (swap! state update-in [:counter/by-id id :counter/n] inc))})

(defmethod m/mutate 'add-counters-to-panel [{:keys [state] :as env} k {:keys [id] :as params}]
  {:action (fn []
             (let [ident-list (get @state :all-counters)]
               (js/console.log :idents ident-list :state @state)
               (swap! state update-in [:panels/by-kw :counter] assoc :counters ident-list)))})

(defui ^:once CounterPanel
  static uc/InitialAppState
  (uc/initial-state [this params]
    {:counters [(uc/initial-state Counter {:id 1 :start 1})]})
  static om/IQuery
  (query [this] [{:counters (om/get-query Counter)}])
  static om/Ident
  (ident [this props] [:panels/by-kw :counter])
  Object
  (render [this]
    (let [{:keys [counters]} (om/props this)
          click-callback (fn [id] (om/transact! this `[(counter/inc {:id ~id}) :counter-sum]))]
      (dom/div nil
        (dom/style nil ".counter { width: 400px; padding-bottom: 20px; } button { margin-left: 10px; }")
        (map #(ui-counter (om/computed % {:onClick click-callback})) counters)))))

(def ui-counter-panel (om/factory CounterPanel))

(defui ^:once CounterSum
  static uc/InitialAppState
  (uc/initial-state [this params] {})
  static om/IQuery
  (query [this] [[:counter/by-id '_]])
  Object
  (render [this]
    (let [{:keys [counter/by-id]} (om/props this)
          total (reduce (fn [total c] (+ total (:counter/n c))) 0 (vals by-id))]
      (dom/div nil
        (str "Grand total: " total)))))

(def ui-counter-sum (om/factory CounterSum))

(defui ^:once Root
  static uc/InitialAppState
  (uc/initial-state [this params]
    {:panel (uc/initial-state CounterPanel {})})
  static om/IQuery
  (query [this] [:ui/loading-data
                 {:panel (om/get-query CounterPanel)}
                 {:counter-sum (om/get-query CounterSum)}])
  Object
  (render [this]
    (let [{:keys [ui/loading-data counter-sum panel]} (om/props this)]
      (dom/div nil
        (when loading-data
          (dom/span #js {:style #js {:float "right"}} "Loading..."))
        (ui-counter-panel panel)
        (ui-counter-sum counter-sum)))))

;; Simulated Server

; Servers could keep state in RAM
(defonce server-state (atom {:counters {1 {:counter/id 1 :counter/n 44}
                                        2 {:counter/id 2 :counter/n 23}
                                        3 {:counter/id 3 :counter/n 99}}}))

; The server queries are handled by returning a map with a :value key, which will be placed in the appropriate
; response format
(defn read-handler [{:keys [ast state]} k p]
  (log/info "SERVER query for " k)
  (case k
    ; When querying for :all-counters, return the complete set of values in our server counter db (atom in RAM)
    :all-counters {:value (-> (get @state :counters) vals vec)}
    nil))

; The server mutations are handled by returning a map with a :action key whose value is the function that will
; cause the change on the server
(defn write-handler [env k p]
  (log/info "SERVER mutation for " k " with params " p)
  (case k
    ; When asked to increment a counter on the server, do so by updating in-memory atom database
    'counter/inc (let [{:keys [id]} p]
                   {:action (fn [] (swap! server-state update-in [:counters id :counter/n] inc))})
    nil))

; Om Next query parser. Calls read/write handlers with keywords from the query
(def server-parser (om/parser {:read read-handler :mutate write-handler}))

; Simulated server. You'd never write this part
(defn server [env tx]
  (server-parser (assoc env :state server-state) tx))

; Networking that pretends to talk to server. You'd never write this part
(defrecord MockNetwork [complete-app]
  un/UntangledNetwork
  (send [this edn ok err]
    (let [resp (server {} edn)]
      ; simulates a network delay:
      (js/setTimeout #(ok resp) 300)))
  (start [this app]
    (assoc this :complete-app app)))

(defcard-doc
  "# Quick Tour

  Untangled is meant to be as simple as possible, but as Rich would tell you:
  simple does not mean easy. Fortunately, hard vs. easy is something you can fix just by learning.

  Om Next (the lower layer of Untangled) takes a very progressive approach
  to web development, and as such requires that you understand some new
  concepts. This is *by far* the most difficult part of adapting to Untangled.

  In this quick tour our intention is to show you a full-stack Untangled
  application. We hope that as you read this Quick Start you will
  start to comprehend how simple the resulting structure is:

  - No controllers are needed, ever.
  - Networking becomes completely transparent.
  - Server and client code structure are identical. In fact, this tour leverages this fact to simulate server code in
  the browser that is identical to what you'd *put* on the server.
  - The reasoning at the UI layer can be completely local.
  - The reasoning at the model layer can be completely local.
  - The render refresh story is mostly automatic, and where it isn't, it
  is completely abstracted from the UI structure to ease developer reasoning.

  If you're coming from Om Next, realize that Untangled is *not* a competing project. Untangled is a set of
  thin libraries that
  provide default implementations of all of the artifacts you'd normally have to write to make an Om Next application. In
  many cases Untangled is required to 'make a call' about how to do something. When it does, our documentation tries to
  discuss the relative merits and costs.

  ##



  "
  (dc/mkdn-pprint-source Counter)
  (dc/mkdn-pprint-source ui-counter)
  (dc/mkdn-pprint-source CounterPanel)
  (dc/mkdn-pprint-source ui-counter-panel))

(defcard SampleApp
         (untangled-app Root
                        :started-callback (fn [{:keys [reconciler] :as app}]
                                            (log/info "Application (re)started")
                                            (df/load-data reconciler [{:all-counters (om/get-query Counter)}]
                                                          :post-mutation 'add-counters-to-panel))
                        :networking (map->MockNetwork {}))
         {})
