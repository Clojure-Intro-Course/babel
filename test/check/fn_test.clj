(ns check.fn-test
  (:require [clojure.test :refer :all]
            [check.fns :as fns]
            [errors.dictionaries :as dict]
            [expectations :refer [expect]]
            [logs.utils :as log]
            [babel.utils-for-testing :as t] [babel.non-spec-test]
            [clojure.main :refer [ex-triage]]))



(expect #(not= % nil) (log/set-log babel.non-spec-test/to-log?))

(expect nil (log/add-log
             (do
               (def file-name "this file")
               (:file (meta #'file-name)))))

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
                (catch Throwable e (ex-helper e :via))) [:babel.arity/two])) ; fails on number before failing on type

    ))



(expect "Test (= 1 1) passed" (fns/check-equal 1 1))
(expect "Test (= \"no\" \"no\") passed" (fns/check-equal "no" "no"))
(expect (t/make-pattern"Tried to divide by zero") (log/babel-test-message "(require `check.fns) (check.fns/check-equal 1 (/ 1 0))"))
(expect (t/make-pattern "The file this-file-does-not-exist.txt does not exist.") (log/babel-test-message "(slurp \"this-file-does-not-exist.txt\")"))
(expect "Test (= (2 3 4) (2 3 4)) passed" (fns/check-equal (map inc [1 2 3]) '(2 3 4)))
(expect "Test (= \"this is a string\" \"this is a string\") passed" 
        (fns/check-equal "this is a string" (str "this is " (first "abcdefghijklmnopqrstuvwxyz") " string")))
(expect (t/make-pattern "Wrong number of arguments in (check-equal 1 1 1): the function check-equal expects two arguments but was given three arguments.") (log/babel-test-message "(require `check.fns) (check.fns/check-equal 1 1 1)")) 
(expect (t/make-pattern "Wrong number of arguments in (check-equal 1): the function check-equal expects two arguments but was given one argument.") (log/babel-test-message "(require `check.fns) (check.fns/check-equal 1)")) 
(expect (t/make-pattern"Wrong number of arguments in (check-equal \"hello\"): the function check-equal expects two arguments but was given one argument.") (log/babel-test-message "(require `check.fns) (check.fns/check-equal \"hello\")")) ; should fail on number of arguments first before checking type




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
    (is (= (try (fns/check-range "NaN" (lazy-seq [1 2 3 4]) 1)
                (catch Throwable e (ex-helper e :via))) [:babel.type/number-or-lazy :babel.type/number :babel.type/number])) ; what
    ))

(expect "Test (<= 0 1 2) passed" (fns/check-range 1 0 2))
(expect "Test (<= 1.0 1.1 1.2) passed" (fns/check-range 1.1 1.0 1.2))
(expect "Test (<= 1 0 2) failed" (fns/check-range 0 1 2))
(expect "Test (<= 1.1 1.0 1.2) failed" (fns/check-range 1.0 1.1 1.2))
(expect (t/make-pattern "The first argument of (check-range \"NaN\" 0 1) was expected to be a number but is a string \"NaN\" instead.") (log/babel-test-message "(require `check.fns) (check.fns/check-range \"NaN\" 0 1)"))
(expect (t/make-pattern "The first argument of (check-range \"NaN\" (1 2 3 4) 1) was expected to be a number but is a string \"NaN\" instead.") (log/babel-test-message "(require `check.fns) (check.fns/check-range \"NaN\" (lazy-seq [1 2 3 4]) 1)"))


(deftest test-check-precision
  (testing "Testing the check-precision function"
    (is (= (fns/check-precision 1 1 1) "Test passed: 1 is within 1 of 1"))
    (is (= (fns/check-precision 1 2 1) "Test passed: 2 is within 1 of 1"))
    (is (= (fns/check-precision 1 1.1 0.2) "Test passed: 1.1 is within 0.2 of 1"))
    (is (= (fns/check-precision 1 -1 1) "Test failed: -1 is not within 1 of 1"))
    (is (= (fns/check-precision 1 1.4 0.2) "Test failed: 1.4 is not within 0.2 of 1"))
    (is (= (fns/check-precision 1/3 (/ 1 3.0) 0) "Test passed: 0.3333333333333333 is within 0 of 1/3"))
    (is (= (fns/check-precision 0.40 (+ 0.1 0.3) 0.0000001) "Test passed: 0.4 is within 1.0E-7 of 0.4")) 
    ))

(expect "Test passed: 1 is within 1 of 1" (fns/check-precision 1 1 1))
(expect "Test passed: 2 is within 1 of 1" (fns/check-precision 1 2 1))
(expect "Test passed: 1.1 is within 0.2 of 1" (fns/check-precision 1 1.1 0.2))
(expect "Test failed: -1 is not within 1 of 1" (fns/check-precision 1 -1 1))
(expect "Test failed: 1.4 is not within 0.2 of 1" (fns/check-precision 1 1.4 0.2))
(expect "Test passed: 0.3333333333333333 is within 0 of 1/3" (fns/check-precision 1/3 (/ 1 3.0) 0))
(expect "Test passed: 0.4 is within 1.0E-7 of 0.4" (fns/check-precision 0.40 (+ 0.1 0.3) 0.0000001))

(expect "Found 5" (fns/has-element [1 2 3 4 5] 5))
(expect "Found 3, Found 1" (fns/has-element [1 3 4 7] 3 1))
(expect "Found 1, Found 3" (fns/has-element [1 3 4 7] 1 3)) ;; the order of the outputs depends on the order of the input elements to find
(expect "Found \"hello\", Found \"not here\"" (fns/has-element '("hello" "goodbye" "I'm here" "not here") "hello" "not here"))
(expect "Did not find 7, Found \"weee\"" (fns/has-element [1 3 "weee" "we" "weeee" "Wii"] 7 "weee"))




