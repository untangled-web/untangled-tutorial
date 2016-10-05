(ns untangled-tutorial.M30-Advanced-Mutation
  (:require-macros [cljs.test :refer [is]])
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [devcards.core :as dc :refer-macros [defcard defcard-doc]]
            [untangled.client.core :as uc]))

; ADVANCED:
; TODO: Talk about integrating external data (e.g. setTimeout, XRH from Yahoo, etc.)
; TODO: might be useful to talk about tree->db, merge!, and just merge-state! (advanced)
; really important to cover WHY you NEED a query AND data to do this

(defcard-doc
  "
  # Mutation
  ")

