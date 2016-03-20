(ns untangled-tutorial.J-Putting-It-Together
  (:require-macros [cljs.test :refer [is]])
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [devcards.core :as dc :refer-macros [defcard defcard-doc]]))

(defcard-doc
  "
  # Putting it All Together

  In this section we'll put all of the pieces together into a fully functioning, full-stack application.

  ...

  Things to cover:

  - Basic full-stack application:
      - General philosophy of Untangled on UI queries, server queries, and views.
          - Structural differences between UI and server
          - Ideas around UI views vs server schema
          - Reminders about:
              - normalization
              - UI attributes
              - Attribute merging behavior
      - Initial loading
          - Sending the query
          - Handling the query
          - Post-processing the result
          - Loading markers/indicators
      - Adding an optimistic update
      - Processing mutations on the server
          - Tempid handling
      - Lazy Loading
          - Field-based loading
          - Local UI loading/failed markers
      - Handling Failures
          - Error responses with post-processed handling
          - Hard errors with transaction fallbacks

  - Provisioning a Datomic Database (probably in another section)
      - Adding a migration
      - Configuring the database
      - Injecting it into the parsing env
      - Pre-seeding data

  ")
