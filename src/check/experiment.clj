(ns check.experiment)


;; THIS INTENTIONALLY RUNS FOREVER. The purpose of the experiment is to test the timeout feature, and possibly generate an exception.
(def initial-state {})
(defn update-game
  [state command]
  (loop [state state]
    (recur (str command state))) 

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