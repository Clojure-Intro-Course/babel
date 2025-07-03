(ns check.fn-test
  (:require [clojure.test :refer :all]
            [check.fns :as fns]))

(deftest test-check-equal 
  (testing "Testing the check-equal function"
    (is (fns/check-equal 1 1))
    (is (fns/check-equal "no" "no"))
    ))
