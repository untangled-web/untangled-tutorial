(ns untangled-tutorial.queries.exercise-util
  (:require [untangled-tutorial.local-read :as local]
            [om.next :as om]))

(def parser (om/parser {:read local/read-local}))
