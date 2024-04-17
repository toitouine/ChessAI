/////////////////////////////////////////////////////////////////

// Échecs on Java, Antoine Mechulam
// https://github.com/toitouine/ChessAI

// Le dossier ui contient les fichiers concernant l'interface graphique (menu
// principal, affichage du plateau, éditeur de positions...)
// Le dossier game contient les fichiers relatifs au fonctionnement de la
// partie (plateau, joueurs, génération des coups...)
// Le dossier util contient des classes utilisables partout qui facilitent
// certaines actions

// Les IAs disponibles sont :
// - LeMaire
// - Les Moutons
// - Loic
// - Antoine
// - Stockfish

/////////////////////////////////////////////////////////////////

import processing.core.PApplet;

public class Main {
  public static void main(String[] args) {
    // Initialisation
    GameManager gameManager = GameManager.getInstance();
    MainApplet mainApplet = new MainApplet();

    // Démarre l'applet principal
    String[] processingArgs = {Config.General.name};
    PApplet.runSketch(processingArgs, mainApplet);
  }
}
