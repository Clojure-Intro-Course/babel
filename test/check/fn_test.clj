(ns check.fn-test
  (:require [clojure.test :refer :all]
            [check.fns :as fns]))

(deftest test-check-equal 
  (testing "Testing the check-equal function"
    (is (fns/check-equal 1 1))
    (is (fns/check-equal "no" "no"))
    (is (= (try (fns/check-equal 1 (/ 1 0))
                (catch java.lang.ArithmeticException e (.getMessage e))) "Divide by zero"))
    (is (= (try (fns/check-equal "hello" (slurp "this-file-does-not-exist.txt"))
                (catch java.io.FileNotFoundException e (.getMessage e))) "this-file-does-not-exist.txt (No such file or directory)")) ; interesting. I would expect the message to just be "No such file or directory" - I guess babel is working then
    )) 