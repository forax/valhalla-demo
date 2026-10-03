/*value*/ record Song(String title, short duration) {}

void main() {
  var bad1 = new Song("Bad", (short) 247);
  var bad2 = new Song("Bad", (short) 247);

  IO.println(bad1 == bad2);
}
