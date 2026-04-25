(ns check.experiment)

(def initial-state {})
(defn update-game 
  [state command]
  (if zero? command state)
  (update-game state (- command 0)))
(defn win? [state]
  false)
(defn draw-state [state]
  "This is a test game that will run forever because yes")

(def game-map {:commands "Type in any number"
               :initial-state initial-state
               :update-game update-game
               :win? win?
               :draw-state draw-state})