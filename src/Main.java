// TODO :
//
// - Annuler les coups
// - Génération de coups
// - Déroulement de la partie, Game Manager, Players, IA et Humain
// - UI : Suite
// - IA, Hacker, Book, PGN, et autre classes utiles
// - UI : Fin

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
