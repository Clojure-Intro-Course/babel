(ns check.fn-test
  (:require [clojure.test :refer :all]
            [check.fns :as fns]
            [errors.dictionaries :as dict]
            [clojure.main :refer [ex-triage]]))

(defn- ex-helper [error kw]
  (-> error
      Throwable->map
      ex-triage
      :clojure.error/spec
      :clojure.spec.alpha/problems
      first
      kw))

(deftest test-check-equal
  (testing "Testing the check-equal function"
    (is (= (fns/check-equal 1 1) "Test (= 1 1) passed"))
    (is (= (fns/check-equal "no" "no") "Test (= \"no\" \"no\") passed"))
    (is (= (try (fns/check-equal 1 (/ 1 0))
                (catch java.lang.ArithmeticException e (.getMessage e))) "Divide by zero"))
    (is (= (try (fns/check-equal "hello" (slurp "this-file-does-not-exist.txt"))
                (catch java.io.FileNotFoundException e (.getMessage e))) "this-file-does-not-exist.txt (No such file or directory)")) ; interesting. I would expect the message to just be "No such file or directory" - I guess babel is working then
    (is (= (fns/check-equal (map inc [1 2 3]) '(2 3 4)) "Test (= (2 3 4) (2 3 4)) passed"))
    (is (= (fns/check-equal "this is a string" (str "this is " (first "abcdefghijklmnopqrstuvwxyz") " string")) "Test (= \"this is a string\" \"this is a string\") passed"))
    (is (= (try (fns/check-equal 1 1 1)
                (catch Throwable e (ex-helper e :via))) [:babel.arity/two]))
    (is (= (try (fns/check-equal 1)
                (catch Throwable e (ex-helper e :via))) [:babel.arity/two]))
    (is (= (try (fns/check-equal "hello")
                (catch Throwable e (ex-helper e :via))) [:babel.arity/two]))

    ))

;; (deftest test-prettify-object﻿
;;   (testing "Testing the prettify-object function"
;;     (is (= (fns/prettify-object 1) 1))
;;     (is (= (fns/prettify-object "string") "string"))
;;     (is (= (fns/prettify-object [1 2 3 4]) [1 2 3 4]))
;;     (is (= (fns/prettify-object (lazy-seq [1 2 3 4 5])) "(1 2 3 4 5)")) ; this will turn lazy sequences into strings, but not regular sequences 
;;     ;; not sure if we want to keep that behavior, since pr-str is the only way we know how to evaluate lazy sequences
;;     (is (= (try (fns/prettify-object (/ 1 0))
;;                 (catch java.lang.ArithmeticException e (.getMessage e))) "Divide by zero"))))

(deftest test-check-range
  (testing "Testing the check-range function"
    (is (= (fns/check-range 1 0 2) "Test (<= 0 1 2) passed"))
    (is (= (fns/check-range 1.1 1.0 1.2) "Test (<= 1.0 1.1 1.2) passed"))
    (is (= (fns/check-range 0 1 2) "Test (<= 1 0 2) failed"))
    (is (= (fns/check-range 1.0 1.1 1.2) "Test (<= 1.1 1.0 1.2) failed"))
    (is (= (try (fns/check-range "NaN" 0 1)
                (catch Throwable e (ex-helper e :via))) [:babel.type/number-or-lazy :babel.type/number :babel.type/number])) ; is this normal? this seems weird to me. at no point did I specify any kind of spec that uses :babel.type/number
    ))

(deftest test-check-precision
  (testing "Testing the check-precision function"
    (is (= (fns/check-precision 1 1 1) "Test passed: 1 is within 1 of 1"))
    (is (= (fns/check-precision 1 2 1) "Test passed: 2 is within 1 of 1"))
    (is (= (fns/check-precision 1 1.1 0.2) "Test passed: 1.1 is within 0.2 of 1"))
    (is (= (fns/check-precision 1 -1 1) "Test failed: -1 is not within 1 of 1"))
    (is (= (fns/check-precision 1 1.4 0.2) "Test failed: 1.4 is not within 0.2 of 1"))
    (is (= (fns/check-precision 1/3 (/ 1 3.0) 0) "Test passed: 0.3333333333333333 is within 0 of 1/3"))
    (is (= (fns/check-precision 0.40 (+ 0.1 0.3) 0.0000001) "Test passed: 0.4 is within 1.0E-7 of 0.4"))
    ;; add tests to check if babel error messages work properly
    ))



;; does = do deep check or shallow check? compare hashmaps and strings and stuff that are formed in different ways (not a priority)
;; test the specs (DO THIS SOON)
;; game/h
