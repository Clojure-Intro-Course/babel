(ns babel.middleware
  (:require [babel.processor :as processor]
            [errors.prettify-exception :as p-exc]
            [nrepl.middleware.caught]
            [clojure.repl]
            [clojure.main :as cm])
  (:gen-class))

(def track (atom {})) ; For debugging and testing purposes.

(defn- record-message
  [e]
  (cm/ex-str (cm/ex-triage (Throwable->map e))))

(defn remove-nil
  "Takes a hashmap and recursively removes pairs that have the value nil or \"nil\". Returns a copy of the hashmap that has these pairs removed"
   [hm]
  (->> hm
       ;; removes all pairs with a second value of nil or "nil"
      (filter #(not (or (= nil (second %)) (= "nil" (second %)))))
       ;; recursive call to remove things at all further layers of nesting
      (map #(if (map? (second %)) [(first %) (remove-nil (second %))] %))
       ;; put all this into a hashmap
      (into {})))


(defn modify-message ; made public for usage in check.fns/check-equal
  "TODO: Write some great docstring explaining what all of this does."
  [exc]
  (let [exc-type (class exc)
        {:keys [cause data via trace]} (Throwable->map exc)
        nested? (> (count via) 1)
        {:keys [type message]} (last via)
        phase (:clojure.error/phase (:data (first via)))
        exc-info? (= clojure.lang.ExceptionInfo exc-type)
        compiler-exc? (= clojure.lang.Compiler$CompilerException exc-type)] 
    (cond
      ;; Macro spec failures
      (and nested? compiler-exc? (processor/macro-spec? cause via))
      (str (processor/spec-macro-message cause data)
           "\n"
           (processor/location-macro-spec via))
      ;; Invalid signature errors 
      ;; (macroexpansion problems not caught by any specs)
      (and nested? compiler-exc? (processor/invalid-signature? cause via))
      (str (processor/invalid-sig-message cause
                                          (:clojure.error/symbol (:data (first via))))
           "\n"
           (processor/location-macro-spec via))
      ;; Spec errors; can be produced by Babel or a third-party.
      ;; Both cases are handled separately in (spec-message).
      (or (and exc-info? (not nested?))
          (and compiler-exc? (= clojure.lang.ExceptionInfo (resolve type))))
      (str (processor/spec-message data)
           "\n"
           (processor/location-function-spec data)) 
      ;; Alternate handling of spec errors, 
      ;; if the error happens in the :print-eval phase
      (and exc-info? (= clojure.lang.ExceptionInfo (resolve type)))
      (str (processor/spec-message data)
           "\n"
           (processor/location-print-phase-spec data))
      ;; All other errors in the :print-eval phase
      ;; i.e., specific errors that we can't generalize via specs.
      (= phase :print-eval-result)
      (str (p-exc/process-errors type message)
           "\n"
           (processor/location-print-phase via trace))
      ;; All other errors (not in the :print-eval phase).
      ;; These are processed by lookup in errors/error_dictionary.clj
      :else
      (str (p-exc/process-errors type message)
           "\n"
           (processor/location-non-spec via trace)))))

(defn split-exception [ex]
  "split the exception into its keys"
  (let [{:keys [cause trace via phase]} (Throwable->map ex)
        via-details (map #(select-keys % [:at :message :type]) via)]
    {:cause cause :via via-details})) ; removed :trace (pr-str trace) and phase :phase (pr-str phase)

(defn split-triage [ex]
  "split the ex-triage into its keys"
  (let [triage-map (clojure.main/ex-triage (Throwable->map ex))
        {class :clojure.error/class line :clojure.error/line cause :clojure.error/cause symbol :clojure.error/symbol source :clojure.error/source spec :clojure.error/spec phase :clojure.error/phase} triage-map 
        {problems :clojure.spec.alpha/problems alpha-spec :clojure.spec.alpha/spec value :clojure.spec.alpha/value func :clojure.spec.alpha/fn args :clojure.spec.alpha/args} (or spec {}) 
        {path :path reason :reason pred :pred val :val problems-via :via in :in} (or (first problems) {})] 
    {:class class :line line :cause cause :symbol symbol :source source :spec {:problems {:path path :reason reason :pred pred :val (pr-str val) :via (pr-str problems-via) :in in} :spec (pr-str alpha-spec) :value (pr-str value) :fn (pr-str func) :args (pr-str args)} :phase phase}))



;; I don't seem to be able to bind this var in middleware.
;; Running (setup-exc) in repl does the trick.
(defn setup-exc
  []
  (set! nrepl.middleware.caught/*caught-fn* #(do
    (let [modified (modify-message %)
          trace (processor/print-stacktrace %) ; for logging
          split-exc (split-exception %)
          split-tri (split-triage %)
          _ (reset! track {
            :message (record-message %) 
            :modified modified 
            :trace trace
            ; :exception (pr-str (Throwable->map %))
            :exception (pr-str split-exc)
            ; :ex-triage (subs(pr-str (clojure.main/ex-triage (Throwable->map %))) 15)
            :ex-triage (pr-str split-tri)
            })]            
    (println modified)
    (if (not= trace "") (println trace) ())))))

(defn reset-track [] (reset! track {}))

