abstract /*value*/ class AbstractTitle {
  String title;

  AbstractTitle(String title) {
    this.title = title;
    super();
  }

  public String title() {
    return title;
  }
}

/*value*/ class Song extends AbstractTitle {
  Song(String title) {
    super(title);
  }
}

/*not a value*/ class Song2 extends AbstractTitle {
  public Song2() {
    super("whoo");
  }
}

void main() {}
