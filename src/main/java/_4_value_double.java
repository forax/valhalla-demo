void main() {
  var notANumber = Double.NaN;
  System.out.println((Long.toHexString(Double.doubleToLongBits(notANumber)));

  var notANumber2 = Double.longBitsToDouble(0x7ff8000000000001L);
  System.out.println(("isNaN " + Double.isNaN(notANumber2));

  //var d1 = new Double(notANumber);
  //var d2 = new Double(notANumber2);

  //System.out.println(notANumber == notANumber2);
  //System.out.println(d1 == d2);
  //System.out.println(d1.equals(d2));
}
