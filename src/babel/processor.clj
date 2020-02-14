(ns babel.processor
 (:require [clojure.string :as s]
           [errors.messageobj :as m-obj]
           [errors.prettify-exception :as p-exc]
           [errors.utils :as u]
           [errors.dictionaries :as d]
           [clojure.core.specs.alpha]))

;;an atom that record original error response
(def recorder (atom {:msg [] :detail []}))

(defn reset-recorder
  "This function reset the recorder atom"
  []
  (reset! recorder {:msg [] :detail []}))

(defn update-recorder-msg
  "takes an unfixed error message, and put it into the recorder"
  [inp-message]
  (swap! recorder update-in [:msg] conj inp-message))
  ;(swap! recorder assoc :msg inp-message))

(defn update-recorder-detail
  "takes error message details, and put them into the recorder"
  [inp-message]
  (swap! recorder update-in [:detail] conj inp-message))

(defn process-message
  "takes a Java Throwable object, and returns the adjusted message as a string."
  [err]
  (let [errmap (Throwable->map err)
        throwvia (:via errmap)
        viacount (count throwvia)
        errclass (str (:type (first throwvia)))
        errdata (:data errmap)]
    (if (and (= "clojure.lang.ExceptionInfo" errclass) (= viacount 1))
        (p-exc/process-spec-errors (str (.getMessage err)) errdata true)
        (if (= "clojure.lang.Compiler$CompilerException" errclass)
        ;; TO-DO: refactor this function and get rid of its uses on ExceptionInfo
          "";(p-exc/process-macro-errors err errclass (ex-data err))
          (if (and (= "clojure.lang.ExceptionInfo" errclass) (> viacount 1))
            (str
              (->> throwvia
                   reverse
                   first
                   :message
                   (str (:type (first (reverse throwvia))) " ")
                   p-exc/process-errors
                   :msg-info-obj
                   m-obj/get-all-text)
              (p-exc/process-stacktrace err))
            (str
              (->> err
                   .getMessage
                   (str errclass " ")
                   p-exc/process-errors
                   :msg-info-obj
                   m-obj/get-all-text)
              (p-exc/process-stacktrace err)))))))


