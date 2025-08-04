(ns check.fns
  (:require [babel.middleware :as middleware]
            [errors.dictionaries :as dict]
            [clojure.spec.alpha :as s]))



(defn prettify-object [o]
  (cond (string? o) o
        (= (type o) clojure.lang.LazySeq) (pr-str o)
        :else o))

(defn- object? [x]
  (instance? java.lang.Object x))

(defn check-equal [e1 e2]

  (try (if (nil? (assert (= e1 e2))) (str "Test (= " (second (dict/type-and-val e1)) " " (second (dict/type-and-val e2)) ") passed"))
       (catch java.lang.AssertionError e (str "Test failed: (= " (second (dict/type-and-val e1)) " " (second (dict/type-and-val e2)) ")" #_(.getMessage e)))
       #_(catch Throwable e (middleware/modify-message e))))

(s/fdef check-equal
  :args (s/and :babel.arity/two))

(defn check-range [v low high] 
  (if (<= low v high) (str "Test (<= " low v high ") passed") (str "Test (<= " low v high ") failed")))

(s/fdef check-range
  :args (s/and :babel.arity/three (s/tuple number? number? number?) ))

(defn check-precision [expected actual precision]
  (if (<= (- expected precision) actual (+ expected precision)) (str "Test passed: " actual "is within " precision " of " expected) 
      (str "Test failed: " actual "is not within " precision " of " expected))) ; not sure how this one should be phrased, also might need help on the docstrings

(s/fdef check-precision
  :args (s/and :babel.arity/three (s/tuple number? number? number?)))