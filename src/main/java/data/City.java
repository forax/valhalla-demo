package data;

import java.util.Objects;

public value class City {
  private final String name;
  private final Population population;

  public City(String name, Population population) {
    this.name = name;
    this.population = population;
    super();
  }

  @Override
  public String toString() {
    return "City(" + name + ", " + population + ")";
  }

  public String name() {
    return name;
  }

  public Population population() {
    return population;
  }
}
