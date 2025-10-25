// /Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home/bin/java src/main/java/_3_value_wrapper.java
// /Users/forax/valhalla-live/valhalla/build/macosx-aarch64-server-release/images/jdk/bin/java src/main/java/_3_value_wrapper.java
// /Users/forax/valhalla-live/valhalla/build/macosx-aarch64-server-release/images/jdk/bin/java --enable-preview src/main/java/_3_value_wrapper.java

void main() {

  var i1 = Integer.valueOf(1024);
  IO.println(i1);
  var i2 = Integer.valueOf(1024);
  IO.println(i2);

  IO.println(i1 == i2);
  IO.println(i1.equals(i2));

}
