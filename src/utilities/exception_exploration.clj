(ns utilities.exception_exploration
   (:require [clojure.string :as str]))

;; Utility functions for exploring logged exceptions.

;; Takes a HashMap of a logged exception and converts strings to HashMaps as needed.

(defn read-log [map]
  ;; create a new map with with strings converted 
  (assoc {}
         :code (:code map)
         :exception (read-string (:exception map))
         :ex-triage (read-string (:ex-triage map))))