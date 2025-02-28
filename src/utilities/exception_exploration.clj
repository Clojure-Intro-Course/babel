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

;;(require '[utilities.exception_exploration :as exploration])
;;(exploration/parse-logs "ex.txt")
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

;; (def parsed-logs (exploration/parse-logs "ex.txt"))
;; (exploration/filter-by-phase parsed-logs :macro-syntax-check)
;; (count (exploration/filter-by-phase parsed-logs :macro-syntax-check)) => 189

(defn filter-by-phase 
  "Takes a vector of (log) maps and returns the logs that have the phase that is input"
  [logs phase]
  (filter #(= phase
              (get-in % [:ex-triage :phase])) logs))

;; log(map) -> number
(defn get-level-nesting
  "get the level of nesting in an exception log"
  [log]
  (count (get-in log [:exception :via])))
;; (exploration/get-level-nesting (parsed-logs 22)) => 3

;; logs(vector of maps) -> logs(vector of maps)
(defn filter-by-nesting
  ""
  [logs level]
  (filter #(= level
              (get-level-nesting %)) logs))

;; Generallized filter fn
;; logs (vector of maps), filter option, filter option/value -> logs (vector of maps)
;; (filter-by
;;  ""
;;  [])