(ns check.tic-tac-toe
  (:require [babel.utils-for-testing :as utils]))

(def initial-state {:tl nil, :tm nil, :tr nil,
                    :ml nil, :mm nil, :mr nil,
                    :bl nil, :bm nil, :br nil})

(defn win?
  "Takes the current state and returns 0 if player 0 has won, 1 if player 1 has won, and nil otherwise."
  [state player]
  (let [winning-combinations [[:tl :tm :tr]
                              [:ml :mm :mr]
                              [:bl :bm :br]
                              [:tl :ml :bl]
                              [:tm :mm :bm]
                              [:tr :mr :br]
                              [:tl :mm :br]
                              [:tr :mm :bl]]]
    (loop [combinations winning-combinations]
      (if (empty? combinations)
        nil
        (let [combo (first combinations)
              values (map state combo)]
          (if  (every? #(= player %) values)
            (first values)
            (recur (rest combinations))))))))

(defn update-game
  "Takes the current state and a command string, and returns the updated state after processing the command."
  [state command]
  (let [move (keyword command)]
    (if (contains? #{:tl :tm :tr :ml :mm :mr :bl :bm :br} move)
      (if (nil? (state move))
        (assoc state move 0) ; Player is 0
        (do (println "This square is already full! Try again.") state))
      (do (println "This command is invalid. Please use one of the following: tl, tm, tr, ml, mm, mr, bl, bm, br") state))))
(defn enemy-turn
  "Takes the current state, and returns the updated state after simulating the enemy's turn."
  [state]
  (loop [choice (rand-nth [:tl :tm :tr :ml :mm :mr :bl :bm :br])]
    (cond (nil? (state choice)) (assoc state choice 1) ; Enemy is 1
          (and (not (or (win? state 0) (win? state 1))) (every? #(not (nil? (state %))) [:tl :tm :tr :ml :mm :mr :bl :bm :br])) (do (println "It's a tie!") state) ;; Check for tie
          :else (recur (rand-nth [:tl :tm :tr :ml :mm :mr :bl :bm :br]))))) ;; TODO: Currently causing an infinite loop. Look into timeout threads?

(defn draw-state
  "Takes the current state and prints information from it to the console."
  [state]
  (println (str (or (state :tl) "_") " " (or (state :tm) "_") " " (or (state :tr) "_") "\n"
                (or (state :ml) "_") " " (or (state :mm) "_") " " (or (state :mr) "_") "\n"
                (or (state :bl) "_") " " (or (state :bm) "_") " " (or (state :br) "_"))))

(def game-map {:commands "Possible commands: tl, tm, tr, ml, mm, mr, bl, bm, br \n (tl for Top Left, etc.)\n press Control+D to quit"
               :initial-state initial-state
               :update-game update-game
               :win? win?
               :enemy-turn enemy-turn
               :draw-state draw-state})
