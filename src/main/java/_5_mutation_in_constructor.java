public /*value*/ class Song {
  String title;

  public Song(String title) {
    IO.println("Title: " + this);
    this.title = title;
  }

  public String toString() {
    return this.title;
  }
}

void main() {
  var song = new Song("Smooth Criminal");

  IO.println(song);
}
