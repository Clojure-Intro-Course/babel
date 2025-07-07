(ns check.fns
  (:require [babel.middleware :as middleware]))

(defn- prettify-object [o]
  (cond (string? o) (str "\"" o "\"")
        (= (type o) clojure.lang.LazySeq) (pr-str o)
        :else o))

(defn check-equal [e1 e2]
  (try (if (nil? (assert (= e1 e2))) (str "Test (= " (prettify-object e1) " " (prettify-object e2) ") passed"))
       (catch java.lang.AssertionError e (str "Test failed: (= " (prettify-object e1) " " (prettify-object e2) ")" #_(.getMessage e)))
       (catch Throwable e (middleware/modify-message e))))

