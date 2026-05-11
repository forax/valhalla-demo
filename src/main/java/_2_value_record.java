value record Range(int start, int end) {
  Range {
    if (start > end) {
      throw new IllegalArgumentException("start > end");
    }
  }
}

void main() {
  var range = new Range(0, 1);

  IO.println(range);
  IO.println(range.getClass().isValue());
}
