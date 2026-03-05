# Writing a Game
To write a game, you call the `check.game/game` function with a hashmap containing the following keywords:
`:initial-state` - this should be associated with a hashmap for the initial state of the game, containing any sort of information that would be present right at the start.
`:update-game` - this should be associated with a function that takes the state as a hashmap and a command entered by the player, and returns the state of the game after processing that command. To stop the game, adding a truthy value to `:stop` in the state can be done when the correct solution has been reached.
`:draw-state` - this should be associated with a function that takes the state and prints out a representation of the game state, to show the player what's currently going on. What it returns is not relevant; it can be nil or any other value.

Some games have more than one player; to simplify that, the turn that should be the second player's turn is instead simulated by another function. 


### Functions Specific to Two-Player Games
- `(win?)` - takes the current state, and returns 0 for a player win, 1 for an enemy win.

- `(enemy-turn)` - should work the same way as `(update-game)`, but instead of taking input from the player, it should randomly choose a move. Takes the current state as a hashmap, and returns the updated state of the game after the enemy's turn.
