/*value*/ record Song(String title, short duration) {}

void main() {
  var song1 = new Song("Bad", (short) 247);
  var song2 = new Song("Bad", (short) 247);

  IO.println(song1 == song2);
}
