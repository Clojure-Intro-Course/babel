(ns check.game
  (:require [check.number-guess :as number-guess]
            [clojure.edn]))


(def number-guess-state (atom {})) ; student does not get to change this



(defn number-guess
  []
  (reset! number-guess-state number-guess/initial-state)

  (loop []  (let [input (. *in* read)]
              (if (= input -1) (println "Quitting...")
                  (let [command (. *in* readLine)]
                    (swap! number-guess-state number-guess/update-game command)
                    (number-guess/draw-state @number-guess-state) ;; not sure where in the order to put this   
                    (if (:stop @number-guess-state) (println "Stopping game") (recur)))))))

;; read keypresses to quit the game?

;; require `check.game
;; (check.game/number-guess) to start the game