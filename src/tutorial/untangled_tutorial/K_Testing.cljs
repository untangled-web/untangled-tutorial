(ns untangled-tutorial.K-Testing
  (:require-macros [cljs.test :refer [is]]
                   [untangled-tutorial.tutmacros :refer [untangled-app]])
  (:require [devcards.core :as dc :include-macros true :refer-macros [defcard defcard-doc dom-node]]
            [cljs.reader :as r]
            [om.next.impl.parser :as p]))

; TODO: Exercises. The project is set up with tests and such...have them augment.
; Exercises should include:
; - Showing them how to make inlined/protocol stuff mockable (e.g. wrap calls in function)
; - testing unhappy paths/exceptions
; - Use of all arrow types

(defcard-doc
  "# Testing

  Untangled includes a library for great BDD. The macros in untangled spec wrap clojure/cljs test, so that you may
  use any of the features of the core library. The specification DSL makes it much easier to read the
  tests, and also includes a number of useful features:

  - Outline rendering
  - Left-to-right assertions
  - More readable output, such as data structure comparisons on failure (with diff notation as well)
  - Real-time refresh of tests on save (client and server)
  - Seeing test results in any number of browsers at once
  - Mocking of normal functions, including native javascript (but as expected: not macros or inline functions)
      - Mocking verifies call sequence and call count
      - Mocks can easily verify arguments received
      - Mocks can simulate timelines for CSP logic
  - Protocol testing support (helps prove network interactions are correct without running the full stack)

  ## Setting up

  If you look in `test/client/app` you'll see a few files. Only one of the four is a specification. The other three
  serve the following purposes:

  - `all_tests.cljs` : An entry point for CI testing from the command line.
  - `suite.cljs` : The entry point for browser test rendering.
  - `tests_to_run.cljs` : A file that does nothing more than require all of the specs. The test runners search for
  testing namespaces, so if you don't load them somewhere, they won't be found. Since there are two places tests
  run from (browser and CI) it makes sense to make this DRY.

  There is a `package.json` file for installing node packages to run CI tests.
  The `project.clj` includes various things to make all of this work:

  - The lein doo plugin, for running tests through karma *via* node (in Chrome).
  - A `:doo` section to configure the CI runner
  - A cljsbuild for test with figwheel true. This is the browser test build.
  - A cljsbuild for the CI tests output (automated-tests).
  - The lein `test-refresh` plugin, which will re-run server tests on save, and also can be configured with the
  spec renderer (see the `:test-refresh` section in the project file).

  ## Running server tests

  See `test/server/app/server_spec.clj` for a sample specification. To run all specs, just use:

  ```
  lein test-refresh
  ```

  ## Running client tests (during development)

  Just include `-Dtest` in your JVM argument list. This will cause the test build to start running via figwheel. Then
  just open the [http://localhost:3449/test.html](http://localhost:3449/test.html) file in your browser.

  ## Anatomy of a specification

  The main macros are `specification`, `behavoir`, and `assertions`:

  ```
  (specification \"A Thing\"
     (behavior \"does something\"
        (assertions
           \"optional sub-clause\"
           form => expected-form
           form2 => expected-form2

           \"subclause\"
           form => expected-form)))
  ```

  The specification macro just outputs a `deftest`, so you are free to use `is`, `are`, etc. The `behavior` macro
  outputs additional events for the renderer to make an outline.

  ### Assertions

  Special arrows:

  - `=fn=>` : The right-hand side should be a boolean-returning lambda: (fn [v] boolean)

  ### Mocking

  The mocking system does a lot in a very small space. It can be invoked via the `provided` or `when-mocking` macro.
  The former requires a string and adds an outline section. The latter does not change the outline output.

  Mocking must be done in the context of a specification, and creates a scope for all sub-outlines. Generally
  you want to isolate mocking to a specific behavior:

  ```
  (specification \"Thing\"
    (behavior \"Does something\"
      (when-mocking
        (my-function arg1 arg2) => (do (assertions
                                          arg1 => 3
                                          arg2 => 5)
                                       true)

        (my-function 3 5))))
  ```

  Basically, you include triples (a form, arrow, form), followed by the code to execute (which will not have arrows).

  It is important to note that the mocking support does a bunch of verification at the end of your test:

  - It verifies that your functions are called the appropriate number of times (at least once is the default)
  - It uses the mocked functions in the order specified.
  - It captures the arguments in the symbols you provide (in this case arg1 and arg2). These
  are available for use in the RHS of the mock expression.
  - It returns whatever the RHS of the mock expression indicates
  - If assertions run in the RHS form, they will be honored (for test failures)

  So, the following mock script:

  ```
  (when-mocking
     (f a) =1x=> a
     (f a) =2x=> (+ 1 a)
     (g a b) => 32

     (assertions
       (+ (f 2) (f 2) (f 2) (g 1 2) (g 99 4) => 72))
  ```

  should pass. The first call to `f` returns the argument. The next two calls return the argument plus one.
  `g` can be called any amount (but at least once) and returns 32 each time.

  If you were to remove any call to `f` this test would fail.

  #### Timeline testing

  On occasion you'd like to mock things that use callbacks. Chains of callbacks can be a challenge to test, especially
  when you're trying to simulate timing issues.

  ```
  (def a (atom 0))

  (specification \"Some Thing\"
    (with-timeline
      (provided \"things happen in order\"
                (js/setTimeout f tm) =2x=> (async tm (f))

                (js/setTimeout
                  (fn []
                    (reset! a 1)
                    (js/setTimeout
                      (fn [] (reset! a 2)) 200)) 100)

                (tick 100)
                (is (= 1 @a))

                (tick 100)
                (is (= 1 @a))

                (tick 100)
                (is (= 2 @a))))
  ```

  In the above scripted test the `provided` (when-mocking with a label) is used to mock out `js/setTimeout`. By
  wrapping that provided in a `with-timeline` we gain the ability to use the `async` and `tick` macros (which must be
  pulled in as macros in the namespace). The former can be used on the RHS of a mock to indicate that the actual
  behavior should happen some number of milliseconds in the *simulated* future.

  So, this test says that when `setTimeout` is called we should simulate waiting however long that
  call requested, then we should run the captured function. Note that the `async` macro doesn't take a symbol to
  run, it instead wants you to supply a full form to run (so you can add in arguments, etc).

  Next this test does a nested `setTimeout`! This is perfectly fine. Calling the `tick` function advances the
  simulated clock. So, you can see we can watch the atom change over \"time\"!

  Note that you can schedule multiple things, and still return a value from the mock!

  ```
  (with-timeline
    (when-mocking
       (f a) => (do (async 200 (g)) (async 300 (h)) true)))
  ```

  the above indicates that when `f` is called it will schedule `(g)` to run 200ms from \"now\" and `(h)` to run
  300ms from \"now\". Then `f` will return `true`.

  ## Datomic Testing Support

  NOTE: Requires `untangled-datomic` in your project file.

  Untangled includes top-notch utilities for doing focused integration tests against an in-memory database. This
  allows you to quickly get your low-level database code correct without having to mess with a UI or full stack.

  ### Database fixtures

  The `with-db-fixture` macro creates an in-memory database with a schema of your Datomic migrations. It supports
  the following:

  - Running your migrations on an in-memory database localized to your test
  - Seeding that database via a function you supply (which just returns transaction data)
    - Returning a map from temporary ids in your seed data to real datomic ids in the resulting database

  #### Seed functions

  The seed function is very simple: Just return a list of legal Datomic transaction, but use `:datomic.id/X` in
  place of database IDs anywhere they make sense! This allows you to create any graph of data you need:

  ```
  (defn seed1 []
     [{:db/id :datomic.id/thing :thing/name \"Boo\" }
      {:db/id :datomic.id/list :list/things #{:datomic.id/thing}}])

  (specification \"Boo\"
    (with-db-fixture db
       (behavior ...)
       :migrations \"namespace.of.migrations\"
       :seed-fn seed1))
  ```

  The fact that the seed is a function means you can compose your seed functions for quick, DRY test data generation.

  To get the remapped IDs, it helps to use something like this:

  ```
  (defn db-fixture-defs
    \"Given a db-fixture returns a map containing:
    `connection`: a connection to the fixture's db
    `get-id`: give it a temp-id from seeded data, and it will return the real id from seeded data\"
    [fixture]
    (let [connection (udb/get-connection fixture)
          tempid-map (:seed-result (udb/get-info fixture))
          get-id (partial get tempid-map)]
      {:connection connection
       :get-id     get-id}))
  ```

  which can be combined into it like this:

  ```
  (with-db-fixture fixture
     (let [{:keys [connection get-id]} (db-fixture-defs fixture)
           thing-id (get-id :datomic.id/thing)
           thing (d/entity (d/db connection) thing-id)]
           ; check out thing
       ))
  ```

  ## Protocol Testing

  Untangled client and server include utilities to help you test network protocols without a network. The idea is to
  prove (via a shared data file instead of a network) that the client is saying the right thing and the server
  will understand it and return a compatible result. Furthermore, it allows testing that the server response will
  result in your expected client app state.

  In general, we place these tests and setup in a single `cljc` file. Here are the basic tests you want to run:

  - A test that joins your UI invocation with a real transaction. This proves your UI is saying what you expect.
  - A test that checks the local mutation of the transaction.
  - A test that checks that the mutation/query is transformed as expected (if at all)
  - A test that the server transaction does proper tempid remapping (if tempids are used)
  - A test that the server responds as expected (if it was a query)
  - A test that the server does the right change to server-side state.

  If all of these tests pass, then you have pretty strong proof that your overall dynamic interactions in the Untangled
  application works (optimistic updates, queries, and full stack mutations). This covers a lot of your code in a way
  that is easy to write and understand!
  ")

(defcard-doc
  "### Testing that the UI executes the transaction you expect

  At the moment we're not doing direct UI testing. Instead we typically place any UI transact into a helper function
  that calls `transact!`. You could render your React components to a dom fragment and trigger DOM events with mocking
  in place, but in practice we find that the helper function approach is just less fuss, and it is good enough. You
  don't end up with absolute proof things are hooked up right, but close enough.

  So, let's say you've defined the following mutation:

  ```
  (defmethod m/mutate 'a-mutation [{:keys [state] :as env} k params]
    {:action (fn []
               (swap! state assoc-in [:tbl 4] {:id 4 :name \"Thing\"}))})
  ```

  and your UI uses this function to invoke it:

  ```
  (defn do-a-mutation [comp]
    (om/transact! comp '[(a-mutation)]))
  ```

  #### Exercise 1: Write a test that verifies the UI helper requests the correct mutation

  Make sure you're running the test build in figwheel (`-Dtest`) and have the specification open
  [http://localhost:3449/test.html](http://localhost:3449/test.html).
  You should see a grey area in the spec for the testing-tutorial-spec. This is because all assertions are commented out.

  For sharing between client and server, we write our tests in a `.cljc` file. Open `testing_tutorial_spec.cljc`
  and add a data structure for our expected results:

  ```
  (def do-a-mutation-protocol
    { :ui-tx '[(a-mutation)] ; what the UI should send
    })
  ```

  Your first test would be to prove that the UI will cause a given mutation to occur. Add this specification
  to the test and verify it passes in the browser outline:

  ```
  (specification \"Doing a mutation\"
    (behavior \"generates the correct ui transaction\"
      (when-mocking
        (om/transact! c tx) => (is (= tx (-> do-a-mutation-protocol :ui-tx)))

        (do-a-mutation nil))))
  ```

  Now modify the helper function and see that the test fails. This seems like a trivial test, but it helps glue
  together the proof that a mutation is tested all the way through the full stack. The next step **assumes** that the
  UI will really generate the given `:ui-tx`, so if that were wrong the whole chain would be a false positive.

  ### Testing that the UI Optimistic Update is correct

  The next thing you'd like to do is ensure that the mutation does the correct optimistic update. Since this could
  involve changing all sorts of things in a map at arbitrary nesting levels, and should technically run as
  a partial integration test through the internal parser Untangled provides a nice helper function
  that does all of this for you.

  This is made possible by the fact that mutations are globally added to a single multimethod, and all of the plumbing
  is built-in. Thus, the only thing you have to say is what should happen to the app state! We do this in the shared
  data structure at the top of the CLJC file.

  The data for an optimistic update is stored as a delta, formatted as follows:

  ```
  {:optimistic-delta { [key path to data] expected-value }
   ...other test data... }
  ```

  So, for example to check that a map with `:id 1` has appeared in database table `:table` at key `1` you'd use:

  ```
  { :optimistic-delta { [:table 1 :id] 1 } }
  ```

  You can, of course, check the whole object (instead of just the ID), but often spot checking is sufficient.

  To check the optimistic delta:

  - Make **sure** you've required the namespace that defines the mutation under test! If you don't do this, your
  mutation may not be installed, and will fail.
  - Require `untangled.client.protocol-support` (perhaps as `ps`)
  - Call `(ps/check-optimistic-delta protocol-def-map)` within a specification

  The `check-optimistic-delta` uses the `ui-tx` entry to know what to attempt, and the mutations are already
  installed. So, it makes up an app state (which can be based on `initial-ui-state`, if supplied).

  #### Exercise 2: Check the optimistic update

  1. Add two entries to an `:optimistic-delta` map in the `do-a-mutation-protocol` map.
  2. Add a `initial-ui-state` to the protocol with: `{ :tbl { 4 {:id 4 :name \"Boo\"} } }` (pretend there was something in the table)
  3. Run `check-optimistic-delta` within the cljs specification, passing it the protocol data.

  The test should pass. Note that the checker automatically adds items to the specification about what it tested.

  For sanity, comment out the body of the mutation function. The test should fail (you should see that the original
  object is still there via the delta).

  ### Testing how a mutation talks to the server

  Mutations can trigger a remote mutation (and can modify the original UI transaction). For example, say
  `(a-mutation)` is a perfect thing to say to the UI, but when sending it to the server you must add in
  a parameter (e.g. reference ID, auth info, etc.).

  #### Exercise 3: Check that a mutation is sent to the server

  This test is again mostly automated for you. Simply call `(ps/check-server-tx do-a-mutation-protocol)` after
  setting `:server-tx value` in your protocol.

   NOTE: This test will fail at first.

  - Add `:server-tx '[(a-mutation)]` to your protocol map
  - Add a call to `check-server-tx` within your specification (again, it embeds its own behaviors/assertions)

  Save, and you should see the test complain that nothing was supposed to go to the server. This is correct! Your
  mutation does not specify a remote is to be used!

  Add `:remote true` to your return value from `a-mutation`. The test should now pass.

  #### Exercise 4: Mutations that modify the ui-tx

  As you probably already know, it is possible to modify the mutation sent to the server by giving an AST
  as the value of `:remote`. Add a parameter to the transaction by modifying the mutation to:

  ```
   (defmethod m/mutate 'a-mutation [{:keys [state ast] :as env} k params]
     {:remote (assoc ast :params {:x 1})
      :action (fn []
                (swap! state assoc-in [:tbl 4] {:id 4 :name \"Thing\"}))})
  ```

  Your test should now be failing again. Change the `:server-tx` value in the protocol to fix this.

  ### Testing the the server transaction does the correct thing on the server (Currently assumes Datomic)

  Now we've verified that the UI will invoke the right mutation, the mutation will do the correct optimistic update,
  the mutation will properly modify and generate a server transaction. We've reached the plumbing (network), and we'll
  pretend that it works (since it is provided for you).

  Once it is on the network, we can assume it reached the server (these are happy-path tests). Now you're interested
  in proving that the server code works.

  Untangled has you covered here too!

  Only one function call is needed (with some setup): `check-response-to-client`. It supports checking:

  - That the `:server-tx` runs without error
  - That `:response` in the protocol data matches the real response of the `:server-tx`
  - That Om tempids are remapped to real datomic IDs in the `:response`
  - (optional) post-processing checks where you can examine the state of the database/system

  The setup allows you to seed an in-memory test database for the transaction to run against.

  TODO: Finish writing this section...

  ### Testing that the server response is properly understood by the client

  Now that you've proven the server part works, you'd shown that `:response` in the protocol data is correct. You
  can now use that to verify the client will do the correct thing with it.

  This is another cljs test, supported by `check-response-from-server`. It is very similar to the optimistic
  update test, and simply requires these keys in the protocol data:

  `response`: the exact data the server is supposed to send back to the client (which you proved with a server test)
  `pre-response-state`: normalized app state prior to receiving `response`
  `server-tx`: the transaction originally sent to the server, yielding `response` (which you proved was what the UI sends)
  `merge-delta`: the delta between `pre-response-state` and its integration with `response`. Same format as optimistic-delta.

  The call to `check-response-from-server` sets up the Untangled internals with the given `pre-response-state`, merges
  the response with the normal merge mechanisms (which requires the `server-tx` for normalization), and then
  verifies your `merge-delta`.

  You can use this method to test simple query response normalization, but in that case you must use a `server-tx` that
  comes from UI components (or normalization won't work).

  TODO: Add exercise...

  ### Handling temporary IDs in protocol tests

  The protocol testing helpers completely understand temporary IDs, and make it really easy to use.

  The basic features are:

  - Anywhere you use a keyword namespaced to `:om.tempid/` (client or server side of the protocol), then
  the protocol helpers will translate them to real Om tempids during the testing
  (e.g. ensuring type checks for Om tempids will work in your real code).
  - Anywhere you use a keyword namespaced to `:datomic.id/` (client or server side of the protocol)
  the helpers will treat them correctly (e.g. on the server side it will join them up to seeded data correctly)

  So, in all of the above tests you can use these namespaced keywords to simulate Om temporary and seeded data IDs.

  ")


