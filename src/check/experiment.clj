(ns check.experiment)

(def initial-state {})
(defn update-game
  [state command]
  (println (type command) "is command's type")
  (println command "is command") 

  ;; (println (type (parse-long command)) "is type after calling parse-long") 
  (if (number? command)   
    (if (zero? command) (update-in state [:counter] #(+ (or % 0) 1))
      (update-game state (dec command))) 
    (update-game state (dec (parse-long command))))

  )
(defn win? [state player]
  false)
(defn draw-state [state]
  state)

(def game-map {:commands "Type in any number"
               :initial-state initial-state
               :update-game update-game
               :win? win?
               :draw-state draw-state})