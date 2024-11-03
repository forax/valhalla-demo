package data3;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

@jdk.internal.vm.annotation.LooselyConsistentValue
@jdk.internal.vm.annotation.ImplicitlyConstructible
public value class City {
  private final String name;
  private final @jdk.internal.vm.annotation.NullRestricted @NonNull Population population;

  public City(@NonNull String name, @NonNull Population population) {
    this.name = Objects.requireNonNull(name);
    this.population = Objects.requireNonNull(population);
    super();
  }

  @Override
  public String toString() {
    return "data3.City(" + name + ", " + population + ")";
  }

  public String name() {
    return name;
  }

  public @NonNull Population population() {
    return population;
  }
}
