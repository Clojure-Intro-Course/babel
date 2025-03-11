(ns utilities.exception_exploration
   (:require [clojure.string :as str]
             [clojure.java.io :as io]))

;; Utility functions for exploring logged exceptions.

;; Takes a HashMap of a logged exception and converts strings to HashMaps as needed.

(defn read-log
  "create a new map with with strings converted"
  [map]
  (assoc {}
         :code (:code map)
         :exception (when-let [ex (:exception map)] (read-string ex)) ;; when-let because some :exception and :ex-triage are nil
         :ex-triage (when-let [triage (:ex-triage map)] (read-string triage))))

(defn parse-logs
  "parses the babel log output and saves it for the other functions. The file is the name of the log you want to parse in the code-ex-triage file"
  [file]
  (let [file-path (str "./log/code-ex-triage/" file)]
    (println "Attempting to read file at path:" file-path)
    (if (.exists (io/file file-path))
      (let [log-content (slurp file-path)]
        (println (str "read `"file-path "` succesfully"))
        (let [logs (read-string log-content)]
          (println "mapping `read-log` onto `logs`")
          (into [] (map read-log logs))))
      (println "File not found:" file-path))))

(defn get-phase
  "get the phase of the exception in a log"
  [log]
  (get-in log [:ex-triage :phase]))

(defn filter-by-phase 
  "Takes a vector of (log) maps and returns the logs that have the phase that is input"
  [logs phase]
  (filter #(= phase
              (get-phase %)) logs))

(defn get-level-nesting
  "get the level of nesting in an exception log"
  [log]
  (count (get-in log [:exception :via])))

(defn get-nested-types
  "get the types of errors in the :at :type of an error"
  [log]
  (map :type (get-in log [:exception :via])))

;; logs(vector of maps) -> logs(vector of maps)
(defn filter-by-nesting
  ""
  [logs level]
  (filter #(= level
              (get-level-nesting %)) logs))

(defn get-nested-types
  "get the types of errors in the :at :type of an error"
  [log]
  (vec (map :type (get-in log [:exception :via]))))


;; [logs, map of :kw and vals] -> logs
;; (defn filter-search
;;   ""
;;   [])

;; --- Function Example Uses ---
;;
;; - Setup -
;; needs the original "ex.txt" file for tests
;; (require '[utilities.exception_exploration :as exploration])
;; (def parsed-logs (exploration/parse-logs "ex.txt"))
;; (def log1 (parsed-logs 22))
;;  --
;; 
;; - Using Functions -
;; (exploration/parse-logs "ex.txt")
;; (exploration/filter-by-phase parsed-logs :macro-syntax-check)
;; (exploration/get-nested-types log1) => 

;; --
;; 
;; - Tests -
;; (count (exploration/filter-by-phase parsed-logs :macro-syntax-check)) => 189
;; (exploration/get-level-nesting (parsed-logs 22)) => 3
;; (count (exploration/filter-by-nesting parsed-logs 3)) => 8
;; (count (exploration/get-nested-types log1)) => 3
;; (exploration/get-nested-types log1) => (clojure.lang.ExceptionInfo clojure.lang.LispReader$ReaderException java.lang.RuntimeException)