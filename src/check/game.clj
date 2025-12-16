(ns check.game
  (:require [check.number-guess :as number-guess]))


(def number-guess-state (atom {})) ; student does not get to change this



(defn number-guess []
  (reset! number-guess-state (number-guess/initial-state))

(loop  (let [command (read-line)]
    (swap! number-guess-state number-guess/update command)
    (number-guess/draw-state @number-guess-state) ;; not sure where in the order to put this
    (if (contains? @number-guess-state :end) (number-guess/draw-state @number-guess-state) (recur))))
)