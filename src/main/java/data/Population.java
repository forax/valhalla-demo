package data;

import java.util.Objects;

public /*value*/ class Population {
  private int value;

  public Population(int value) {
    this.value = value;
    super();
  }

  @Override
  public String toString() {
    return "Population(" + value + ')';
  }

  public int value() {
    return value;
  }

  public Population add(Population population) {
    Objects.requireNonNull(population);
    return new Population(value + population.value);
  }
}
