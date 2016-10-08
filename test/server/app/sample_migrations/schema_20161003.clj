(ns app.sample-migrations.schema-20161003
  (:require [untangled.datomic.schema :as s]))

(defn transactions []
  [(s/generate-schema
     [(s/schema user
                (s/fields
                  [name :string :required :definitive]
                  [age :long]
                  [mate :ref :one {:references :user/name}]
                  [friends :ref :many {:references :user/name}]))])])
