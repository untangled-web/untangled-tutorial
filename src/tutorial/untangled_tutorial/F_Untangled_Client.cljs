(ns untangled-tutorial.F-Untangled-Client
(:require [om.next :as om :refer-macros [defui]]
  [om.dom :as dom]
  [devcards.core :as dc :refer-macros [defcard defcard-doc]]))

(defcard-doc
  "
  # Builing an Untangled Client

  We're now prepared to write a standalone Untangled Client! Once you've understood
  how to build the UI and do a few mutations it actually takes very little code:

  ```
  (ns app.core
    (:require
      app.mutations ; remember to load your add-on mutations
      [untangled.client.core :as uc]
      [app.ui :as ui]
      yahoo.intl-messageformat-with-locales ; if using i18n
      [om.next :as om]))

  (defonce app (atom (uc/new-untangled-client :initial-state { :some-data 42 })))
  (reset! app (core/mount @app ui/Root \"app\"))
  ```

  This tiny bit of code does a *lot*:

  - It creates an entire Om ecosystem:
      - A parser for reads/mutates
      - A local read function that can read your entire app database
      - Plumbing to make it possible to do networking to your server
      - Complete handling of tempids, merging, attribute conflict resolution, and more!
      - Application locale handling
      - Support VCR viewer recording with internal mutations that can submit support requests
  - It mounts the application UI on the given DOM element (you can pass a real node or string ID)

  Some additional things that are availabe (which we'll cover soon):

  - The ability to load data on start using any queries you've placed on the UI or written elsewhere
  - The ability to do deferred lazy-load on component fields (e.g. comments on an item)

  Wow! That's a lot for two lines of code.

  ## Recommended Application Layout for Development

  A lot of thought has gone into how to lay out your application to be able to:

  - Run it in development mode with figwheel
  - Compile it to production code with advanced optimizations
  - Use the REPL support in your IDE/editor

  The primary components of this layout are:

  - An entry point for production that does nothing *but* mount the app
  - A core namespace that creates the application and loads things like i18n support
  - A development namespace file that mounts the app in development mode
  - A project file that is configured for running both the server, client, and tests with hot code reload.
  "
  )

