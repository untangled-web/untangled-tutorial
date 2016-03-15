(ns untangled-tutorial.H-Server-Interactions
  (:require-macros [cljs.test :refer [is]])
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [devcards.core :as dc :refer-macros [defcard defcard-doc]]))

(defcard-doc
  "
  # Server Interaction

  The semantics of server request processing in Untangled have a number of guarantees
  that Om does not (out of the box) provide:

  - Networking is provided
  - All network requests (queries and mutations) are single-threaded. This allows you
  to reason about optimistic updates (Starting more than one at a time via async calls could
  lead to out-of-order execution, and impossible to reason about recovery from errors).
  - You may provide fallbacks that indicate error-handling mutations to run on failures

  ## Reads

  Remote reads in Untangled are explicit. There is no Om parser to write on the client side.

  ### UI Attributes

  Untangled recognizes the need to separate attributes that are UI-only and those that should actually be sent to
  the server. If a component, for example, wants to query for:

  ```
  [:ui/popup-visible :db/id :item/name]
  ```

  where the `popup-visible` item is actually in app state (a useful thing to do with most state instead of making
  it component local), then you have a problem when that component is composed into another component that
  is to be used when generating a server query. You don't want the UI-specific attributes to *go* to the server!

  Untangled handles this for you. Any attributes in you component queries that are namespaced to `ui` are automatically
  (and recursively) stripped from queries before they are sent to the server. There is nothing for you to do except
  namespace these local-only attributes in the queries! (Additionally, there are local mutation
  helpers that can be used to update these without writing custom mutation code. See the section on Mutation)


  ### Data Merge

  #### Ensuring queries from more that one part of the UI behave well

  #### Normalization


  ### Query Narrowing



  ## Mutations

  ### Optimistic (client) changes

  ### Server Writes

  #### Updating an existing item

  #### New Item Creation – Temporary IDs

  #### Reading results after a mutation


  ## Differences from stock Om (next)

  For those that are used to Om, you may be interested in the differences and rationale behind the way Untangled
  handles server interactions.

  In Om, you have a fully customizable experience for reads/writes; however, to get this power you must write
  a parser to process the queries and mutations, including analyzing application state to figure out when to talk
  to a remote. While this is fully general and quite flexible (Untangled is implemented on top of it, after all),
  there is a lot of head scratching to get the result you want.

  Initial loads and user-driven loads are the most common task in interacting with a server. In both of these cases
  you probably want:

  - To know that it is happening in the UI
      - To know/show (in the UI) when the load is in progress on the network
      - To know/show (in the UI) when the load is complete
      - The ability to show a loading marker in the UI globally
      - The ability to show a loading marker in the UI at the *target* location for the incoming data
  - To have clear reasoning about when you load something that doesn't involve thinking about a parser
  - The ability to create derived data after the load

  The UI portions are accomplished, of course, by putting something in the app state that the UI is querying for, and
  rendering it. The reasoning portion requires that you keep track of what you have, and what you want to do. The
  ability to create derived data and manage the 'state machine' of the process of loading (and failures)
  is a bit more difficult.

  By eliminating the need for an Om parser to process all of this, and centralizing the logic to a core set of functions
  that handle all of these concerns you gain a lot of simplicity.

  The use-cases we're addressing are:

  ### Use Case - Initial Load

  In Om, you'd write a parser, set some initial state indicating 'I need to load this', and in your parser you'd return
  a remote `true` for that portion of the read when you hit it. The intention would then be that the server returning
  data would overwrite that marker and the resulting re-render would update the UI. If your UI query doesn't match what
  your server wants to hear, then you either write multiple UI-handling bits on the server, or you pre/post process the
  ROOT-centric query in your send function. Basically, you write a lot of plumbing. Server error handling is completely
  up to you in your send method.

  In Untangled, initial load is an explicit step. You simply put calls to `load-collection` in your app start callback.
  State markers are put in place that allow you to then render the fact that you are loading data. Any number of separate
  server queries can be queued, and the queries themselves are used for normalization. Post-processing of the response
  is well-defined and trivial to access.

  ### Use Case - Lazy Loading

  The other major case is wanting to load data in response to a user interaction. Interestingly, the query that you might
  have used in the initial load use case might have included UI queries for data you didn't want to fetch yet. So, we want
  to note that the initial load use-case supports eliding part of the query. For example, you can load an item without,
  say, comments. Later, when the user wants to see comments you can supply a button that can load the comments on demand.

  This is directly supported by `load-field`, which derives the query to send to the server from the component itself!

  ```
  (load-field this :comments)
  ```

  The only requirements are that the component has an Ident and the query for the component includes a join or property
  named `:comments` in the query.

  For example, say you had:

  ```
  (defui Item
     static om/IQuery
     (query [this] [:id :value {:comments (om/get-query Comment)}])
     static om/Ident
     (ident [this props] [:item/by-id (:id props)])
     Object
     (render [this]
        ...
        (dom/button (clj->js { :onClick #(df/load-field this :comments) }) \"Load comments\")
        ...)
  ```

  then clicking the button will result in the following query to the server:

  ```
  [{[:item/by-id 32] [{:comments [:other :props]]}]
  ```

  and the code to write for the server is now trivial. The dispatch key is :item/by-id, the 32 is accessible on the AST,
  and the query is a pull fragment that will work relative to an item in your (assuming Datomic) database!

  Furthermore, the underlying code can easily put a marker in place of that data in the app state so you can show the
  'load in progress' marker of your choice.

  Untangled has supplied all of the Om plumbing for you.

  ### Remote Reads after a Mutation

  In Om, you can list properties after your mutation to indicate re-renders. You can force them to be remote reads by
  quoting them. All of this requires complex logic in your parser to compare flags on the AST, process the resulting
  query in send (e.g. via process-roots), etc. It is more flexible, but the very common case can be made a lot more direct.

  ```
  (om/transact! this '[(app/f) ':thing]) ; run mutation and re-read thing on the server...
  ; BUT YOU implement the logic to make sure it is understood that way!
  ```

  In Untangled, follow-on keywords are always local re-render reads, and nothing more:

  ```
  (om/transact! this '[(app/f) :thing]) ; Om and Untangled: Do mutation, and re-render anything that has :thing in a query
  ```

  Instead, we supply access to the internal mutation we use to queue loads, so that remote reads are simple and explicit:

  ```
  ; Do mutation, then run a remote read of the given query, along with a post-mutation to alter app state when the load is complete
  (om/transact! this `[(app/f) (app/load {:query ~(om/get-query Thing) :post-mutation after-load-sym}])
  ```

  Of course, you can (and *should) use syntax quoting to embed a query from (om/get-query) so that normalization works,
  and the post-mutation can be used to deal with the fact that other parts of the UI may want to, for example, point
  to this newly-loaded thing.

")
