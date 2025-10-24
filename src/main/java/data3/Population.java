package data3;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public value class Population {
  private int value;

  public Population(int value) {
    this.value = value;
    super();
  }

  @Override
  public String toString() {
    return "data3.Population(" + value + ')';
  }

  public int value() {
    return value;
  }

  public @NonNull Population add(@NonNull Population population) {
    Objects.requireNonNull(population);
    return new Population(value + population.value);
  }
}
