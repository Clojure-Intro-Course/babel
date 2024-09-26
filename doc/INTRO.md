# Introduction to Babel

The purpose of this write-up is to help new and future contributors to the project, primarily students, familiarize themselves with the purpose and usage of Babel. We also describe Babel's design, particularly how it performs its error analysis.

This document assumes only surface-level knowledge of Clojure. We expect that the reader at least possesses a level of understanding about Clojure equivalent to playing around with the language for a couple of weeks or so. We do our best to explain the necessary mechanical details about the language here.

Surface-level knowledge about Clojure's REPL works is beneficial. Practice with using the `spec` library and manipulating Clojure error data is even more useful. Links to resources and documentation are provided at the end of this document.

### Table of Contents

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
  - ...

---

# What is Babel?

Babel is a "proof of concept tool for transforming error message in Clojure into beginner-friendly forms." 

It makes use of Clojure's built-in `spec` library 

The goal of Babel is to catch errors and exceptions thrown by the interpreter, analyze their contents, extract the most relevant information for learners of Clojure, and produce a more comprehensive error message for efficient learning and debugging. Babel is a piece of middleware (like an interface between the interpreter and the end user) for nREPL, which is the Clojure REPL that Leiningen, our project manager, runs.

# Motivation
Most error message output in Clojure is difficult to understand and sort through without detailed knowledge of the language (see the accompanying piece that looks at Clojure errors in more detail). This is partially due to the fact that Clojure (a dynamically-typed language) is built on top of Java (a statically-typed language) and inherits its exception objects, which Clojure must then make sense of using its own data types, and often fails to do well. 

The problems with Clojure's error messages can be seen in its verbose stack traces, type casting, and inconsistent/unclear usage of specs, especially when it comes to anonymous functions and expanding macros. 

This can cause beginners to feel easily confused and overwhelmed, making it harder to spot mistakes and perform debugging. We would like tooling to abstract the details of Clojure error data in a way that is more presentable and comprehensible. Babel is the basis of our approach to this.




[Learn Clojure](https://clojure.org/guides/learn/syntax).