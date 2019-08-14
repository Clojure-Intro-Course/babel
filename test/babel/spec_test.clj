(ns babel.spec-test
  (:require
    [logs.utils :as log]
    [babel.non-spec-test :refer [to-log?]]
    [expectations :refer :all]))

;############################################
;### Tests for functions that have specs  ###
;############################################

;; TO RUN tests, make sure you have repl started in a separate terminal
; (expect "The second argument of (take 9 8) was expected to be a sequence but is a number 9 instead."
; (log/babel-test-message "(take 9 9)"))
(expect #(not= % nil)  (log/set-log babel.non-spec-test/to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;Insufficient Input;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(expect "Wrong number of arguments, expected in (map ): the function map expects one or more arguments but was given no arguments"
(log/babel-test-message "(map)"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;Extra Output;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(expect "Wrong number of arguments, expected in (contains? {} \"a\" #{}): the function contains? expects two arguments but was given three arguments" (log/babel-test-message "(contains? {} \"a\" #{})"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;First Argument;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(expect "The first argument of (conj \"lijk\" \"jlksdfj\") was expected to be a sequence but is a string \"lijk\" instead."
        (log/babel-test-message "(conj \"lijk\" \"jlksdfj\")"))

(expect "The first argument of (conj 1 \"a\") was expected to be a sequence but is a number 1 instead." (log/babel-test-message "(conj 1 \"a\")"))

(expect "The first argument of (conj 1 \"a\") was expected to be a sequence but is a number 1 instead." (log/babel-test-message "(conj 1 \"a\")" ))

(expect "The first argument of (into 1 even?) was expected to be a sequence but is a number 1 instead." (log/babel-test-message "(into 1 even?)"))

(expect "The first argument of (contains? :a :a) was expected to be a sequence but is a keyword :a instead." (log/babel-test-message "(contains? :a :a)"))

(expect "The first argument of (contains? \"a\" :a) was expected to be a sequence but is a string \"a\" instead." (log/babel-test-message "(contains? \"a\" :a)"))

(expect "The first argument of (contains? 1 :a) was expected to be a sequence but is a number 1 instead." (log/babel-test-message "(contains? 1 :a)"))

(expect "The first argument of (contains? j :a) was expected to be a sequence but is a character j instead." (log/babel-test-message "(contains? \\j :a)"))

(expect "The first argument of (reduce 4 \"strawberry\") was expected to be a function but is a number 4 instead." (log/babel-test-message "(reduce 4 \"strawberry\")"))

(expect "The first argument of (even? (0 1 2 3 4)) was expected to be a number but is a sequence (0 1 2 3 4) instead." (log/babel-test-message "(even? (range 5))"))

(expect "The first argument of (even? (0 1 2 3 4 5 6 7 8 9)) was expected to be a number but is a sequence (0 1 2 3 4 5 6 7 8 9) instead." (log/babel-test-message "(even? (range 10))"))

(expect "The first argument of (even? ((0 1 2 3 4 5 6 7 8 9) ...)) was expected to be a number but is a sequence ((0 1 2 3 4 5 6 7 8 9) ...) instead." (log/babel-test-message "(even? (range 11))"))

(expect "The first argument of (filter (1)) was expected to be a function but is a list (1) instead." (log/babel-test-message "(filter '(1))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;Second Argument;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(expect "The second argument of (map map map) was expected to be a sequence but is a function map instead."
(log/babel-test-message "(map map map)"))

(expect "The second argument of (map f f) was expected to be a sequence but is a function f instead." (log/babel-test-message "(defn f [x] (+ x 2)) (map f f)"))

(expect "The second argument of (conj {} \"a\") was expected to be a sequence of vectors with only 2 elements or a map with key-value pairs but is a string \"a\" instead."
(log/babel-test-message "(conj {} \"a\")"))

(expect "The second argument of (conj {} []) was expected to be a sequence of vectors with only 2 elements or a map with key-value pairs but is a vector [] instead."
(log/babel-test-message "(conj {} [])"))

(expect "The second argument of (partition 1 1 1 1) was expected to be a sequence but is a number 1 instead." (log/babel-test-message "(partition 1 1 1 1)"))

(expect "The second argument of (partition 1 1 1) was expected to be a sequence but is a number 1 instead." (log/babel-test-message "(partition 1 1 1)"))

(expect "The second argument of (drop 1 :a) was expected to be a sequence but is a keyword :a instead." (log/babel-test-message "(drop 1 :a)"))

(expect "The second argument of (get-in [1] \"a\") was expected to be a sequence but is a string \"a\" instead." (log/babel-test-message "(get-in [1] \"a\")"))

(expect "The second argument of (filter even? odd?) was expected to be a sequence but is a function odd? instead." (log/babel-test-message "(filter even? odd?)"))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;Lazy Sequence;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Expected a number, but a sequence was given instead." (log/babel-test-message "(drop (range 20) (range 20))"))

(expect "Expected a number, but a sequence was given instead." (log/babel-test-message "(rand (range))"))

(expect "Expected a number, but a sequence was given instead." (log/babel-test-message "(rand-int (range 20))"))

(expect "Expected a function, but a number was given instead." (log/babel-test-message "(drop 3 (1 2 3))")) ;fails


(expect "Expected a number, but a sequence was given instead." (log/babel-test-message "(mod (range 5) (range 10))"))


(expect "Expected a number, but a character was given instead." (log/babel-test-message "(map #(> % 5) \"strawberry\")"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;Pass Tests;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect nil (log/babel-test-message "(map char? \"abc\")")) ;; there should be no error for this

(expect nil (log/babel-test-message "(rand)"))

(expect nil (log/babel-test-message "(filter even? '(1 2 3))"))

(expect nil (log/babel-test-message "(filter 2 [])")) ;fails

(expect nil (log/babel-test-message "(take 2 \"hmmmm\")"))

(expect nil (log/babel-test-message "(conj)"))
