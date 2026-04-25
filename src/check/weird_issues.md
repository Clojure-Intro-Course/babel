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
  Execution error (IllegalArgumentException) at check.experiment/update-game (experiment.clj:7).
Expected string, got java.lang.Long
Default Error: Expected string, got java.lang.Long