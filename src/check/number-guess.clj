(ns check.number-guess
 (:require [number-guess.core :as core]))

(def initial {:correct (rand-int 50), :previous []})

(defn game
  []
  (let [command (read-line)]
  (cond (contains? (set (map #(str "guess " %) (core/state :previous))) command) (println "This has already been guessed! Try something else.")
        (= command (str "guess " (core/state :correct))) (println "Correct!")
        (< command (str "guess " (core/state :correct))) (do (println "Higher.") (swap! core/state (conj core/state )) (game))
        (> command (str "guess " (core/state :correct))) (println "Lower."))))

(defn guess
  "Takes an integer and returns a string saying whether the correct number is above or below this value. Ends the game if the correct number is guessed."
  [num]
  )

(defn input 
  "Prints a prompt and asks for player input, which affects the state in some way."
  )

