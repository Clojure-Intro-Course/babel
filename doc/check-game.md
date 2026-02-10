# Writing a Game
To write a game using these functions, you first start from one of two bases. (how much are we giving them? do they receive an entirely separate set of files for the two-player game vs the one-player (empty project with the one-player framework already there with a DO NOT TOUCH sign, vs empty project with the two-player framework already there with a DO NOT TOUCH sign), or are we letting them write it from an empty file?)


## The Functions In Your Game
Your game should contain certain things:
- a call to `def` defining `initial-state` for your game - this is a hashmap, and it can contain whatever values you choose to use for your game, and what they are initially set to at the start of the game.
- `(update-game [hashmap] [string])` - takes the current state, which is a hashmap, and a string, which is the command that the player enters on their turn. Returns the updated state of the game after that command has been processed.
- `(draw-state [state])` - takes the current state, and prints out a text-based representation of the current state of the game. This varies greatly depending on what your game should look like between turns.
- 

### Functions Specific to Two-Player Games
- `(win?)` - takes the current state, and returns 0 for a player win, 1 for an enemy win.

- `(enemy-turn)` - should work the same way as `(update-game)`, but instead of taking input from the player, it should randomly choose a move. Takes the current state as a hashmap, and returns the updated state of the game after the enemy's turn.
