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

(defn check-equal 
  "Takes two objects e1 and e2, returns a string 'Test (= <e1> <e2>) passed' if they are equal or 'Test (= <e1> <e2>) failed' otherwise."
  [e1 e2]
  (try (if (nil? (assert (= e1 e2))) (str "Test (= " (second (dict/type-and-val e1)) " " (second (dict/type-and-val e2)) ") passed"))
       (catch java.lang.AssertionError e (str "Test failed: (= " (second (dict/type-and-val e1)) " " (second (dict/type-and-val e2)) ")" #_(.getMessage e)))
       #_(catch Throwable e (middleware/modify-message e))))

(s/fdef check-equal
  :args (s/and :babel.arity/two))

(defn check-range 
  "Takes a number v, and two numbers, low and high. Returns 'Test (<= <low> <v> <high>) passed' if v is between low and high, and 'Test (<= <low> <v> <high>) failed' otherwise."
  [v low high] 
  
  (if (<= low v high) (str "Test (<= " low v high ") passed") (str "Test (<= " low v high ") failed")))

(s/fdef check-range
  :args (s/and :babel.arity/three (s/tuple number? number? number?)))

(defn check-precision 
  "Takes three numbers: the expected value, the actual value, and the precision. Returns 'Test passed: <actual> is within <precision> of <expected>' if the difference between actual and expected is less than or equal to precision, and 'Test failed: <actual> is not within <precision> of <expected>' otherwise."
  [expected actual precision]
  (if (<= (- expected precision) actual (+ expected precision)) (str "Test passed: " actual "is within " precision " of " expected) 
      (str "Test failed: " actual "is not within " precision " of " expected))) ; not sure how this one should be phrased, also might need help on the docstrings

(s/fdef check-precision
  :args (s/and :babel.arity/three (s/tuple number? number? number?)))