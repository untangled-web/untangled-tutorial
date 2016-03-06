(ns app.core
  (:require
    app.mutations
    yahoo.intl-messageformat-with-locales
    [untangled.client.core :as uc]
    ))

(defonce app (atom (uc/new-untangled-client
                     :initial-state {:item {:id 1 :label "This is an item"}})))

