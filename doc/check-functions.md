### check-equal
 `(check-equal expected actual)`

 ```
Takes two parameters expected and actual, returns a string 'Test (= v1 v2) passed' if their values are equal,
and 'Test (= v1 v2) failed' otherwise, where v1 and v2 are the results of evaluating expected and actual respectively.
 ```

```clojure
(check-equal 1 2)
;;=> "Test (= 1 2) failed"
(check-equal 10 10)
;;=> "Test (= 10 10) passed"
(check-equal "hello" "hello")
;;=> "Test (= "hello" "hello") passed"
(check-equal {:key "word", :word "key"} (hash-map :key "word" :word "key"))
;;=> "Test (= {:key "word", :word "key"} {:key "word", :word "key"}) passed"
```


### check-range
`(check-range n low high)`

```
Takes a number n, and two numbers, low and high. Returns 'Test (<= low n high) passed' if v is between low and high, 
and 'Test (<= low n high) failed' otherwise.
```

```clojure
(check-range 15 10 20)
;;=> "Test (<= 10 15 20) passed"
(check-range 1 2 3)
;;=> "Test (<= 2 1 3) failed"
(check-range 100 0 50)
;;=> "Test (<= 0 100 50) failed"

;; edge cases
(check-range 10 0 10)
;;=> "Test (<= 0 10 10) passed"
(check-range 0 0 10)
;;=> "Test (<= 0 0 10) passed"
```

### check-precision
`(check-precision expected actual precision)`

```
Takes three numbers: the expected value, the actual value, and the precision. 
Returns 'Test passed: actual is within precision of expected' if the difference between actual and expected is less than or equal to precision, 
and 'Test failed: actual is not within precision of expected' otherwise.
```

```clojure
(check-precision 1 2 0.5)
;;=> "Test failed: 2 is not within 0.5 of 1"
(check-precision 1 1.5 1)
;;=> "Test passed: 1.5 is within 1 of 1"

;; edge cases
(check-precision 1 1.1 0.1)
;;=> "Test passed: 1.1 is within 0.1 of 1"
(check-precision 1 0.9 0.1)
;;=> "Test passed: 0.9 is within 0.1 of 1"
```

