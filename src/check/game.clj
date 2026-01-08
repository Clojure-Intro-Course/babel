(ns check.game
  (:require [check.number-guess :as number-guess]
            [check.tic-tac-toe :as tic-tac-toe]
            [clojure.edn]))


(def number-guess-state (atom {})) ; student does not get to change this
(def tic-tac-toe-state (atom {}))


(defn number-guess
  "Runs the number-guess game. Takes no arguments and returns nil."
  []
  (reset! number-guess-state number-guess/initial-state)

  (loop []  (let [input (. *in* read)]
              (if (= input -1) (println "Quitting...")
                  (let [command (str (char input) (read-line))]
                    (swap! number-guess-state number-guess/update-game command)
                    (number-guess/draw-state @number-guess-state) ;; not sure where in the order to put this   
                    (if (:stop @number-guess-state) (println "Stopping game") (recur)))))))

(defn tic-tac-toe
  "Runs the number-guess game. Takes no arguments and returns nil."
  []
  (reset! tic-tac-toe-state number-guess/initial-state)

  (loop []  (let [input (. *in* read)]
              (if (= input -1) (println "Quitting...")
                  (let [command (str (char input) (read-line))]
                    (swap! tic-tac-toe-state tic-tac-toe/update-game command)
                    (swap! tic-tac-toe-state tic-tac-toe/enemy-turn)
                    (tic-tac-toe/draw-state @tic-tac-toe-state) ;; not sure where in the order to put this   
                    (if (:stop @tic-tac-toe-state) (println "Stopping game") (recur)))))))

;; read keypresses to quit the game?

;; require `check.game
;; (check.game/number-guess) to start the game