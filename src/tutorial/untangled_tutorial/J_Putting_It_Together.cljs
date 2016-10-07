(ns untangled-tutorial.J-Putting-It-Together
  (:require-macros [cljs.test :refer [is]]
                   [untangled-tutorial.tutmacros :refer [untangled-app]])
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [untangled-tutorial.putting-together.soln-ex-1 :as soln1]
            [untangled-tutorial.putting-together.soln-ex-2 :as soln2]
            [devcards.core :as dc :refer-macros [defcard defcard-doc]]
            [untangled.client.core :as uc]
            [untangled.client.data-fetch :as df]))

(defcard-doc
  "
  # Putting it all together

  This section is a large interactive exercise. In this exercise you'll use the server that is partially built in
  this project, and you'll build your client right here in this file (in devcards).

  Our goal is to demonstrate the following core features:

  - Building a UI that will be (eventually) populated from a server (using InitialAppState as a guiding light)
  - Writing a server-side query response that can fill that UI
  - Using the `started-callback` to trigger an initial load of the data into the UI
  - Working with mutations that do optimistic update and remote mutations

  ## Setting up

  First, review the steps for getting the server running in [Building A Server Exercises](#!/untangled_tutorial.I_Building_A_Server_Exercises).

  Make sure you can start the server, and make sure you're loading this tutorial *through the server* (e.g. check
  the port, it should not be 3449. Check your server logs, as the port is reported there.)
  ")

(defui CheckSetupRoot
  static uc/InitialAppState
  (initial-state [this params] {})
  static om/IQuery
  (query [this] [:ui/react-key :something])
  Object
  (render [this]
    (let [{:keys [ui/react-key something]} (om/props this)]
      (dom/div #js {:key react-key :style #js {:border "1px solid black"}}
        (if (= 66 something)
          (dom/p nil "SERVER RESPONDED WITH 66!")
          (dom/p nil "No response from server. You might have a problem. Make sure the API in api.clj is returning {:value 66} for :something, and your browser is hitting the correct server port."))))))

(defcard check-setup
  "This card checks to see if your server is working. Start your server, then reload this page on that server and you should see a SERVER RESPONDED message."
  (untangled-app CheckSetupRoot :started-callback (fn [{:keys [reconciler] :as app}] (df/load-data reconciler [:something]))))

(defcard-doc "
  ## The Project

  We're going to write a simple TODO list, with a very simple UI. Use what you've learned to lay out the following
  DOM (via defui):

  ```html
  <div>  <!-- TodoList -->
    <div>  <!-- ItemList -->
      <h4>TODO</h4>
      <input type=text><button>Add</button>
      <ol>
        <li><input type=checkbox> Item 1 <button>X</button></li> <!-- TodoItem -->
        <li><input type=checkbox> Item 2 <button>X</button></li> <!-- TodoItem -->
        <li><input type=checkbox> Item 3 <button>X</button></li> <!-- TodoItem -->
      </ol>
    </div>
  </div>
  ```

  ## Exercise 1 - Create the UI

  You should have components for `TodoList` (which will be your root), `ItemList`, and `TodoItem`.
  `K_Putting_It_Together.cljs` has the basic bits already named.

  Remember:
  - Use InitialAppState, and compose a few manually created items into the list to verify rendering
  - include `Ident` on `TodoItem` and `ItemList`
  - include a query everything (list title, items, item details (label, done, id)
  - don't foget static
  - A :keyfn on the TodoItem factory

  When you're done, the devcard should render things correctly. There are solutions in `untangled_tutorial/putting_together/soln_ex_1.cljs`
  if you get stuck.
  ")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;START HERE
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
#_(defui ^:once TodoItem
    Object
    (render [this]))

#_(defui ^:once ItemList
    Object
    (render [this]))

(defui ^:once TodoList
  Object
  (render [this] (dom/div nil "TODO")))

(defcard todo-list-application
  "This card can be used to show your application. "
  (untangled-app soln2/TodoList)
  {}
  {:inspect-data true})

(defcard-doc "
  ## Exercise 2 -- Add Some Local Mutations

  First, let's make it so we can check/uncheck items. A simple mutation that toggles done should do this, once it is
  hooked to `:onChange` of the input.

  Once you've done that, hook up the input field and button to add items. The mutation should add a new item to the database
  of items and append it (use `integrate-ident!`) to the list of items. Remember to use `om/tempid` (as a
  parameter of the UI transaction) to generate an ID for the new item.

  Next, hook up the delete button to remove items.

  The solutions to this exercise are in `putting_together/soln_ex_2.cljs`.

  ## Exercise 3


  ## Further Reading

  There are many examples of client-only and full-stack applications in the
  [https://github.com/untangled-web/untangled-cookbook](Untangled Cookbook).
  ")


