# Introduction to Babel

:warning: This documentation is a work-in-progress. Some information may be incomplete, misleading, or lacking clarity. 

Sections that are uncertain or incomplete are marked with :question:. Sections that are likely incorrect, or may become incorrect in the future, are marked with :warning:.

If you notice something that should be changed, please open an issue about it and tag the issue with `documentation`. Thanks!

---

The purpose of this write-up is to help new and future contributors to the project, primarily students, familiarize themselves with the purpose and usage of Babel. We also describe Babel's design, particularly how it performs its error analysis.

This document assumes only surface-level knowledge of Clojure. We expect that the reader at least possesses a level of understanding about Clojure equivalent to playing around with the language for a couple of weeks or so. We do our best to explain the necessary mechanical details about the language here.

Surface-level knowledge about Clojure's REPL works is beneficial. Practice with using the `spec` library and manipulating Clojure error data is even more useful. Links to resources and documentation are provided at the end of this document.

### Table of Contents

- [Overview](link)
  - [What is Babel?](link)
  - [Motivation](link)
- [Getting started](link)
  - [Prerequisites](link)
  - [Using the REPL](link)
  - [Loading files](link)
  - [Using Babel in other Leiningen projects](link)
- [Top-level project structure](link)
  - [Source code](link)
  - [Test code](link)
  - [Documentation](link)
  - [project.clj](link)
- [Internal structure and error analysis](link)
  - [Catching errors and analyzing them](link)
  - [Spec errors](link)
  - [Babel specs](link)
  - :question: [more to come]
- [References and further reading](link)

# Overview

### What is Babel?

Babel is a "proof-of-concept tool for transforming Clojure's error messages into beginner-friendly forms." 

The objective of Babel is to catch REPL errors, analyze them, extract only the most useful information, and produce new error data that is more digestible for new programmers.

:question: Babel accomplishes this by "hooking" onto a running [Leiningen nREPL](https://nrepl.org/nrepl/usage/server.html#using-leiningen) server. It configures the `:caught` flag in the REPL to call a high-level `(modify-message)` function when an exception or error is thrown during the read/eval/print phases[^1].

[^1]: To be more specific, the function used by Babel, `nrepl.middleware.caught/*caught-fn*`, is a wrapper over the `clojure.main/repl-caught` function, the default procedure for what Clojure does when the REPL returns an error/exception. We use it to tell the REPL to instead run our function. 

### Motivation

Error message output in Clojure is often difficult to parse and understand without detailed knowledge of the language, in particular, the Java Virtual Machine that Clojure is built on top of. Clojure inherits and uses Java's exception objects, which often are produced at the JVM level, resulting in Clojure errors focusing on internal details that the end user does not need to know about[^2].

[^2]: The details of this are described more carefully in Charlot Shaw's MICS 2018 paper: https://github.com/Clojure-Intro-Course/mics2018demo/blob/master/mics2018.pdf.

Here is one of many examples of a common syntactic mistake in Clojure:

```clojure
(defn string-or-nil
  [s]
  (if (and (string? s) (not (nil? s)))
    (s)
    (str "String was nil")))
```
```
user=> (string-or-nil "hello")
Execution error (ClassCastException) at user/string-or-nil (REPL:4).
class java.lang.String cannot be cast to class clojure.lang.IFn (java.lang.String is in module java.base of loader 'bootstrap'; clojure.lang.IFn is in unnamed module of loader 'bootstrap')
```

The mistake is small, yet obvious to an experienced Clojure programmer; the argument `s` was simply put in a function position. However, the error message appears cryptic, and does not clarify the mistake at all.

Confusions like these undoubtedly make it needlessly difficult for beginners to spot mistakes and debug their code. Therefore, there is a need for tooling to abstract the details of Clojure error data in a way that is more presentable and comprehensible. Babel is the foundation of our approach to this problem.

# Getting started

### Prerequisites

- [Java](https://www.oracle.com/java/technologies/downloads/) version 11 or later
- [Clojure](https://clojure.org/guides/getting_started) version 1.11.1 or later
- [Leiningen](https://leiningen.org/), :question: any version

### Using the REPL

Clone the repository and make sure you are `cd`'ed into the project directory. With Leiningen installed, run `lein repl` on the command line to launch an nREPL instance. From here, rather than the usual `user` [namespace](https://clojure.org/reference/namespaces), you should be in `babel.middleware` instead.

⚠️ To finish the setup, call the `(setup-exc)` function in the REPL (may change in the future). 

To show this, try calling `(fn {} "test")` and see that the modified output contains an improved analysis of the syntax mistake:

```
Syntax problems with (fn {} "test"):
A function definition requires a vector of parameters, but was given {} instead.
...
```

...as opposed to the default message reported by Clojure:

```
Syntax error macroexpanding clojure.core/fn at (...)
{} - failed: vector? at: [:fn-tail :arity-1 :params] spec: :clojure.core.specs.alpha/param-list
{} - failed: (or (nil? %) (sequential? %)) at: [:fn-tail :arity-n] spec: :clojure.core.specs.alpha/params+body
```

### Loading files

Files cannot be run using `lein run`.

Follow these steps to load outside files:

1. Start the Babel nREPL with `lein repl` (see previous section).
2. Run `(load-file "<filepath>")`
3. Run `(<file namespace>/<function>)`. If you want to run a main function, it should be named `-main`.

Files cannot be run if they make use of dependencies not specified in
babel's project.clj file. So, if you want to use something like
`clojure.math.combinatorics` in a file, you will need to add
`[org.clojure/math.combinatorics "0.1.5"]` to the dependencies in
project.clj.

### Using Babel in other Leiningen projects

:warning: [WIP]

# Top-level project structure

### Source code

The code for Babel is located in the [src](/src) directory, which contains multiple sub-directories that handle various aspects of the project. These are covered in the [internal structure](link) section.

### Test code

Unit tests for Babel can be found in the [test](/test/babel) directory. See also the notes on running tests and [logging](doc/logging.md).

### Documentation

Files documenting Babel's development are located in the [doc](/doc) directory. You are here!




# ...

# :warning: References and further reading (WIP)

- nREPL documentation on catching errors interactively:
  - [Docstrings](https://cljdoc.org/d/nrepl/nrepl/1.3.0/api/nrepl.middleware.caught)
  - [Source code](https://github.com/nrepl/nrepl/blob/v1.3.0/src/clojure/nrepl/middleware/caught.clj#L20)

- [Paper presented at MICS 2018 describing the need and potential for better Clojure error messages, by Charlot Shaw](https://github.com/Clojure-Intro-Course/mics2018demo/blob/master/mics2018.pdf)

- Closer look at Clojure errors:
  - [Phases and ex-data keys](https://clojure.org/reference/repl_and_main#_error_printing)


---
---

# Incomplete junk

NOTE: about how babel/middleware.clj works
It makes use of `nrepl.middleware.caught`, which is essentially a wrapper function applied to the Clojure REPL's `:caught` option (more on this below), to dynamically modify any error data returned by the server before the user sees them.


[Learn Clojure](https://clojure.org/guides/learn/syntax).