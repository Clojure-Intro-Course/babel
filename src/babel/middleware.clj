(ns babel.middleware
  (:require [babel.processor :as processor]
            [errors.prettify-exception :as p-exc]
            [nrepl.middleware]
            [nrepl.middleware.caught]
            [clojure.repl]
            [clojure.main :as cm])
  (:gen-class))

(def track (atom {})) ; For debugging and testing purposes.

(defn- record-message
  [e]
  (cm/ex-str (cm/ex-triage (Throwable->map e))))

(defn- str-ex->vec
  "Takes processed exception messages as produced in modify-message and returns
   a tagged vector of pairs.
   If the first argument is already tagged, calls conj on x and tags remaining xs.
   If the first argument is a string, then tags all arguments.
   We are assuming that the first case implies remaining arguments are all strings.
   This may change in the future as we start tagging things like location/stack keywords."
  [x & xs]
  (if (vector? x) 
      (into x (map #(vector :txt %) xs))
      (map #(vector :txt %) (into [x] xs))))

(defn- modify-message
  "TODO: Write some great docstring explaining what all of this does."
  [exc]
  (let [exc-type (class exc)
        {:keys [cause data via trace]} (Throwable->map exc)
        nested? (> (count via) 1)
        {:keys [type message]} (last via)
        phase (:clojure.error/phase (:data (first via)))
        exc-info? (= clojure.lang.ExceptionInfo exc-type)
        compiler-exc? (= clojure.lang.Compiler$CompilerException exc-type)]
        (cond (and nested? compiler-exc? (processor/macro-spec? cause via))
                   (str (processor/spec-macro-message cause data)
                        "\n"
                        (processor/location-macro-spec via))
              (and nested? compiler-exc? (processor/invalid-signature? cause via))
                   (str (processor/invalid-sig-message cause
                                                       (:clojure.error/symbol (:data (first via))))
                        "\n"
                        (processor/location-macro-spec via))
              (or (and exc-info? (not nested?))
                  (and compiler-exc? (= clojure.lang.ExceptionInfo (resolve type))))
                  (str-ex->vec (processor/spec-message data)
                       "\n"
                       (processor/location-function-spec data))
              (and exc-info? (= clojure.lang.ExceptionInfo (resolve type)))
                  (str (processor/spec-message data)
                       "\n"
                       (processor/location-print-phase-spec data))
              ;; Non-spec message in the print-eval phase:
              (= phase :print-eval-result)
                  (str (p-exc/process-errors type message)
                       "\n"
                       (processor/location-print-phase via trace))
              :else
                  (str-ex->vec (p-exc/process-errors type message)
                       "\n"
                       (processor/location-non-spec via trace)))))

(defn- tagged->str 
  "Takes a vector of tag-content pairs and returns contents 
   as a single string, ignoring tags."
  [[[t v] & xs :as all]]
  (if all (str v (tagged->str xs)) ""))

;; I don't seem to be able to bind this var in middleware.
;; Running (setup-exc) in repl does the trick.
(defn setup-exc []
  (set! nrepl.middleware.caught/*caught-fn* #(do
    (let [modified (modify-message %)
          ;; problem: modify-message is turning modified into a string, always
          tagged (if (string? modified) [[:txt modified]] modified)
          untagged (tagged->str tagged) ; splitting it up like this lets us do things gradually
          trace (processor/print-stacktrace %) ; for logging
          _ (reset! track {:message (record-message %) :modified untagged :trace trace})]
          (println untagged)
          ; (println (type modified))
    (if (not= trace "") (println trace) ())))))

(defn reset-track [] (reset! track {}))