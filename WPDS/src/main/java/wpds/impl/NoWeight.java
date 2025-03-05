package wpds.impl;

import javax.annotation.Nonnull;

public class NoWeight implements Weight {

  private static final NoWeight INSTANCE = new NoWeight();

  private NoWeight() {
    /* Singleton */
  }

  @Nonnull
  public static NoWeight getInstance() {
    return INSTANCE;
  }

  @Nonnull
  @Override
  public Weight extendWith(@Nonnull Weight other) {
    return other;
  }

  @Nonnull
  @Override
  public Weight combineWith(@Nonnull Weight other) {
    return other;
  }

  @Override
  public String toString() {
    return "";
  }
}
