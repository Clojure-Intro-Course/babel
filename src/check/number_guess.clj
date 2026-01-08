(ns check.number-guess)


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
      (> guess (state :correct)) (do (println "Lower.") (update-in state [:previous] conj guess))
      :else (update-in state [:previous] conj guess)))
  )

(defn draw-state
  "Takes the current state and prints information from it to the console."
  [state]
  (println (str "You have guessed: " (:previous state))))


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

