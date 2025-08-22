### check-equal
 `(check-equal expected actual)`

 ```
Takes two parameters expected and actual, returns a string 'Test (= v1 v2) passed' if their values are equal,
and 'Test (= v1 v2) failed' otherwise, where v1 and v2 are the results of evaluating expected and actual respectively.
 ```


### check-range
`(check-range n low high)`

```
Takes a number n, and two numbers, low and high. Returns 'Test (<= low n high) passed' if v is between low and high, 
and 'Test (<= low n high) failed' otherwise.
```

### check-precision
`(check-precision expected actual precision)`

```
Takes three numbers: the expected value, the actual value, and the precision. 
Returns 'Test passed: actual is within precision of expected' if the difference between actual and expected is less than or equal to precision, 
and 'Test failed: actual is not within precision of expected' otherwise.
```

