/////////////////////////////////////////////////////////////////

// Contient les informations nécessaires pour repasser à la position précédente après avoir joué un coup
// Permet également d'éviter de recalculer certaines choses

/////////////////////////////////////////////////////////////////

public class MoveSave {

  final public Piece capture; // Index de la pièce capturée
  final public byte castleState; // Droits au roque
  final public float phase; // Phase du jeu
  final public long zobrist; // Hash de la position

  public MoveSave(Piece capture, byte castleState, float phase, long zobrist) {
    this.capture = capture;
    this.castleState = castleState;
    this.phase = phase;
    this.zobrist = zobrist;
  }

  @Override
  public String toString() {
    return "MoveSave[capture=" + capture + ",castle=" + castleState + ",phase=" + phase + ",zobrist=" + zobrist + "]";
  }
}
