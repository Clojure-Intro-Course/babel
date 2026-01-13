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
      (do (println "This command is invalid. Please use one of the following: tl, tm, tr, ml, mm, mr, bl, bm, br") state))
    )
  )
(defn enemy-turn 
  "Takes the current state, and returns the updated state after simulating the enemy's turn."
  [state] 
  (loop [choice (rand-nth [:tl :tm :tr :ml :mm :mr :bl :bm :br])]
    (if (nil? (state choice)) 
      (assoc state choice 1) ; Enemy is 1
      (recur (rand-nth [:tl :tm :tr :ml :mm :mr :bl :bm :br])))))

(defn draw-state
  "Takes the current state and prints information from it to the console."
  [state]
  (println (str (or (state :tl) "_") " " (or (state :tm) "_") " " (or (state :tr) "_") "\n"
                (or (state :ml) "_") " " (or (state :mm) "_") " " (or (state :mr) "_") "\n"
                (or (state :bl) "_") " " (or (state :bm) "_") " " (or (state :br) "_"))))



;; (defn- print-state
;;   []
;;   (println (@core/state :tl) (@core/state :tm) (@core/state :tr) "\n"
;;            (@core/state :ml) (@core/state :mm) (@core/state :mr) "\n"
;;            (@core/state :bl) (@core/state :bm) (@core/state :br)))

;; (defn game 
;;   []
;;   (print-state)
;;   (let [command (read-line)]
;;     (loop [choice (rand-nth [:tl :tm :tr :ml :mm :mr :bl :bm :br])]
;;       (if (nil? (@core/state choice)) (swap! core/state update-in [choice] (constantly 1)) (recur (rand-nth [:tl :tm :tr :ml :mm :mr :bl :bm :br]))))
;;     (cond (= command "tl") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:tl] (constantly 0)) (game)) 
;;                                #_(else) (do (println "This square is already full! Try again.") (game)))
;;           (= command "tm") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:tm] (constantly 0)) (game))
;;                                #_(else) (do (println "This square is already full! Try again.") (game)))
;;           (= command "tr") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:tr] (constantly 0)) (game))
;;                                #_(else) (do (println "This square is already full! Try again.") (game)))
;;           (= command "ml") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:ml] (constantly 0)) (game))
;;                                #_(else) (do (println "This square is already full! Try again.") (game)))
;;           (= command "mm") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:mm] (constantly 0)) (game))
;;                                #_(else) (do (println "This square is already full! Try again.") (game)))
;;           (= command "mr") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:mr] (constantly 0)) (game))
;;                                #_(else) (do (println "This square is already full! Try again.") (game)))
;;           (= command "bl") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:bl] (constantly 0)) (game))
;;                                #_(else) (do (println "This square is already full! Try again.") (game)))
;;           (= command "bm") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:bm] (constantly 0)) (game))
;;                                #_(else) (do (println "This square is already full! Try again.") (game)))
;;           (= command "br") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:br] (constantly 0)) (game))
;;                                #_(else) (do (println "This square is already full! Try again.") (game)))
;;           :else (do (println "This command is invalid. Please use one of the following: tl, tm, tr, ml, mm, mr, bl, bm, br") (game)))
;;     ))



;; (defn start-game 
;;   []
;;   (reset! core/state {:tl nil, :tm nil, :tr nil,
;;                       :ml nil, :mm nil, :mr nil,
;;                       :bl nil, :bm nil, :br nil})
;;   (game))
