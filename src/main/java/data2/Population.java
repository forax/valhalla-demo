package data2;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class Population {
  private int value;

  public Population(int value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "data2.Population(" + value + ')';
  }

  public int value() {
    return value;
  }

  public @NonNull Population add(@NonNull Population population) {
    Objects.requireNonNull(population);
    return new Population(value + population.value);
  }
}