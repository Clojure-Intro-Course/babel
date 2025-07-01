(ns check.fns)
(defn check-equal [e1 e2]
  (try (assert (= e1 e2))
       (catch java.lang.AssertionError e (str "Test failed:" (.getMessage e)))))