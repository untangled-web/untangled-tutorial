(ns untangled-tutorial.J-Putting-It-Together
  (:require-macros [cljs.test :refer [is]]
                   [untangled-tutorial.tutmacros :refer [untangled-app]])
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [untangled-tutorial.putting-together.solutions :as soln]
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
      <ol>
        <li>Item 1</li> <!-- TodoItem -->
        <li>Item 2</li> <!-- TodoItem -->
        <li>Item 3</li> <!-- TodoItem -->
      </ol>
    </div>
  </div>
  ```

  You should have components for `TodoList` (which will be your root), `ItemList`, and `TodoItem`.
  `K_Putting_It_Together.cljs` has the basic bits already named.

  Remember:
  - Use InitialAppState, and compose a few manually created items into the list to verify rendering
  - include `Ident` on `TodoItem` and `ItemList`
  - include a query everything (list title, items, item details (label, done, id)
  - don't foget static
  - A :keyfn on the TodoItem factory

  When you're done, the devcard should render things correctly. There are solutions in `untangled_tutorial/putting_together/solutions.cljs`
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
  (untangled-app TodoList)
  {}
  {:inspect-data true})

(defcard-doc "
  ## Further Reading

  There are many examples of client-only and full-stack applications in the
  [https://github.com/untangled-web/untangled-cookbook](Untangled Cookbook).
  ")


