(ns check.tic-tac-toe
  (:require [check.game :as core ]
            [babel.utils-for-testing :as utils]))

(defn- print-state
  []
  (println (@core/state :tl) (@core/state :tm) (@core/state :tr) "\n"
           (@core/state :ml) (@core/state :mm) (@core/state :mr) "\n"
           (@core/state :bl) (@core/state :bm) (@core/state :br)))

(defn game 
  []
  (print-state)
  (let [command (read-line)]
    (loop [choice (rand-nth [:tl :tm :tr :ml :mm :mr :bl :bm :br])]
      (if (nil? (@core/state choice)) (swap! core/state update-in [choice] (constantly 1)) (recur (rand-nth [:tl :tm :tr :ml :mm :mr :bl :bm :br]))))
    (cond (= command "tl") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:tl] (constantly 0)) (game)) 
                               #_(else) (do (println "This square is already full! Try again.") (game)))
          (= command "tm") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:tm] (constantly 0)) (game))
                               #_(else) (do (println "This square is already full! Try again.") (game)))
          (= command "tr") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:tr] (constantly 0)) (game))
                               #_(else) (do (println "This square is already full! Try again.") (game)))
          (= command "ml") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:ml] (constantly 0)) (game))
                               #_(else) (do (println "This square is already full! Try again.") (game)))
          (= command "mm") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:mm] (constantly 0)) (game))
                               #_(else) (do (println "This square is already full! Try again.") (game)))
          (= command "mr") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:mr] (constantly 0)) (game))
                               #_(else) (do (println "This square is already full! Try again.") (game)))
          (= command "bl") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:bl] (constantly 0)) (game))
                               #_(else) (do (println "This square is already full! Try again.") (game)))
          (= command "bm") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:bm] (constantly 0)) (game))
                               #_(else) (do (println "This square is already full! Try again.") (game)))
          (= command "br") (if (nil? (:tl @core/state)) (do (swap! core/state update-in [:br] (constantly 0)) (game))
                               #_(else) (do (println "This square is already full! Try again.") (game)))
          :else (do (println "This command is invalid. Please use one of the following: tl, tm, tr, ml, mm, mr, bl, bm, br") (game)))
    ))



(defn start-game 
  []
  (reset! core/state {:tl nil, :tm nil, :tr nil,
                      :ml nil, :mm nil, :mr nil,
                      :bl nil, :bm nil, :br nil})
  (game))
