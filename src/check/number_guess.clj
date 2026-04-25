(ns check.number-guess
  (:require [check.game]))


(def initial-state {:correct (rand-int 100), :previous #{}, :stop false})


(defn update-game
  "Takes the current state and a command string, and returns the updated state after processing the command."
  [state command]
  (let [guess (parse-long (or (second (re-matches #"guess ([0-9]+)" command)) ""))]
    ;; (println "guess = " guess)
    (cond
      (nil? guess) (println "Invalid command")
      (contains? (:previous state) guess) (println "This has already been guessed! Try something else.")
      (= guess (state :correct)) (do (println "Yippee!") (update-in state [:stop] any?))
      (< guess (state :correct)) (do (println "Higher.") (update-in state [:previous] conj guess))
      (> guess (state :correct)) (do (println "Lower.") (update-in state [:previous] conj guess)))
    (update-in state [:previous] conj guess)))

(defn draw-state
  "Takes the current state and returns the string to print based on that state."
  [state]
  (str "You have guessed: " (:previous state)))


(def game-map
  {:commands "To guess a number, type 'guess ' followed by a number. For example: 'guess 42'"
   :initial-state initial-state
   :update-game update-game
   :win? (fn [state player]
           (and (= player 0) (contains? (:previous state) (:correct state))))
   :draw-state draw-state})


;; (defn game
;;   []
;;   (let [command (read-line)]
;;     (cond (contains? (set (map #(str "guess " %) (@core/state :previous))) command) (do (println "This has already been guessed! Try something else.") (game))
;;           (= command (str "guess " (@core/state :correct))) 
;;             (println "Correct!")
;;           (< (parse-long (second (re-matches #"guess ([0-9]+)" command))) (@core/state :correct))
;;             (do (println "Higher.") (swap! core/state update-in [:previous] (fn [x] (conj (re-matches #"guess ([0-9]+)" command) x))) (game))
;;           (> (parse-long (second (re-matches #"guess ([0-9]+)" command))) (@core/state :correct)) 
;;             (do (println "Lower.") (swap! core/state update-in [:previous] (fn [x] (conj (re-matches #"guess ([0-9]+)" command) x))) (game)))))



;; (defn start-game
;;   []
;;   (reset! core/state {:correct (rand-int 50), :previous []})
;;   (game))

(defn update-game-test
  []
  (check.fns/check-equal (update-game {:correct 42, :previous #{}, :stop false} "guess 50")
                     {:correct 42, :previous #{50}, :stop false}))

