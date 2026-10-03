/*value*/ record Song(String title, short duration) {}

void main() {
  Song song = new Song("Smooth Criminal", (short) 258);
  synchronized(song) {}

  Object o = song;
  synchronized(o) {}
}
