void main() {
  var notANumber = Double.NaN;
  IO.println(Long.toHexString(Double.doubleToLongBits(notANumber)));

  var notANumber2 = Double.longBitsToDouble(0x7ff8000000000001L);
  IO.println("isNaN " + Double.isNaN(notANumber2));

  IO.println(notANumber == notANumber2);

  var d1 = Double.valueOf(notANumber);
  var d2 = Double.valueOf(notANumber2);

  IO.println(d1 == d1);
  IO.println(d1 == d2);
}
