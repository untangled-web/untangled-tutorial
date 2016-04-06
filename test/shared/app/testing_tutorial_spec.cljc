(ns app.testing-tutorial-spec
  (:require
    [untangled-spec.core #?(:cljs :refer-macros :clj :refer) [specification behavior when-mocking provided assertions component with-timeline async tick]]
    [#?(:cljs untangled.client.protocol-support :clj untangled.server.protocol-support) :as ps]
    [#?(:cljs cljs.test :clj clojure.test) :refer-macros [is]]
    #?(:cljs [untangled.client.mutations :as m])
    #?(:cljs [om.next :as om])))

; This is the declaration of the elements of data that are to be verified by the tests.
(def do-a-mutation-protocol
  {
   })

#?(:cljs
   (defmethod m/mutate 'a-mutation [{:keys [state ast] :as env} k params]
     {:action (fn []
                (swap! state assoc-in [:tbl 4] {:id 4 :name "Thing"}))}))

#?(:cljs
   (defn do-a-mutation [comp]
     (om/transact! comp '[(a-mutation)])))

#?(:cljs
   (specification "UI helper do-a-mutation"
                  ; Ex1: A spec to show the UI tx

                  ))

