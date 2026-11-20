# HOW TO WRITE A GAME USING THIS FRAMEWORK
Create a new .clj file in the same project as `game.clj`, with the name of your game.
This file should contain:
- `:commands`, a string explaining how to give commands in your game.
- `:initial-state`, a map defining what the original state of your game is.
- `:update-game`, a function that takes the `state` and a string `command` and returns the new state as a map.
- `:win?`, a function that takes the `state` and a `player` (0 or 1) and returns true when the given player has won, false otherwise.
- `:draw-state`, a function that takes the `state` and returns a string that defines what should be printed to the console from that state. This defines what will be displayed to the players.
**If you are making a two-player game,** one additional function is required.
- `:enemy-turn`, which takes the `state` and returns the new state after a simulated enemy has completed its turn.
You should also create additional handling in `win?` so that it checks the enemy's win condition as well as the player's. If the player has won, `win?` should return true when `player`=0 and false when `player`=1. If the enemy has won, `win?` should return false when `player`=0 and true when `player=1.

Two example games, `number-guess` and `tic-tac-toe`, are provided.

Say you want to create the number-guess game from scratch.
Begin by creating the `game-map` hashmap:
```clojure
(def game-map {})
```
It should contain keys for each of those functions. I chose to name my functions the same thing as the keys, but that isn't necessary.
```clojure
(def initial-state {:correct (rand-int 100), :previous #{}, :stop false}) 

(def game-map {:commands "To guess a number, type 'guess ' followed by a number. For example: 'guess 42'"
    :initial-state initial-state ;; Defined at the top, so it's more readable. Remember, definitions must come before usages in Clojure.
    :update-game update-game
    :win? win?
    :draw-state draw-state})
```
Next, you should start defining your functions. `update-game` is the most complex one, as it handles most of the functionality of your game: what happens when the player does something?
```clojure
(defn update-game 
"Takes the current state and a command string, and returns the updated state after processing the command."
[state command]
  (let [guess (parse-long (or (second (re-matches #"guess ([0-9]+)" command)) ""))] ; regex matching to extract the number, get the second value from that (the matched number), return empty string if no number is found, then parse that into a number.
    (cond
      (nil? guess) (println "Invalid command") ; if the value of guess is nil, then the command was invalid
      (contains? (:previous state) guess) (println "This has already been guessed! Try something else.") ; if the state's :previous key already contains the current guess, then there's no point in checking it again
      (= guess (state :correct)) (do (println "Yippee!") (update-in state [:stop] any?)) ; stop the game if the guess was correct
      (< guess (state :correct)) (do (println "Higher.") (update-in state [:previous] conj guess)) ; it's no fun if you don't point the player toward what the right guess is, right?
      (> guess (state :correct)) (do (println "Lower.") (update-in state [:previous] conj guess))) ; has to go both ways
    (update-in state [:previous] conj guess))) ; and make sure to add the current guess to the list of previous guesses.

(def initial-state {:correct (rand-int 100), :previous #{}, :stop false})

(def game-map {:commands "To guess a number, type 'guess ' followed by a number. For example: 'guess 42'"
    :initial-state initial-state
    :update-game update-game
    :win? win?
    :draw-state draw-state})
```