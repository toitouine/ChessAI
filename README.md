# Chess AI
This is a chess AI, still in progress, using Processing 3. It's based on the negamax algorithm with alpha-beta pruning and transposition table.

You need to install the library ControlP5 and Processing Sound in order to use it.

There are 5 bots :
 - LeMaire : my most advanced bot so far : it has positionnal, material, endgame and opening knowledge (using an opening book). It has basic understanding of king safety, but it needs to be improved and it misses the pawn structure knowledge. It is pretty good in endgame thanks to the transposition table.
 - Loic : a bot intentionally pretty bad and dumb, he will try to stalemate you and will blunder really often
 - Antoine : just a random player
 - LesMoutons : this bot is very special. It is a sheep, that will try to beat you by threatening your pieces. It also cheats sometimes, so watch his pieces because some might appear... Be careful, he will steal your time and make you mouse slip too!
 - Stockfish : a bot that will try to lose the game. It is extremely hard to lose against him, that's the most challenging.
 
 You can adjust the settings using the sliders in the menu (one for the depth, one for the max depth of the second search (quiet search, only looking for captures) and one for the time limit (only for LeMaire). If no time limit is set, then the AI will just directly search at the depth entered).
 Also, there is a position editor accessible from the home page (top right icon), from which you can... well... edit a position, copy the FEN and play a game from it.

Pretty much everything is in french so good luck understanding everything ;)
 
 
Home page :

<img width="1193" alt="Capture d’écran 2022-11-27 à 12 53 16" src="https://user-images.githubusercontent.com/107322964/204137021-a3bcbc62-46c4-49ed-82e8-27b38cbaa9f2.png">

Game between two AIs :

<img width="652" alt="Capture d’écran 2022-11-27 à 12 56 22" src="https://user-images.githubusercontent.com/107322964/204137014-6cff8840-31c6-4f46-afa4-d2563cadb84e.png">

Graph of the advantage : 

<img width="548" alt="197406027-825e2e3f-f612-4fe7-88db-754986557901" src="https://user-images.githubusercontent.com/107322964/204137008-bf4686e1-7f9f-4b4d-991d-6ec15dfbd851.png">
