
# Exception Exploration

## Introduction

this write-up discusses the `src/utilities/exception_exploration.clj` file and functionality. It provides the functions and how they are used with examples.

### Background

While working with nested exceptions, we pulled from a large group of error test cases we already had. We had some questions about them

* How many of these exceptions involve multiple levels of nesting?
* Which combinations of error types frequently occur within those nested exceptions?

We then set out to build a tool to help search through these exceptions more effectively. The goal was to support flexible searching across a variety of cases—such as:

* Phase of error
* Levels of exception nesting
* The nested error types

[Functions](#functions)
[Tests](#tests)
[Generating the Log File](#generating-the-log-file)
[Example Run](#example-run)

## Functions

### Setup
The following needs a "ex.txt" file for the actual logs to read in `log/code-ex-triage/ex.txt` This can be obtained by running `lein expectations` in another terminal while REPL is running and renaming the returned file. 
1. In order to use the functions you must be in the babel REPL and run `(require '[utilities.exception_exploration :as exploration])`
2. run `(def parsed-logs (exploration/parse-logs "ex.txt"))` in order to get the logs for the input in the later functions.
3. If you want to run a function on a single log, you can use something like `(def log0 (parsed-logs 0))`

### Filter-Search
Filters a vector of logs based on a search map of key-value pairs. Returns only the logs that match all specified criteria.
`filter-search [logs search-map]`

search-map should include zero or more of the following key-value pairs:
`:phase` and a valid phase
`:nesting` and an integer (level of nesting)
`:code` and a code snippet string
`:type` - **WIP**

Example:
`(exploration/filter-search parsed-logs {:phase :read-source 
:nesting  3 :code "8.5.1"})`

### Multiple-Log Functions

#### parse-logs
takes a name of a log file in `./log/code-ex/triage`, parses the babel log output, and returns a vector of logs.
`parse-logs [file]`

Example:
`(exploration/parse-logs "ex.txt")`

#### filter-by-phase
Takes a vector of logged exceptions and a phase and returns the logs that have the given phase.
`filter-by-phase [logs phase]`

#### filter-by-nesting
Takes a vector of logged exceptions and an integer level of nesting and returns the logs that have the given level of nesting.

**WIP**
.#### filter-by-type
.#### filter-by-subtype

#### filter-by-code
Takes a vector of logged exceptions and an string with "code" in it and returns the logs that have that exact code in it.
`filter-by-code [logs code]`

Example:
`(exploration/filter-by-code parsed-logs "(/ 9 0)")`


### Single-Log Functions

#### read-log
Takes a HashMap of a logged exception and converts strings to HashMaps as needed.
`read-log [log]`

#### get-phase
Takes a logged exception and returns the phase of the exception.
`get-phase [log]`

#### get-level-nesting
Takes a logged exception and returns the level of nesting of the errors
`get-level-nesting [log]`

#### get-nested-types
Takes a logged exception and returns the types of errors in the exception
`get-nested-types [log]`

## Tests
### Running the Tests
Evaluate the following expressions in the REPL:
```
(require '[clojure.test :refer :all])
(require '[utilities.exception_exploration :as exploration])
(run-tests 'utilities.exception_exploration)
```

## Generating the Log File

Start up a REPL in the Babel repository using `lein repl`. Open another terminal and type `lein expectations`. This should create a file in `babel/log/code-ex-triage/`. The resulting file is the file named ex.txt in the example below.

## Example Run

I want to look at the exception for `(/ 9 0)` 

```lein repl`
=> (require '[utilities.exception_exploration :as exploration])
=> (def parsed-logs (exploration/parse-logs "ex.txt"))
=> (exploration/filter-search parsed-logs {:code "(/ 9 0)"})```

`({:code "(/ 9 0)", :exception {:cause "Divide by zero", :via ({:at [clojure.lang.Numbers divide "Numbers.java" 190], :message "Divide by zero", :type java.lang.ArithmeticException})}, :ex-triage {:class java.lang.ArithmeticException, :line 1, :cause "Divide by zero", :symbol babel.middleware/eval4879, :source "form-init684767312890052564.clj", :spec "nil", :phase :execution}})`
what is the level of nesting?
`=>(exploration/get-level-nesting (first (exploration/filter-se
arch parsed-logs {:code "(/ 9 0)"})))`
returns:
`1`

Now I want to know what else has 1 level of nesting and phase execution

`=> (exploration/filter-search parsed-logs {:phase :execution :nesting 1})`
It returns a large vector of logs. How many?
`=>(count (exploration/filter-search parsed-logs {:phase :execution :nesting 1}))`
returns:
`145`
