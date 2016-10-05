(ns app.db-testing-solutions-spec
  (:require [untangled-spec.core :refer [specification provided behavior assertions]]
            [datomic.api :as d]
            [untangled.datomic.protocols :as udb]
            [untangled.datomic.test-helpers :as helpers :refer [with-db-fixture]]))

(defn user-seed [connection]
  (helpers/link-and-load-seed-data connection
    [{:db/id :datomic.id/joe :user/name "Joe" :user/age 45 :user/mate :datomic.id/mary :user/friends #{:datomic.id/sam :datomic.id/mary}}
     {:db/id :datomic.id/mary :user/name "Mary" :user/age 33 :user/mate :datomic.id/joe :user/friends #{:datomic.id/sally}}
     {:db/id :datomic.id/sam :user/name "Sam" :user/age 15 :user/friends #{:datomic.id/sally}}
     {:db/id :datomic.id/sally :user/name "Sally" :user/age 22 :user/friends #{:datomic.id/sam :datomic.id/mary}}]))

(defn db-fixture-defs
  "Given a db-fixture returns a map containing:
  `connection`: a connection to the fixture's db
  `get-id`: give it a temp-id from seeded data, and it will return the real id from seeded data"
  [fixture]
  (let [connection (udb/get-connection fixture)
        tempid-map (:seed-result (udb/get-info fixture))
        get-id (partial get tempid-map)]
    {:connection connection
     :get-id     get-id}))

(specification "My Seeded Users (SOLUTION)"
  (with-db-fixture my-db
    (let [{:keys [connection get-id]} (db-fixture-defs my-db)
          info (udb/get-info my-db)
          db (d/db connection)
          get-user (fn [id] (some->> (get-id id) (d/entity db)))
          joe (get-user :datomic.id/joe)
          mary (get-user :datomic.id/mary)
          sam (get-user :datomic.id/sam)
          sally (get-user :datomic.id/sally)]
      (assertions
        "Joe is married to Mary (solution)"
        (-> joe :user/mate :user/name) => "Mary"
        "Mary is friends with Sally (solution)"
        (->> mary :user/friends (mapv :db/id) set) => #{(:db/id sally)}
        "Joe is friends with Mary and Sam (solution)"
        (->> joe :user/friends (mapv :db/id) set) => #{(:db/id mary) (:db/id sam)}
        "Sam is 15 years old (solution)"
        (-> sam :user/age) => 15))
    :migrations "app.sample-migrations"
    :seed-fn user-seed))
