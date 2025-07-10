(ns check.fn-test
  (:require [clojure.test :refer :all]
            [check.fns :as fns]
            [errors.dictionaries :as dict])
  )

(deftest test-check-equal 
  (testing "Testing the check-equal function"
    (is (fns/check-equal 1 1))
    (is (fns/check-equal "no" "no"))
    (is (= (try (fns/check-equal 1 (/ 1 0))
                (catch java.lang.ArithmeticException e (.getMessage e))) "Divide by zero"))
    (is (= (try (fns/check-equal "hello" (slurp "this-file-does-not-exist.txt"))
                (catch java.io.FileNotFoundException e (.getMessage e))) "this-file-does-not-exist.txt (No such file or directory)")) ; interesting. I would expect the message to just be "No such file or directory" - I guess babel is working then
    (is (fns/check-equal (map inc [1 2 3]) "a sequence (2 3 4)")))) ; don't know if I like this better than my version

(deftest test-prettify-object
  (testing "Testing the prettify-object function"
    (is (= (fns/prettify-object 1) 1))
    (is (= (fns/prettify-object "string") "string"))
    (is (= (fns/prettify-object [1 2 3 4]) [1 2 3 4]))
    (is (= (fns/prettify-object (lazy-seq [1 2 3 4 5])) "(1 2 3 4 5)")) ; this will turn lazy sequences into strings, but not regular sequences 
    ;; not sure if we want to keep that behavior, since pr-str is the only way we know how to evaluate lazy sequences
    (is (= (try (fns/prettify-object (/ 1 0))
                (catch java.lang.ArithmeticException e (.getMessage e))) "Divide by zero"))))