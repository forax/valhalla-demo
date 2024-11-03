package data;

public value record Range(int from, int to) {
  public Range {
    if (to < from) {
      throw new IllegalArgumentException();
    }
    //System.out.println(this);
  }
}



// public data.Range(int, int);
//    descriptor: (II)V
//    flags: (0x0001) ACC_PUBLIC
//    Code:
//      stack=2, locals=3, args_size=3
//         0: iload_2
//         1: iload_1
//         2: if_icmpge     13
//         5: new           #1                  // class java/lang/IllegalArgumentException
//         8: dup
//         9: invokespecial #3                  // Method java/lang/IllegalArgumentException."<init>":()V
//        12: athrow
//        13: aload_0
//        14: iload_1
//        15: putfield      #7                  // Field from:I
//        18: aload_0
//        19: iload_2
//        20: putfield      #13                 // Field to:I
//        23: aload_0
//        24: invokespecial #16                 // Method java/lang/Record."<init>":()V
//        27: return