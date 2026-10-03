/*value*/ record Song(String title, short duration) {}

void main() {
  Song song = new Song("Smooth Criminal", (short) 258);
  var weak = new WeakReference<>(song);

  IO.println(weak);
}
