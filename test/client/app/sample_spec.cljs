(ns app.sample-spec
  (:require
    [untangled-spec.core :refer-macros [specification behavior when-mocking provided assertions component with-timeline async tick]]
    [cljs.test :refer-macros [is]]))

(specification "A Sample Specificaation"
               (behavior "Has some items"
                         (assertions
                           "Math works"
                           (+ 1 1) => 2

                           "Data structures can be compared"
                           {:a 2} => {:a 2})))

