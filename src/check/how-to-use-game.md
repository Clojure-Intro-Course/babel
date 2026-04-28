# HOW TO WRITE A GAME USING THIS FRAMEWORK
Create a new .clj file in the same project as `game.clj`, with the name of your game.
This file should contain:
- `:commands`, a string explaining how to give commands in your game.
- `:initial-state`, a map defining what the original state of your game is.
- `:update-game`, a function that takes the `state` and a string `command` and returns the new state as a map.
- `:win?`, a function that takes the `state` and a `player` (0 or 1) and returns true when the given player has won, false otherwise.
- `:draw-state`, a function that takes the `state` and returns a string that defines what should be printed to the console from that state. This defines what will be displayed to the players.
If you are making a two-player game, one additional function is required.
- `enemy-turn`, which takes the `state` and returns the new state after a simulated enemy has completed its turn.
You should also create additional handling in `win?` so that it checks the enemy's win condition as well as the player's. If the player has won, `win?` should return true when `player`=0 and false when `player`=1. If the enemy has won, `win?` should return false when `player`=0 and true when `player=1.

Two example games, `number-guess` and `tic-tac-toe`, are provided.