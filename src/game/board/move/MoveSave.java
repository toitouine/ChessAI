/////////////////////////////////////////////////////////////////

// Contient les informations nécessaires pour repasser à la position précédente après avoir joué un coup
// Permet également d'éviter de recalculer certaines choses

/////////////////////////////////////////////////////////////////

import java.io.Serializable;

public class MoveSave implements Serializable {

  final public Piece capture; // Index de la pièce capturée
  final public byte castleState; // Droits au roque
  final public float phase; // Phase du jeu
  final public long zobrist; // Hash de la position
  final public Integer opponentEnPassant; // Case éventuelle d'en passant de l'adversaire
  final public float[] material; // Évaluation matérielle de la position
  final public float[] positional; // Évaluation positionelle de la position

  public MoveSave(Piece capture, byte castleState, float phase, long zobrist, Integer opponentEnPassant, float[] material, float[] positional) {
    this.capture = capture;
    this.castleState = castleState;
    this.phase = phase;
    this.zobrist = zobrist;
    this.opponentEnPassant = opponentEnPassant;
    this.material = new float[2];
    this.positional = new float[2];
    this.material[Player.White] = material[Player.White];
    this.material[Player.Black] = material[Player.Black];
    this.positional[Player.White] = positional[Player.White];
    this.positional[Player.Black] = positional[Player.Black];
  }

  @Override
  public String toString() {
    return "MoveSave[capture=" + capture + ",castle=" + castleState + ",phase=" + phase
           + ",zobrist=" + zobrist + ",enpassant=" + opponentEnPassant + ",mat=" + material[0]
           + "/" + material[1] + ",pos=" + positional[0] + "/" + positional[1] + "]";
  }
}
