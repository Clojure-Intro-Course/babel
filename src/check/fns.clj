(ns check.fns
  (:require [babel.middleware :as middleware]
            [errors.dictionaries :as dict]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))



;; (defn prettify-object [o]
;;   (cond (string? o) o
;;         (= (type o) clojure.lang.LazySeq) (pr-str o)
;;         :else o))

;; (defn- object? [x]
;;   (instance? java.lang.Object x))

(defn check-equal
  "Takes two parameters expected and actual. Returns a string 'Test (= v1 v2) passed' if their values are equal,
   and 'Test (= v1 v2) failed' otherwise, where v1 and v2 are the results of evaluating expected and actual respectively."
  [expected actual]
  (try (if (nil? (assert (= expected actual))) (str "Test (= " (second (dict/type-and-val expected)) " " (second (dict/type-and-val actual)) ") passed"))
       (catch java.lang.AssertionError e (str "Test (= " (second (dict/type-and-val expected)) " " (second (dict/type-and-val actual)) ") failed" #_(.getMessage e)))
       #_(catch Throwable e (middleware/modify-message e))))

(s/fdef check-equal
  :args (s/and :babel.arity/two
               (s/cat :value (s/nilable any?) :second (s/nilable any?))))
(stest/instrument `check-equal)

(defn check-range
  "Takes a number n, and two numbers, low and high. Returns 'Test (<= low n high) passed' if v is between low and high,
   and 'Test (<= low n high) failed' otherwise."
  [n low high]

  (if (<= low n high) (str "Test (<= " low " " n " " high ") passed") (str "Test (<= " low " " n  " " high ") failed")))

(s/fdef check-range
  :args (s/and :babel.arity/three 
               (s/cat :n :babel.type/number-or-lazy :low :babel.type/number-or-lazy :high :babel.type/number-or-lazy)))
(stest/instrument `check-range)

(defn check-precision
  "Takes three numbers: the expected value, the actual value, and the precision. 
   Returns 'Test passed: actual is within precision of expected' if the difference between actual and expected is less than or equal to precision, 
   and 'Test failed: actual is not within precision of expected' otherwise."
  [expected actual precision]
  (if (<= (- expected precision) actual (+ expected precision)) (str "Test passed: " actual " is within " precision " of " expected)
      (str "Test failed: " actual " is not within " precision " of " expected))) ; not sure how this one should be phrased, also might need help on the docstrings

(s/fdef check-precision
  :args (s/and :babel.arity/three 
               (s/cat :expected :babel.type/number-or-lazy :actual :babel.type/number-or-lazy :precision :babel.type/number-or-lazy)))
(stest/instrument `check-precision)

(defn has-key?
  "Takes a keyword and a hashmap, and recursively searches for that keyword in the hashmap, returning logical true if the hashmap has a value other than nil associated with that keyword, and false otherwise."
  [k hm]
  (or (k hm)
   (reduce #(or %1 %2) (map #(if (map? %) (has-key? k %) false) (vals hm)))))

;; takes any collection except map, looks to see if it has a particular element in it
;; returns a string saying which things it found and which things it didn't

(defn has-element
  "Takes any collection except for a map, and checks to see if it contains all listed elements. Returns a string listing which elements were found and which were not." 
  [coll & els]
(reduce #(str %1 ", " %2) (for [el els]
    (if (contains? (into #{} coll) el) (str "Found " el) (str "Did not find " el))
    )))

(s/fdef has-element
  :args (s/and :babel.arity/greater-than-one
               (s/cat :non-map :babel.type/coll-not-map :anything-else (s/+ :babel.type/any-or-lazy))))
(stest/instrument `has-element)

(comment
  (require '[utilities.exception_exploration :as exploration]) 
  (require `check.fns)
  (def parsed-logs (exploration/parse-logs "ex.txt"))
  (filter #(check.fns/has-key? :reason %) parsed-logs) 
  (require '[babel.utils-for-testing :as utils])
  (filter #(re-matches (utils/make-pattern #".*" ":babel.arity") (str %)) parsed-logs)

  )
;; using has-key? as an argument to filter - (filter #(check.fns/has-key? :reason %) exec), where exec is the result of this series of commands being copied into repl
;; we learned that babel specs do not produce a :reason on invalid function arity, where things like s/cat and s/tuple do. we also improved the exception exploration tool while we were going, so that's good
