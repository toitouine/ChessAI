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

  public MoveSave(Piece capture, byte castleState, float phase, long zobrist, Integer opponentEnPassant) {
    this.capture = capture;
    this.castleState = castleState;
    this.phase = phase;
    this.zobrist = zobrist;
    this.opponentEnPassant = opponentEnPassant;
  }

  @Override
  public String toString() {
    return "MoveSave[capture=" + capture + ",castle=" + castleState + ",phase=" + phase
           + ",zobrist=" + zobrist + ",enpassant=" + opponentEnPassant + "]";
  }
}
