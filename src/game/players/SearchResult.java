import java.util.Optional;

public final class SearchResult {
  public static final float MateValue = 100000f;
  public static final float Infinity = 999999999f;

  private final Move move;
  private final Optional<Float> eval;
  private final Optional<Integer> depth;

  private SearchResult(Move move, Optional<Float> eval, Optional<Integer> depth) {
    this.move = move;
    this.eval = eval;
    this.depth = depth;
  }

  public SearchResult(Move move, float eval, int depth) {
    this(move, Optional.of(eval), Optional.of(depth));
  }

  public SearchResult(Move move) {
    this(move, Optional.empty(), Optional.empty());
  }

  public Move move() {
    return move;
  }

  public Optional<Float> eval() {
    return eval;
  }

  public Optional<Integer> depth() {
    return depth;
  }

  public boolean isMate() {
    return eval.isPresent() && eval.get() == MateValue;
  }

  // TODO: Mate depth
  // public int getMateDepth() {
  // }
}
