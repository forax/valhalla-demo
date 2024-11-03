package data2;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class Capital extends City {
  private final @NonNull Population population;

  public Capital(@NonNull Population population) {
    super();
    this.population = Objects.requireNonNull(population);
  }

  @Override
  void init() {
    System.out.println(this);
  }

  @Override
  public String toString() {
    return "data2.Capital(" + population + ')';
  }
}
