(ns app.exercises-solutions-spec
  (:require
    [untangled-spec.core :refer-macros [specification behavior when-mocking provided assertions component with-timeline async tick]]
    [cljs.test :refer-macros [is]]))

(defn sum-sq [a b] (+ (* a a) (* b b)))

(defn replace-pairs [data]
  (map (fn [[a b]] (sum-sq a b)) data))

(specification "Exercise 1"
  (behavior "2 * 2 = 4"
    (assertions
      (* 2 2) => 4)))

(specification "Exercise 2"
  (behavior "6 is even"
    (assertions
      6 =fn=> even?)))

(specification "Exercise 3"
  (component "sum-sq"
    (assertions "Sums the squares of the two arguments"
                (sum-sq 1 1) => 2
                (sum-sq 2 4) => 20
                (sum-sq -3 -9) => 90))
  (component "replace-pairs"
    (when-mocking
      (sum-sq a b) => :sum-sq

      (assertions
        "Replaces pairs with the sum of their squares"
        (replace-pairs [[1 2] [5 4] [9 9]]) => [:sum-sq :sum-sq :sum-sq]
        )))
  (component "replace-pairs (bonus)"
    (when-mocking
      (sum-sq a b) =1x=> :sum1
      (sum-sq a b) =1x=> :sum2
      (sum-sq a b) =1x=> :sum3

      (assertions
        "Replaces pairs with the sum of their squares"
        (replace-pairs [[1 2] [5 4] [9 9]]) => [:sum1 :sum2 :sum3]))))

(specification "Exercise 4"
  (component "replace-pairs (integration)"
    (let [real-sum-sq sum-sq]
      (provided "Uses sum-sq to calculate entries, which means:"
        (sum-sq a b) =6x=> (real-sum-sq a b)

        (assertions
          "Result is a positive sequence when pairs are all positive numbers"
          (replace-pairs [[1 2] [5 4] [9 9]]) =fn=> (fn [list] (every? pos? list))
          "Result is a positive sequence when pairs contain mixes of negative numbers"
          (replace-pairs [[-1 2] [5 -4] [-9 -9]]) =fn=> (fn [list] (every? pos? list)))))))
