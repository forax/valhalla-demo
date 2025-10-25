package data2;

import org.jspecify.annotations.NonNull;

public class Village {
  private final @NonNull Population population;

  public Village(@NonNull Population population) {
    this.population = population;
    super();
    System.out.println(this);
  }

  @Override
  public String toString() {
    return "data2.Village(" + population + ")";
  }
}
