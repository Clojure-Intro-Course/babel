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
    (is (fns/check-equal (map inc [1 2 3]) "(2 3 4)")))) ; don't know if I like this better than my version

(deftest test-prettify-object
  (testing "Testing the prettify-object function"
    (is (= (fns/prettify-object 1) 1))
    (is (= (fns/prettify-object "string") "string"))
    (is (= (fns/prettify-object [1 2 3 4]) [1 2 3 4]))
    (is (= (fns/prettify-object (lazy-seq [1 2 3 4 5])) "(1 2 3 4 5)")) ; this will turn lazy sequences into strings, but not regular sequences 
    ;; not sure if we want to keep that behavior, since pr-str is the only way we know how to evaluate lazy sequences
    (is (= (try (fns/prettify-object (/ 1 0))
                (catch java.lang.ArithmeticException e (.getMessage e))) "Divide by zero"))))

(deftest test-check-range
  (testing "Testing the check-range function"
    (is (= (fns/check-range 1 0 2) "Test (<= 0 1 2) passed"))
    (is (= (fns/check-range 1.1 1.0 1.2) "Test (<= 1.0 1.1 1.2) passed"))
    (is (= (fns/check-range 0 1 2) "Test (<= 1 0 2) failed"))
    (is (= (fns/check-range 1.0 1.1 1.2) "Test (<= 1.1 1.0 1.2) failed"))))

(deftest test-check-precision
  (testing "Testing the check-precision function"
    (is (= (fns/check-precision 1 1 1) "Test passed: 1 is within 1 of 1"))
    (is (= (fns/check-precision 1 2 1) "Test passed: 2 is within 1 of 1"))
    (is (= (fns/check-precision 1 1.1 0.2) "Test passed: 1.1 is within 0.2 of 1"))
    (is (= (fns/check-precision 1 -1 1) "Test failed: -1 is not within 1 of 1"))
    (is (= (fns/check-precision 1 1.4 0.2) "Test failed: 1.4 is not within 0.2 of 1"))))