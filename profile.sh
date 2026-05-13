echo $JAVA_HOME
cd src/main/java
$JAVA_HOME/bin/javac --enable-preview --source 27 _0_mandelbrot.java
$JAVA_HOME/bin/java --enable-preview -XX:StartFlightRecording=filename=mandelbrot-alloc.jfr,settings=profile _0_mandelbrot --profile
rm _0_mandelbrot*.class
