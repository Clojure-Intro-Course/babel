(ns utilities.exception_exploration
   (:require [clojure.string :as str]))

;; Utility functions for exploring logged exceptions.

;; Takes a HashMap of a logged exception and converts strings to HashMaps as needed.

(defn read-log [map]
  ;; create a new map with with strings converted 
  (assoc {}
         :code (:code map)
         :exception-details (read-string (:exception-details map))
         :triage-details (read-string (:triage-details map))))