(defn macro-spec?
  "Takes an exception object. Returns a true value if it's a spec error for a macro,
   a false value otherwise."
  [exc]
  (let [exc-map (Throwable->map exc)
        {:keys [via cause]} exc-map]
      (and (> (count via) 1)
           (= :macro-syntax-check (:clojure.error/phase (:data (first via))))
           (re-matches #"Call to (.*) did not conform to spec." cause))))

(def spec-ref {:number "a number", :collection "a sequence", :string "a string", :coll "a sequence",
                :map-arg "a two-element-vector", :function "a function", :ratio "a ratio", :future "a future", :key "a key", :map-or-vector "a map-or-vector",
                :regex "a regular expression", :num-non-zero "a number that's not zero", :num "a number", :lazy "a lazy sequence"
                :wrong-path "of correct type and length", :sequence "a sequence of vectors with only 2 elements or a map with key-value pairs",
                :number-greater-than-zero "a number that's greater than zero",
                :collection-map "a sequence" :only-collection "a collection"})

(def length-ref {:b-length-one "one argument", :b-length-two "two arguments", :b-length-three "three arguments", :b-length-zero-or-greater "zero or more arguments",
                 :b-length-greater-zero "one or more arguments", :b-length-greater-one "two or more arguments", :b-length-greater-two "three or more arguments",
                 :b-length-zero-to-one "zero or one arguments", :b-length-one-to-two "one or two arguments", :b-length-two-to-three "two or three arguments",
                 :b-length-two-to-four "two or up to four arguments", :b-length-one-to-three "one or up to three arguments", :b-length-zero-to-three "zero or up to three arguments"})

(defn stringify
  "Takes a vector of keywords of failed predicates. If there is
  only one, returns the result of looking it up in spec-ref.
  Otherwise returns the first result of looking up the rest of
  the keywords in spec-ref, as a string.
  Returns an empty string if no matches are found"
  [vector-of-keywords]
  (if (= (count vector-of-keywords) 1)
      (or (spec-ref (first vector-of-keywords)) "unknown condition")
      (or (->> (rest vector-of-keywords)
               (map #(spec-ref %))
               (filter #(not (nil? %)))
               first) "unknown condition")))

(defn has-alpha-nil?
  [{:keys [path]}]
  (.contains path :clojure.spec.alpha/nil))

(defn filter-extra-spec-errors
   "problem-maps looks like [{:path [:a :b ...] ~~} {:path [] ~~} ...]
   Filters through problem-maps removing any map that contains :clojure.spec.apha/nil in :path or :reason"
   [problem-maps]
   (if (> (count problem-maps) 1)
       (->> problem-maps
            (filter #(not (has-alpha-nil? %)))
            (filter #(not (contains? % :reason))))
       problem-maps))

(defn babel-spec-message
  "Takes ex-info data of our babel spec error, returns a modified message as a string"
  [ex-data]
  (let [{problem-list :clojure.spec.alpha/problems fn-full-name :clojure.spec.alpha/fn args-val :clojure.spec.alpha/args} ex-data
        {:keys [path pred val via in]} (-> problem-list
                                           filter-extra-spec-errors
                                           first)
        fn-name (d/get-function-name (str fn-full-name))
        function-args-val (apply str (interpose " " (map d/range-collapse (map d/anonymous? (map #(second (d/type-and-val %)) args-val)))))
        arg-number (first in)
        [print-type print-val] (map d/range-collapse (d/type-and-val val))]
    (if (re-matches #"corefns\.corefns/b-length(.*)" (str pred))
      (str "Wrong number of arguments, expected in ("
           fn-name
           " "
           function-args-val
           "): the function "
           fn-name
           " expects "
           (length-ref (keyword (d/get-function-name (str (first via)))))
           " but was given "
           (if (or (nil? val) (= (count val) 0)) "no" (d/number-word (count val)))
           " arguments")
      (str "The "
           (d/arg-str arg-number)
           " of ("
           fn-name
           " "
           function-args-val
           ") was expected to be "
           (stringify path)
           " but is "
           print-type print-val
           " instead.\n"))))

(defn unknown-spec
  "determines if the spec function is ours or someone's else"
  [unknown-ex-data]
  (let [{problem-list :clojure.spec.alpha/problems fn-full-name :clojure.spec.alpha/fn args-val :clojure.spec.alpha/args} unknown-ex-data
        {:keys [path pred val via in]} (-> problem-list
                                           filter-extra-spec-errors
                                           first)
         fn-name (d/get-function-name (str fn-full-name))
         function-args-val (apply str (interpose " " (map d/anonymous? (map #(second (d/type-and-val %)) args-val))))
         arg-number (first in)
         [print-type print-val] (d/type-and-val val)]
     (cond
       (= (:reason (first problem-list)) "Extra input")
          (str
            "Extra input: 'In the "
            fn-name
            "call ("
            fn-name function-args-val
            ") there were extra arguments'")
       (= (:reason (first problem-list)) "Insufficient input")
          (str
            "Insufficient input: 'In the "
            fn-name
            "call ("
            fn-name function-args-val
            ") there were insufficient arguments'")
       :else
          (str
            "Fails a predicate: 'The "
            arg-number " argument of ("
            fn-name function-args-val
            ") fails a requirement: must be a "
            pred))))

(defn spec-message
 "uses babel-spec-message"
 [exception]
 (let [{problem-list :clojure.spec.alpha/problems} exception
       {:keys [pred]} (-> problem-list
                          filter-extra-spec-errors
                          first)]
 (if (or (re-matches #"clojure.core(.*)" (str pred)) (re-matches #"corefns\.corefns(.*)" (str pred)))
     (babel-spec-message exception)
     (unknown-spec exception))))


;; Predicates are mapped to a pair of a position and a beginner-friendly
;; name. Negativr positions are later discarded
(def macro-predicates {#'clojure.core/simple-symbol? [0 " a name"],
  #'clojure.core/vector? [1 " a vector"], #'clojure.core/map? [2 " a hashmap"],
  #'clojure.core/qualified-keyword? [-1 " a keyword"],
  #'clojure.core/sequential? [1 " a vector"]}) ; while other sequential constructs are possible, for beginners "a vector" is sufficient

(defn- predicate-name
  "Takes a failed predicate from a macro spec, returns a vector
   of its name and position"
  [p]
  (cond (symbol? p) (or (macro-predicates (resolve p)) [10 " unknown type"]) ; for debugging purposes
        (set? p) [-1 " one of specific keywords"]
        (= (str p) "(clojure.core/fn [%] (clojure.core/not= (quote &) %))") [-1 " not &"]
        (and (seq? p) (re-find #"clojure.core/sequential\?" (apply str (flatten p))))
             (macro-predicates #'clojure.core/sequential?)
        :else  [10 (str " unknown type " p)]))

(defn- print-failed-predicates
  "Takes a vector of hashmaps of failed predicates and returns a string
   that describes them for beginners"
  [probs]
  (->> probs
       (filter #(nil? (:reason %))) ; eliminate "Extra input" and "Insufficient input"
       (map :pred) ; get the failed predicates
       (distinct) ; eliminate duplicates
       (map #(predicate-name %)) ; get position/name pairs
       (sort #(< (first %1) (first %2))) ; sort by the position
       (filter #(>= (first %) 0)); remove negative positions
       (map second) ; take names only
       (distinct) ; eliminate duplicates
       (s/join " or"))) ; join into a string with " or" as a separator

(defn- process-group
  "Takes a vector of a value and hashmaps of predicates it failed and returns
   a string describing the problems"
  [[val probs]]
  (let [printed-group (print-failed-predicates probs)]
       (if (not= printed-group "")
           (str "In place of " (d/print-macro-arg val) " the following are allowed:" (print-failed-predicates probs) "\n")
           "")))

(defn- process-paths-macro
  "Takes the 'problems' part of a spec for a macro and returns a description
   of the problems as a string"
  [problems]
  (let [grouped (group-by :val (map #(select-keys % [:pred :val :reason]) problems))]
       (apply str (map process-group grouped))))

(defn- invalid-macro-params?
  "Takes the 'problems' part of a spect for a macro and returns true
   if all problems refer to the parameters and false otherwise"
   [problems]
   (let [via-lasts (distinct (map str (map last (map :via problems))))]
        (and (not (empty? via-lasts)) (every? #(or (re-find #"param-list" %) (re-find #"param+body" %)) via-lasts))))

(defn- let-macros
  "Takes parts of the spec message for let and related macros and returns an error message as a string"
  [fn-name value problems]
  (str "Syntax problems with ("
        fn-name
        " "
        (str (d/print-macro-arg (first value) "[" "]") (cond (= (count (rest value)) 0) ""
                                                    (= (count (rest value)) 1) (str " " (d/print-macro-arg (first (rest value)) :sym))
                                                    :else (str " " (d/print-macro-arg (rest value)))))
        "):\n"
        (process-paths-macro problems)))

(defn- defn-macros
  "Takes parts of the spec message for defn and defn- and returns an error message as a string"
  [fn-name value problems]
  (let [n (count problems)
        val-str (d/print-macro-arg value)
        probs-labeled (u/label-vect-maps problems) ; each spec fail is labeled with its position in 'problems'
        probs-grouped (group-by :in probs-labeled)
        error-name (str "Syntax problems with (" fn-name (u/with-space-if-needed val-str) "):\n")]
        (cond (u/has-match? probs-grouped {:path [:fn-name]})
                   (str error-name  "Missing a function name, given "
                                    (let [val (:val (first problems))] (if (nil? val) "nil" (d/print-macro-arg val)))
                                    " instead.")
              (u/has-match? probs-grouped {:reason "Insufficient input", :pred :clojure.core.specs.alpha/binding-form})
                   (str error-name fn-name " is missing a name after &.")
              (u/has-every-match? probs-grouped
                   [{:reason "Extra input", :path [:fn-tail :arity-1 :params]}
                   {:pred 'clojure.core/vector?, :path [:fn-tail :arity-n :bodies :params]}])
                   (str error-name (u/parameters-not-names
                                     (first (u/get-match probs-grouped
                                                 {:reason "Extra input", :path [:fn-tail :arity-1 :params]}))
                                      value))
              :else "Placeholder message for defn")))

(defn- fn-macros
  "Takes parts of the spec message for fn and returns an error message as a string"
  [fn-name value problems]
  (let [n (count problems)
       val-str (d/print-macro-arg value)
       probs-labeled (u/label-vect-maps problems) ; each spec fail is labeled with its position in 'problems'
       probs-grouped (group-by :in probs-labeled)
       error-name (str "Syntax problems with (" fn-name (u/with-space-if-needed val-str) "):\n")]
       (cond (and (= n 1) ((u/key-vals-match {:reason "Insufficient input", :path [:fn-tail]}) (first problems)))
                  (str error-name "fn is missing a vector of parameters.")
             (u/has-match? probs-grouped {:reason "Insufficient input", :pred :clojure.core.specs.alpha/binding-form})
                  (str error-name "fn is missing a name after &.")
             (u/has-every-match? probs-grouped
                  [{:pred 'clojure.core/vector?}
                   {:pred '(clojure.core/fn [%] (clojure.core/or (clojure.core/nil? %) (clojure.core/sequential? %)))}])
                   (str error-name (u/missing-vector-message probs-grouped value))
             (and (> n 1) (u/all-match? probs-grouped {:reason "Extra input"}))
                  (str error-name (u/process-nested-error probs-grouped))
             (u/has-every-match? probs-grouped
                  [{:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}
                   {:pred 'clojure.core/vector?, :path [:fn-tail :arity-n :params]}])
                  (str error-name (u/missing-vector-message-seq
                                    (first (u/get-match probs-grouped
                                                 {:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}))
                                     value))
             (u/has-every-match? probs-grouped
                  [{:reason "Extra input", :path [:fn-tail :arity-1 :params]}
                   {:pred 'clojure.core/vector?, :path [:fn-tail :arity-n :params]}])
                  (str error-name (u/parameters-not-names
                                    (first (u/get-match probs-grouped
                                                 {:reason "Extra input", :path [:fn-tail :arity-1 :params]}))
                                    value))
              ;; multi-arity case, first clause
             (u/has-every-match? probs-grouped
                 [{:reason "Extra input", :path [:fn-tail :arity-n :params]}
                  {:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}])
                 (str error-name (u/parameters-not-names
                                   (first (u/get-match probs-grouped
                                                {:reason "Extra input", :path [:fn-tail :arity-n :params]}))
                                   value))
             (u/has-every-match? probs-grouped
                 [{:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}
                  {:reason "Insufficient input", :path [:fn-tail :arity-n :params]}])
                 (str error-name (u/parameters-not-names
                                   (first (u/get-match probs-grouped
                                                {:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}))
                                   value))
              (u/has-every-match? probs-grouped
                   [{:path [:fn-tail :arity-1 :params :var-params :var-form :local-symbol]}
                    {:path [:fn-tail :arity-1 :params :var-params :var-form :seq-destructure]}
                    {:path [:fn-tail :arity-1 :params :var-params :var-form :map-destructure]}
                    {:pred 'clojure.core/vector?, :path [:fn-tail :arity-n :params]}])
                  (str error-name (u/parameters-not-names
                                     (first (u/get-match probs-grouped
                                                  {:path [:fn-tail :arity-1 :params :var-params :var-form :local-symbol]}))
                                     value))
              (and (= n 1) (u/has-match-by-prefix? probs-grouped {:path [:fn-tail :arity-n]}))
                   (str error-name (u/clause-single-spec (first problems) ; n=1, so there is only one prob
                                                         value))

              :else (str error-name "Placeholder for a message for fn"))))


(defn spec-macro-message
  "Takes an exception of a macro spec failure and returns the description of
   the problem as a string"
  [ex]
  (let [exc-map (Throwable->map ex)
        {:keys [cause data via trace]} exc-map
        fn-name-match (nth (re-matches #"Call to (.*) did not conform to spec." cause) 1)
        fn-name (if (= (str fn-name-match) "clojure.core/fn") "fn" (d/get-function-name fn-name-match))
        {problems :clojure.spec.alpha/problems value :clojure.spec.alpha/value args :clojure.spec.alpha/args} data
        val-str (d/print-macro-arg value) ; need to be consistent between val and value
        n (count problems)]
        ;; If there is no value, I need to get the exc type and the messages from the second of via
        ;; and pass it to processing.
        (cond (#{"fn"} fn-name) (fn-macros fn-name value problems)
              (#{"defn" "defn-"} fn-name) (defn-macros fn-name value problems)
              (and (= n 1) (= "Insufficient input" (:reason (first problems)))) (str fn-name " requires more parts than given here: (" fn-name val-str ")\n")
              ;; should we report the extra parts?
              (and (= n 1) (= "Extra input" (:reason (first problems)))) (str fn-name " has too many parts here: (" fn-name " " val-str ")" (d/extra-macro-args-info (first problems)) "\n")
              ;; case of :data containing only :arg Example: (defn f ([+] 5 6) 9) - WE MIGHT NOT NEED THIS CASE
              ;(or (= val-str " ") (= val-str "")) (str "The parameters are invalid in (" fn-name (s/join " " (d/macro-args->str args))  ")\n")
              (and (= n 1) (= (resolve (:pred (first problems))) #'clojure.core.specs.alpha/even-number-of-forms?))
                   (str fn-name " requires pairs of a name and an expression, but in (" fn-name val-str ") one element doesn't have a match.\n")
              (and (= n 1) (= (resolve (:pred (first problems))) #'clojure.core/vector?))
                   (str fn-name " requires a vector of name/expression pairs, but is given " (d/print-macro-arg (:val (first problems)) :sym) " instead.\n")
              (invalid-macro-params? problems) (str "The parameters are invalid in (" fn-name " " val-str ")\n")
              (and (#{"let" "if-let"} fn-name) (seqable? value))
                   ;(let-macros (if (d/let-is-fn? trace) "fn" fn-name) value problems)
                   (let-macros fn-name value problems)
              :else (str "Syntax problems with (" fn-name  " " val-str "):\n" (process-paths-macro problems)))))

(println "babel.processor loaded")
