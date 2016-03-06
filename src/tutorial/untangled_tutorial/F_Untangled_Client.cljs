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
      - Support for refreshing the entire UI on hot code reload (with your help)
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

  - A project file that is configured for running both the server, client, and tests with hot code reload.
  - An entry point for production that does nothing *but* mount the app. Nothing but the project build refers to this,
  so we don't accidentally try to do entry point operations more than once. It can also be used to do things
  like disable console logging.
  - A development-only namespace file that mounts the app in development mode. Only the dev build includes this source.
  - A core namespace that creates the application and loads things like i18n support. This is referred to by the production
  build and the development-only namespace.

  ## Enabling re-render on hot code reload

  Om and React do their best to prevent unnecessary rendering. During development we'd like to
  force a refresh that whenever code reloads so we can see our changes.
  In order to do this:

  - Untangled forces a full UI refresh if `mount` is called on an already mounted app. If you specify that the
  development namespace (which always mounts the app) is reloaded every time, then that is sufficient. If
  you have figwheel `:recompile-dependents true` in the project file (which we set by default) this typically
  ensures that the user development namespace is always reloaded. Since `mount` is called there, this is usually
  sufficient.
  - The application itself has a (protocol) method named `refresh`. You can configure
  figwheel to invoke a function that calls thta after each load.

  However, in order to get *React* to actually re-render, you have to cooperate by adding a react
  key from your app state onto your top-level DOM node, like this:

  ```
  (defui Root
     static om/IQuery
     (query [this] [:ui/react-key ...])
     Object
     (render [this]
        (let [{:keys [ui/react-key ...]} (om/props this)]
          (dom/div #js { :key react-key } ...))))
  ```

  The Untangled refresh will automatically set this key in your app state to a new unique value
  every time, ensuring that the app state has changed (which will cause Om to allow a re-render) and
  the top-level key change will cause React to force a full DOM diff (ignoring the fact that the
  rest of the recursive state has not changed).

  ## Using the REPL

  ### Making sure you're connected to the right browser/tab

  If you're running more than one build in Figwheel, the REPL will only be connected to one browser tab
  at a time. You can run `(fig-status)` to see what builds are running and how many browsers are
  connected to each.

  If you're trying to look at app state, make sure ONLY one browser is connected to it, otherwise
  you'll confuse yourself!

  To ensure that you're talking to the tab of the correct build, you should do the following at
  the REPL:

  ```
  cljs.user=> *:cljs/quit*
  Choose focus build for CLJS REPL (tutorial, client, test) or quit > client
  Launching ClojureScript REPL for build: client
  ```

  ### Viewing application state

  The sample project `user` namespace includes a helper function called `log-app-state`. By default
  it will show the entire application state; however, this is often too much (if it gets too big, your
  REPL will give an error). Instead, you can supply it with a keyword and it will look that up in the
  app state and show just that bit instead.

  ```
  cljs.user=> (log-app-state :item)
  {:item [:item/by-id 1]}
  ```

  ### Chrome dev tools

  ClojureScript has some Chrome dev tools that we highly recommend (and install in the project file by
  default):

  ```
  [binaryage/devtools \"0.5.2\"]
  ```

  These tools require that you run this code as soon as possible:

  ```
  (defonce devtools-installed
    (do (devtools/enable-feature! :sanity-hints)
        (devtools/install!)
        true))
  ```

  AND you must enable custom formatters in Chrome dev tools: Dev tools -> Settings -> Console -> Enable Custom Formatters

  Once you've installed these you'll get features like:

  - Use `(js/console.log v)` and `v` will display as Clojurescript data
  - See cljs variables and other runtime information in the source debugger as Clojurescript data

  These tools are critical when trying to debug your application, as you can actually clearly see
  what is going on!

  "
  )

