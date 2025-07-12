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
       (catch java.lang.AssertionError e (str "Test failed: (= " (prettify-object e1) " " (prettify-object e2) ")" #_(.getMessage e)))
       #_(catch Throwable e (middleware/modify-message e))))

(s/fdef check-equal
  :args (s/tuple object? object?))
