(ns check.game
  (:require [clojure.edn]))


(def number-guess-state (atom {})) ; student does not get to change this
(def tic-tac-toe-state (atom {}))


(defn game 
  "Runs the game. Takes a hashmap containing game functions and returns nil."
  [game-map]
  (loop [game-state (atom (:initial-state game-map))] 
   (println (:commands game-map))
   (let [input (. *in* read)]
    (if (= input -1) (println "Quitting...") ;; ctrl+D is the end of input, which translates to -1 in this case. 
        (let [command (str (char input) (read-line))]
          (swap! game-state (:update-game game-map) command) ;; player turn, always exists 
          (if ((:win? game-map) @game-state 0) (do (println "You Win!") (swap! game-state update-in [:stop] any?))) ;; check if player wins after their turn 
          (if (:enemy-turn game-map) (swap! game-state (:enemy-turn game-map))) ;; run the enemy turn if it exists
          (if ((:win? game-map) @game-state 1) (do (println "Enemy Wins!") (swap! game-state update-in [:stop] any?))) ;; check if enemy wins after their turn
          (println ((:draw-state game-map) @game-state)) ;; draw the state
          (if (:stop @game-state) (println "Stopping game") (recur game-state)) ;; stop the game if :stop is true, otherwise continue
          
          )) ) ))

#_(defn number-guess
  "Runs the number-guess game. Takes no arguments and returns nil."
  []
  (reset! number-guess-state number-guess/initial-state)

  (loop []  (let [input (. *in* read)]
              (if (= input -1) (println "Quitting...")
                  (let [command (str (char input) (read-line))]
                    (swap! number-guess-state number-guess/update-game command)
                    (number-guess/draw-state @number-guess-state) ;; not sure where in the order to put this   
                    (if (:stop @number-guess-state) (println "Stopping game") (recur)))))))

#_(defn tic-tac-toe
  "Runs the number-guess game. Takes no arguments and returns nil."
  []
  (reset! tic-tac-toe-state number-guess/initial-state)

  (loop []  (let [input (. *in* read)]
              (if (= input -1) (println "Quitting...")
                  (let [command (str (char input) (read-line))]
                    (swap! tic-tac-toe-state tic-tac-toe/update-game command)
                    (if (tic-tac-toe/win? @tic-tac-toe-state 0) 
                      (do (println "You Win!") (swap! tic-tac-toe-state update-in [:stop] any?)) 
                      (swap! tic-tac-toe-state tic-tac-toe/enemy-turn))
                    (if (tic-tac-toe/win? @tic-tac-toe-state 1) 
                      (do (println "Enemy Wins!") (swap! tic-tac-toe-state update-in [:stop] any?)))
                    (tic-tac-toe/draw-state @tic-tac-toe-state) ;; not sure where in the order to put this   
                    (if (:stop @tic-tac-toe-state) (println "Stopping game") (recur)))))))

;; read keypresses to quit the game?

;; require `check.game
;; (check.game/number-guess) to start the game