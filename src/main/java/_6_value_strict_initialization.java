public /*value*/ class Song {
  String title;

  public Song(String title) {
    super();
    this.title = title;
    IO.println("Title: " + this);
  }

  public String toString() {
    return this.title;
  }
}

void main() {
  var song = new Song("Smooth Criminal");

  IO.println(song);
}
