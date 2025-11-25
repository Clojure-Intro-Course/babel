(ns check.number-guess
  (:require [check.game :as core]
            [babel.utils-for-testing :as utils]))





(defn game
  []
  (reset! core/state {:correct (rand-int 50), :previous []})
  (let [command (read-line)]
    (cond (contains? (set (map #(str "guess " %) (@core/state :previous))) command) (do (println "This has already been guessed! Try something else.") (game))
          (= command (str "guess " (@core/state :correct))) 
            (println "Correct!")
          (< (parse-long (second (re-matches #"guess ([0-9]+)" command))) (@core/state :correct))
            (do (println "Higher.") (swap! core/state update-in [:previous] (fn [x] (conj (re-matches #"guess ([0-9]+)" command) x))) (game))
          (> (parse-long (second (re-matches #"guess ([0-9]+)" command))) (@core/state :correct)) 
            (do (println "Lower.") (swap! core/state update-in [:previous] (fn [x] (conj (re-matches #"guess ([0-9]+)" command) x))) (game)))))



