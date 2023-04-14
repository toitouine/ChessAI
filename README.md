# Chess AI
This is a chess AI, still in progress, using Processing 3. It's based on the negamax algorithm with alpha-beta pruning and transposition table.

You need to install the library ControlP5 and Processing Sound in order to use it.

## Bots
 - LeMaire : the most advanced bot so far : it has positionnal, material, endgame and opening knowledge (using an opening book). It has basic understanding of king safety, but it needs to be improved and it misses the pawn structure knowledge. It is pretty good in endgame thanks to the transposition table.
 - Loic : a bot intentionally pretty bad and dumb, he will try to stalemate you and will blunder really often
 - Antoine : just a random player
 - LesMoutons : this bot is very special. It is a sheep, that will try to beat you by threatening your pieces. It also cheats sometimes, so watch his pieces because some might appear... Be careful, he will steal your time and make you mouse slip too!
 - Stockfish : a bot that will try to lose the game. It is extremely hard to lose against him, that's the most challenging.
 
 You can adjust the settings using the sliders in the menu : one is for the depth, one for the max depth of the second search (quiet search only looking for captures) and the last is for the time limit of the search (iterative deepening). If no time limit is set, then the AI will just directly search at the depth entered.
 Also, there is a position editor accessible from the home page (top right icon), from which you can... well... edit a position, copy the FEN and play a game from it.

Pretty much everything is in french so good luck understanding everything :)
 
 
Home page :

<img width="1379" alt="ChessAI1" src="https://user-images.githubusercontent.com/107322964/227307760-9b2082ef-6401-4f87-8c6c-b2cc9d7ef7b1.png">

Game between two AIs :

<img width="653" alt="ChessAI2" src="https://user-images.githubusercontent.com/107322964/227307818-79daa69f-0130-478e-8d3a-0cdf67ab0d50.png">

Graph of the evaluations : 

<img width="550" alt="ChessAI3" src="https://user-images.githubusercontent.com/107322964/227307901-2e622d7e-1070-4f52-8fdc-b9f39ca50a3b.png">
