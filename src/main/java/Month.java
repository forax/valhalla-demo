import jdk.internal.value.ValueClass;

public value class Month {
  private final int value;

  public Month(int value) {
    this.value = value;
    super();
  }

  // compile
  // /Users/forax/valhalla-live/valhalla/build/macosx-aarch64-server-release/images/jdk/bin/javac --enable-preview -source 24 --add-exports=java.base/jdk.internal.value=ALL-UNNAMED --add-exports=java.base/jdk.internal.vm.annotation=ALL-UNNAMED src/main/java/Month.java
  // execute
  // /Users/forax/valhalla-live/valhalla/build/macosx-aarch64-server-release/images/jdk/bin/java --enable-preview --add-exports=java.base/jdk.internal.value=ALL-UNNAMED --add-exports=java.base/jdk.internal.vm.annotation=ALL-UNNAMED -cp src/main/java Month
  static void main() {
    IO.println(java.util.Arrays.toString(Month.class.getAnnotations()));

    var array = (Month[]) ValueClass.newNullableAtomicArray(Month.class, 10);
    var list = java.util.Arrays.asList(array);
    IO.println(list);
  }
}

