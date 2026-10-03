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
  var beatIt = new Song("Beat it", (short) 256);
  IO.println(beatIt);

  var playlist = new PlayList("My Playlist", beatIt);
  IO.println(playlist.title);
}
