/*value*/ record Song(String title, short duration) {}

static /*value*/ class PlayList {
  String title;
  Song[] songs;

  PlayList(String title, Song... songs) {
    this.title = title;
    this.songs = songs;
  }
}

void main() {
  var smoothCriminal = new Song("Smooth Criminal", (short) 258);
  var songs = new Song[] {smoothCriminal};
  var playList1 = new PlayList("Bad", songs);
  var playList2 = new PlayList("Bad", songs);

  IO.println(playList1.hashCode());
  IO.println(playList2.hashCode());
}
