# Introduction to Babel

:warning: This documentation is a work-in-progress. Some information may be incomplete, misleading, or lacking clarity. 

Sections that are uncertain, incomplete, or may change in the future are marked with :question:. If you notice something that should be changed, please open an issue about it and tag the issue with `documentation`. Thanks!

---

The purpose of this write-up is to help new and future contributors to the project, primarily students, familiarize themselves with the purpose and usage of Babel. We also describe Babel's design, particularly how it performs its error analysis.

This document assumes only surface-level knowledge of Clojure. We expect that the reader at least possesses a level of understanding about Clojure equivalent to playing around with the language for a couple of weeks or so. We do our best to explain the necessary mechanical details about the language and supporting tools here.

Surface-level knowledge about Clojure's REPL works is beneficial. Practice with using the `spec` library and manipulating Clojure error data is even more useful. Links to resources and documentation are provided at the end of this document.

### Table of Contents

- [Overview](#overview)
  - [What is Babel?](#what-is-babel)
  - [Motivation](#motivation)
- [Usage](#usage)
  - [Prerequisites](#prerequisites)
  - [Using the REPL](#using-the-repl)
  - [Loading files](#loading-files)
  - [Using Babel in other Leiningen projects](#using-babel-in-other-leiningen-projects)
- [Top-level project structure](link)
  - [Source code](link)
  - [Test code](link)
  - [Documentation](link)
  - [project.clj](link)
- [Internal structure and error analysis](link)
  - [Catching errors and analyzing them](link)
  - [Dealing with macros](link)
  - [Spec errors via Babel specs](link)
  - [Spec errors via third-party specs](link)
  - [Known problems with Babel's usage of specs](link)
  - [Non-spec errors](link)
  - :question: [more to come]
- [References and further reading](link)

# Overview

### What is Babel?

In short, Babel is a tool to transform Clojure error messages to be more beginner-friendly. The objective of Babel is to catch errors and exceptions from the REPL, analyze them, extract only the most useful information, and produce new error data that is more digestible for new programmers in Clojure.

Babel accomplishes this using a high-level `modify-message` function that takes a Clojure exception map, as returned by `*e`, and categorizes the exception according to its Java type and key structure. It then returns a new error message, the processing of which is delegated to various helper functions that deal with specific kinds of exceptions, such as spec errors, or raw Java exceptions.

:question: Our implementation of Babel "hooks" onto a running [Leiningen nREPL](https://nrepl.org/nrepl/usage/server.html#using-leiningen) server. Babel implements a wrapper over the `clojure.main/repl-caught` function, the default procedure for what Clojure does when the REPL returns an error/exception. We use it to tell the REPL to run our function in place of this default. When `setup-exc` is called, it configures the `:caught` flag in the REPL to instead call `(modify-message)` upon an error/exception.

### Motivation

Because Clojure is built on top of Java and runs its REPL on the Java Virtual Machine (JVM), errors in Clojure are internally constructed as Java exception objects. Clojure lacks many "safety nets" to spot errors before they reach the JVM level, and as a result, most Clojure errors focus heavily on cryptic, low-level details of the language, which don't make sense to users who are unfamiliar with them[^2].

[^2]: The details of this are described more carefully in Charlot Shaw's MICS 2018 paper: https://github.com/Clojure-Intro-Course/mics2018demo/blob/master/mics2018.pdf.

Here is one such example, using a common syntactic mistake in Clojure:

```clojure
(defn palindrome-or-nil
  "Takes a sequence or collection and checks if it's a palindrome (same sequence forwards and backwards).
   Returns the string if it is a palindrome. Otherwise, returns nil."
  [s]
  (if (= (seq s) (reverse s))
    (s)))
```
```
user=> (palindrome? "racecar")
Execution error (ClassCastException) at babel.middleware/palindrome?
class java.lang.String cannot be cast to class clojure.lang.IFn (java.lang.String is in module java.base of loader 'bootstrap'; clojure.lang.IFn is in unnamed module of loader 'app')
```

The mistake is small, though obvious to an experienced Clojure programmer; after the `if` statement in the function call, `s` was mistakenly wrapped in parentheses, and a string can't be in a function position. However, the error message appears cryptic, and does not clarify the mistake at all.

Confusions like these undoubtedly make it needlessly difficult for beginners to spot mistakes and debug their code. Therefore, there is a need for tooling to abstract Clojure error data in a way that is more presentable and comprehensible. Babel is the basis of our approach to this problem.

# Usage

### Prerequisites

- [Java](https://www.oracle.com/java/technologies/downloads/) version 11 or later
- [Clojure](https://clojure.org/guides/getting_started) version 1.11.1 or later
- [Leiningen](https://leiningen.org/), :question: any version

### Using the REPL

Clone the repository and make sure you are `cd`'ed into the project directory. With Leiningen installed, run `lein repl` on the command line to launch an nREPL instance. From here, rather than the default `user` namespace, you should be in `babel.middleware` instead.

⚠️ To finish the setup, call the `(setup-exc)` function in the REPL (:question: may change in the future). 

To show this, try calling `(fn {} "test")` and see that the output contains a precise description of the error:

```
Syntax problems with (fn {} "test"):
A function definition requires a vector of parameters, but was given {} instead.
...
```

...as opposed to the default spec error message reported by Clojure:

```
Syntax error macroexpanding clojure.core/fn at (...)
{} - failed: vector? at: [:fn-tail :arity-1 :params] spec: :clojure.core.specs.alpha/param-list
{} - failed: (or (nil? %) (sequential? %)) at: [:fn-tail :arity-n] spec: :clojure.core.specs.alpha/params+body
```

### Loading files

Files cannot be run using `lein run`.

Follow these steps to load external files:

1. Start the Babel nREPL with `lein repl` (see previous section).
2. Run `(load-file "<filepath>")`
3. Run `(<file namespace>/<function>)`. If you want to run a main function, it should be named `-main`.

Files cannot be run if they make use of dependencies not specified in Babel's project.clj file. So, if you want to use something like `clojure.math.combinatorics` in a file, you will need to add `[org.clojure/math.combinatorics "0.1.5"]` to the dependencies in `project.clj`.

### Using Babel in other Leiningen projects

:warning: [WIP]

# Top-level project structure

### Source code

The code for Babel is located in the [src](/src) directory, which contains multiple sub-directories that handle various aspects of the project. The specific purposes of each sub-directory are covered in the [internal structure](link) section.

### Test code

Unit tests for Babel can be found in the [test](/test/babel) directory. See also: the notes on running tests and [logging](doc/logging.md). :warning: TODO: Add more information detailing how to run tests.

### Documentation

Files documenting Babel's development and usage are located in the [doc](/doc) directory. You are here!

### project.clj

The [project.clj](/project.clj) file contains information about our Leiningen project, including relevant dependencies and plugins. :warning: TODO: Add more to this.

# Internal structure and error analysis

This section of the write-up details the technicalities behind Babel's operation, and is primarily intended for those who plan to contribute to the project. The reader should be familiar with the following concepts:

- Clojure specs, in particular, knowing how to define and instrument them for functions. 

- How Clojure error data is parsed and interpreted, including the various `ex-data` keys, `:phase` values, and how to retrieve this data (using `*e`, `Throwable->map`, or `clojure.main/ex-triage`).

Documentation on all of this is available in the [References and further reading](#️references-and-further-reading) section.

### Catching errors and analyzing them

As previously mentioned, `modify-message` is the core error-processing function for Babel. When it is given a map of exception data, it breaks it up into the relevant components, such as its Java class, Clojure exception type (can be one of `CompilerException` or `ExceptionInfo`), present keys, and level of nesting.

:warning: Some of the specific details described below may change in the future with further refactoring and cleanup.

As it stands right now, `modify-message` categorizes an error into one of four possible patterns:

1. Spec failures on Clojure macros (like `let`, `defn`, `when-let`, etc.)
2. "Invalid signature" errors on macros (see below)
3. Spec failures on functions
4. Non-spec errors (things like syntax errors, arithmetic errors, etc. that can't be generalized using spec information)

Let's look at each of these patterns individually.

### Dealing with macros

:warning: TODO: Elena, how do we justify the need for a difference in the way macro specs are handled as opposed to regular function specs? What do macro specs have that distinguish them from function specs?

### Spec errors

Since Clojure is a dynamically-typed language, there is a lot that can go wrong if unexpected data types are passed to a function. Additionally, the number of arguments that a function can take is not always clarified by Clojure's native error messages.

Fortunately, Clojure's spec library offers an intuitive way to validate the arguments of a function via the `clojure.spec.test.alpha/instrument` function. Using `clojure.spec.alpha/fdef`, we can define allowed behaviors for a function and its arguments, and by instrumenting the spec, we can override the error message thrown to signify when a call to the spec'd function doesn't conform to the spec, before trying to evaluate it.

Babel makes use of these capabilities in its handling of errors caused when core Clojure functions are called with invalid types or number of arguments. For most functions found in `clojure.core`, Babel creates and instruments function specs in the `corefns.clj` file, which are constructed from predefined predicates and specs on sequences (since function arguments are sequential).

These specs produce uniform errors on improper function calls, which are then easily understood by `modify-message`, since spec errors use `clojure.lang.ExceptionInfo` and have more comprehensive error data (as opposed to `clojure.lang.CompilerException`).

### Spec errors via third-party specs

Babel is, to some extent, capable of handling third-party specs as well, in which case the error messages report on the failed predicates.

```clojure
(require '[clojure.spec.alpha :as s] 
         '[clojure.spec.test.alpha :as stest])

(defn back-to-back
  "Concatenates the first string with the reverse of the second string."
  [s1, s2]
  (apply str (concat (seq s1) (reverse s2))))

(s/fdef back-to-back
  :args (s/cat :arg-one #(string? %), :arg-two #(string? %)))

(stest/instrument `back-to-back)
```
```
babel.middleware=> (back-to-back [1 2] [4 3])
In (back-to-back [1 2] [4 3]) the first argument, which is a vector [1 2], fails a requirement: (clojure.core/fn [%] (clojure.core/string? %))
```

### Known problems with Babel's usage of specs

:warning: WIP, information here will relate to the inlining issues and naming schemes for specs. Macros are difficult or nearly impossible to spec correctly.

### Non-spec errors


# References and further reading

:warning: WIP

- nREPL documentation on catching errors interactively:
  - [Docstrings](https://cljdoc.org/d/nrepl/nrepl/1.3.0/api/nrepl.middleware.caught)
  - [Source code](https://github.com/nrepl/nrepl/blob/v1.3.0/src/clojure/nrepl/middleware/caught.clj#L20)

- [Paper presented at MICS 2018 describing the need and potential for better Clojure error messages, by Charlot Shaw](https://github.com/Clojure-Intro-Course/mics2018demo/blob/master/mics2018.pdf)

- Closer look at Clojure errors:
  - [Phases and ex-data keys](https://clojure.org/reference/repl_and_main#_error_printing)

---

# Incomplete junk


[Learn Clojure](https://clojure.org/guides/learn/syntax).