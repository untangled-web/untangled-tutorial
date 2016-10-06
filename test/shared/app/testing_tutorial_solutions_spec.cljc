(ns app.testing-tutorial-solutions-spec
  (:require
    [untangled-spec.core #?(:cljs :refer-macros :clj :refer) [specification behavior when-mocking provided assertions component with-timeline async tick]]
    [#?(:cljs untangled.client.protocol-support :clj untangled.server.protocol-support) :as ps]
    [#?(:cljs cljs.test :clj clojure.test) :refer-macros [is]]
    #?(:cljs [untangled.client.mutations :as m])
    #?(:clj [untangled.datomic.test-helpers :as helper])
    #?(:clj [untangled.server.core :as server])
    #?(:clj [app.demo-server :as demo])
    #?(:cljs [om.next :as om])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; NOTE: We rename the mutation in the solutions because in Untangled the mutations are globally defined on the multi-method,
;; so we don't want collisions from the exercise file.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; This is the declaration of the elements of data that are to be verified by the tests.
(def add-user-protocol
  {:ui-tx            '[(soln/add-user {:id :om.tempid/new-user :name "Orin"})]
   :server-tx        '[(soln/add-user {:id :om.tempid/new-user :name "Orin" :age 1})]
   :initial-ui-state {:users/by-id {}}
   :optimistic-delta {[:users/by-id :om.tempid/new-user :user/name] "Orin"
                      [:users/by-id :om.tempid/new-user :user/age]  1}
   })

#?(:cljs
   (defmethod m/mutate 'soln/add-user [{:keys [state ast] :as env} k {:keys [id name] :as params}]
     {:remote (assoc ast :params (assoc params :age 1))
      :action (fn []
                (swap! state assoc-in [:users/by-id id] {:db/id id :user/name name :user/age 1}))}))

#?(:cljs
   (defn add-user [comp name]
     (om/transact! comp `[(soln/add-user ~{:id (om/tempid) :name name})])))

#?(:cljs
   (specification "Adding a user"
     (behavior "generates the correct ui transaction"
       (when-mocking
         (om/tempid) => :om.tempid/new-user
         (om/transact! c tx) => (is (= tx (-> add-user-protocol :ui-tx)))

         (add-user :some-component "Orin")))
     (ps/check-optimistic-update add-user-protocol)
     (ps/check-server-tx add-user-protocol)))

#?(:clj
   (defn user-seed [connection]
     (helper/link-and-load-seed-data connection
       [{:db/id :datomic.id/joe :user/name "Joe" :user/age 44}])))

; TODO: Server protocol checks are too hard to write.
