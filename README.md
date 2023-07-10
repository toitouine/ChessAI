# Chess AI
Multiple chess AIs using processing 3. They are based on the negamax algorithm with alpha-beta pruning, transposition table and quiet move search.
You need to install the libraries ControlP5 and Processing Sound in order to use it.
([Programs for debugging and configuring](https://github.com/toitouine/ChessAITools))

## Bots
 - LeMaire : the most advanced bot so far : it has positionnal, material, endgame and opening knowledge (using an opening book). It has basic understanding of king safety, but it needs to be improved and it misses the pawn structure knowledge. It is pretty good in endgame thanks to the transposition table.
 - Loic : a pretty bad bot, he will try to stalemate you and will blunder really often
 - Antoine : just a random player
 - LesMoutons : this bot is very special. It is a sheep, that will try to beat you by threatening your pieces. It also cheats sometimes, so watch his pieces because some might appear... Be careful, he will steal your time and make you mouse slip too!
 - Stockfish : a bot that will try to lose the game. It is extremely hard to lose against him, that's the most challenging.
 
You can adjust the settings using the sliders in the menu : one is for the time limit of the search (iterative deepening) and the other one is for the depth to search directly if no time limit is set. There is also a position editor accessible from the home page (top right icon), from which you can edit a position, copy the FEN and play a game from it.
 
 
Home page :

<img width="1100" alt="ChessAI1" src="https://github.com/toitouine/ChessAI/assets/107322964/a20d99fc-84a6-4271-b895-d14bd17fc67d">

Game between two AIs :

<img width="653" alt="ChessAI2" src="https://user-images.githubusercontent.com/107322964/227307818-79daa69f-0130-478e-8d3a-0cdf67ab0d50.png">

Graph of the evaluations : 

<img width="550" alt="ChessAI3" src="https://user-images.githubusercontent.com/107322964/227307901-2e622d7e-1070-4f52-8fdc-b9f39ca50a3b.png">
