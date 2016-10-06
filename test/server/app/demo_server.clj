(ns app.demo-server
  (:require
    [untangled.server.core :as core]
    [om.next.server :as om]
    [untangled.datomic.core :as ud]))

(defmulti api-mutate om/dispatch)
(defmulti api-read om/dispatch)

(defmethod api-mutate 'soln/add-user [env k p] nil )

(def server-parser (om/parser {:read api-read :mutate api-mutate}))

; build the server
(defn make-system []
  (core/make-untangled-server
    :config-path "/usr/local/etc/app.edn"
    ; Standard Om parser
    :parser server-parser
    ; The keyword names of any components you want auto-injected into the parser env (e.g. databases)
    :parser-injections #{:user-database}
    ; Additional components you want added to the server
    :components {:user-database (ud/build-database :user)}))

