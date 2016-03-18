(ns untangled-tutorial.tutmacros
  (:require [devcards.core :as dc]))

(defmacro untangled-app [root-ui]
  (let [varname (gensym)]
    `(dc/dom-node
       (fn [state-atom# node#]
         (defonce ~varname (atom (uc/new-untangled-client :initial-state state-atom#)))
         (reset! ~varname (uc/mount @~varname ~root-ui node#))))))
