(ns untangled-tutorial.J-Putting-It-Together
  (:require-macros [cljs.test :refer [is]])
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [devcards.core :as dc :refer-macros [defcard defcard-doc]]))

(defcard-doc
  "
  # Putting it all together

  There are many examples of client-only and full-stack applications in the
  [https://github.com/untangled-web/untangled-cookbook](Untangled Cookbook).
  ")
