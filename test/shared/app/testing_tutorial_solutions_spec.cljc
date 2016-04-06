(ns app.testing-tutorial-solutions-spec
  (:require
    [untangled-spec.core #?(:cljs :refer-macros :clj :refer) [specification behavior when-mocking provided assertions component with-timeline async tick]]
    [#?(:cljs untangled.client.protocol-support :clj untangled.server.protocol-support) :as ps]
    [#?(:cljs cljs.test :clj clojure.test) :refer-macros [is]]
    #?(:cljs [untangled.client.mutations :as m])
    #?(:cljs [om.next :as om])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; NOTE: We rename the mutation in the solutions because in Untangled the mutations are globally defined on the multi-method,
;; so we don't want collisions from the exercise file.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; This is the declaration of the elements of data that are to be verified by the tests.
(def do-soln-mutation-protocol
  {:ui-tx            '[(soln-mutation)]
   :server-tx        '[(soln-mutation {:x 1})]
   :initial-ui-state {:tbl {4 {:id 4 :name "Other"}}}
   :optimistic-delta {[:tbl 4 :name] "Thing"
                      [:tbl 4 :id]   4}
   })

#?(:cljs
   (defmethod m/mutate 'soln-mutation [{:keys [state ast] :as env} k params]
     {:remote (assoc ast :params {:x 1})
      :action (fn []
                (swap! state assoc-in [:tbl 4] {:id 4 :name "Thing"}))}))

#?(:cljs
   (defn do-soln-mutation [comp]
     (om/transact! comp '[(soln-mutation)])))

#?(:cljs
   (specification "UI helper do-soln-mutation"
                  (behavior "generates the correct ui transaction"
                            (when-mocking
                              (om/transact! c tx) => (is (= tx (-> do-soln-mutation-protocol :ui-tx)))

                              (do-soln-mutation nil)))
                  (ps/check-optimistic-update do-soln-mutation-protocol)
                  (ps/check-server-tx do-soln-mutation-protocol)))
