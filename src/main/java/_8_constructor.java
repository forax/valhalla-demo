value class V {
  int v;
  V(int v) {
    this.v = v;
    // IO.println(this);
    super();
  }
}

void main() {
  var v = new V(0);
  var v2 = new V(1);
  IO.println(v == v2);
}