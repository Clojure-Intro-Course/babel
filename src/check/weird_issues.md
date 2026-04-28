- babel enters an infinite loop when processing a divide by zero exception
 - examples, observe without fixing
 - (/ 1 0) causes ClassNotFoundException spam (not anymore for some reason? I can't track down what caused it to change)
 - (even? 2 3) does not break, gets handled like a normal spec error
 - (require `h) causes a FileNotFoundException but does not break
- check.game needs to have timeout threads

- when running tic-tac-toe and reaching a full grid, because of the way my example is written, the loop-recur never stops, and this process cannot be interrupted.
- to reproduce:
- optional: modify the initial-state map to have every square filled, but in a configuration that causes a tie (such as 1 1 0, 0 0 1, 1 1 0)
 - open repl
 - ```clojure
  (require `check.game)
  (require `check.tic-tac-toe)
  (check.game/game check.tic-tac-toe/game-map)
  ```
  it then runs forever.
  for some reason this loop cannot be interrupted, and I believe this sort of uninterruptible loop would happen if any loop-recur (or possibly normal recursion?) runs forever in a student game. needs more investigation

  give number instead of string in a game command: handled normally. 
```
Execution error (IllegalArgumentException) at check.experiment/update-game (experiment.clj:7).
Expected string, got java.lang.Long
Default Error: Expected string, got java.lang.Long
```

NullPointerException works as normal - calling `(type x)` on the result of `(parse-long)` on a non-numeric value from `command` gives this:
```
Execution error (NullPointerException) at check.experiment/update-game (experiment.clj:8).
Cannot invoke "Object.getClass()" because "x" is null
An attempt to access a non-existing object: Cannot invoke "Object.getClass()" because "x" is null (NullPointerException).
```

out of memory
```clojure
(ns check.experiment)

(def initial-state {})
(defn update-game
  [state command]
  (println (type command) "is command's type")
  ;; (println (type (parse-long command)) "is type after calling parse-long") 
  (if (number? command) (update-game state (dec command)) (update-game state (dec (parse-long command))))
  #_(if (zero? command) (update-in state [:counter] #(+ % 1))) ; what if no base case?
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
```
works just fine: 
```
Clojure ran out of memory, likely due to an infinite computation or infinite recursion.

In file alpha.clj on line 1420.
Call sequence:
[stringify_keys (ns:nrepl.transport) called in file transport.clj on line 36]
[walk (ns:clojure.walk) called in file walk.clj on line 50]
[postwalk (ns:clojure.walk) called in file walk.clj on line 53]
[stringify_keys (ns:nrepl.transport) called in file transport.clj on line 36]
[ (ns:nrepl.transport.FnTransport) called in file transport.clj on line 41]
[flush (ns:clojure.core) called in file core.clj on line 3712]
[prn (ns:clojure.core) called in file core.clj on line 3722]
[apply (ns:clojure.core) called in file core.clj on line 667]
[println (ns:clojure.core) called in file core.clj on line 3734]
[update_game (ns:check.experiment) called in file experiment.clj on line 6]
```
but, infinite recursion, same as infinite tail recursion, cannot be interrupted with CTRL+C or CTRL+D. the only way is to press CTRL+Z and kill repl, which brings you back to the regular terminal - not sure if this is a bash-only thing or if it also works on pwsh
everything so far is being caught and printed to repl and then sending you back to repl, exactly as normal

