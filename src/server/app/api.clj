(ns app.api
  (:require [om.next.server :as om]
            [taoensso.timbre :as timbre]))

(defmulti apimutate om/dispatch)

;; your entry point for handling mutations. Standard Om mutate handling. All plumbing is taken care of. UNLIKE Om, if you
; return :tempids from your :action, they will take effect on the client automatically without post-processing.
(defmethod apimutate :default [e k p]
  (timbre/error "Unrecognized mutation " k))

;; your query entry point (feel free to make multimethod). Standard Om fare here.
(defn api-read [{:keys [ast query] :as env} dispatch-key params]
  (Thread/sleep 1000)
  (case dispatch-key
    :something {:value 66}
    (timbre/error "Unrecognized query for " dispatch-key " : " query)))
