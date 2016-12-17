package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Convert integer array to a byte array using Byte-aligned code
 * only work for positive integer.
 * The compression performance if better with small numbers.
 * <p>
 * Created by Jiwei Xu on 10/27/16.
 */
public class ByteAligned {

  // to prevent create instance of this class.
  private ByteAligned() {
    throw new AssertionError();
  }

  /**
   * Convert integer array to a byte array using Byte-aligned code
   *
   * @param intVec A integer array, null input and empty vector is not allowed.
   * @return byte array, always returns a array, never null
   */
  public static byte[] encoding(short[] intVec) {
    if (intVec == null || intVec.length == 0) {
      throw new IllegalArgumentException("No null input is allowed");
    }
    Vector<Byte> res = new Vector<Byte>();
    for (int i : intVec) {
      res.addAll(converIntToByte(i));
    }
    res.trimToSize();
    byte[] finalRes = new byte[res.size()];
    for (int i = 0; i < res.size(); i++) {
      finalRes[i] = res.get(i);
    }
    return finalRes;
  }


  /**
   * Convert integer List to a byte array using Byte-aligned code
   *
   * @param intVec A integer List, null input and empty vector is not allowed.
   * @return byte array, always returns a array, never null
   */
  public static byte[] encoding(List<Integer> intVec) {
    if (intVec == null || intVec.size() == 0) {
      throw new IllegalArgumentException("No null input is allowed");
    }
    Vector<Byte> res = new Vector<Byte>();
    for (int i : intVec) {
      res.addAll(converIntToByte(i));
    }
    res.trimToSize();
    byte[] finalRes = new byte[res.size()];
    for (int i = 0; i < res.size(); i++) {
      finalRes[i] = res.get(i);
    }
    return finalRes;
  }

  /**
   * Convert a compressed byte array back to the array of integer
   * null input and empty array is not allowed.
   * Random input is not tested. Behavior might be strange with unexpected input.
   *
   * @param byteVec A compressed byte array, null input and empty array is not allowed.
   * @return decoded integer array.
   */
  public static int[] decoding(byte[] byteVec) {
    if (byteVec == null || byteVec.length == 0) {
      throw new IllegalArgumentException("No null input is allowed");
    }
    Vector<Integer> res = new Vector<Integer>();
    Vector<Byte> temp = new Vector<Byte>();
    for (byte b : byteVec) {
      temp.add(b);
      if (b < 0 || b == 128) {
        res.add(convertByteVecToInt(temp));
        temp = new Vector<Byte>();
      }
    }
    res.trimToSize();
    int[] finalRes = new int[res.size()];
    for (int i = 0; i < res.size(); i++) {
      finalRes[i] = res.get(i);
    }
    return finalRes;

  }

  // Convert a single integer to a vector of Bytes.
  private static Vector<Byte> converIntToByte(int a) {
    Vector<Byte> res = new Vector<Byte>();
    if (a == 0) {
      res.add((byte) 128);
      return res;
    }
    int value = a;
    boolean first = true;
    while (true) {
      if (value == 0) {
        break;
      }
      int remainder = value % 128;
      if (first) {
        if (remainder == 0) {
          res.add((byte) 128);
        } else {
          res.add((byte) (-remainder));
        }
        first = false;
      } else {
        res.add((byte) remainder);
      }
      value = value / (128);
    }
    Collections.reverse(res);
    return res;
  }

  // Convert a compressed vector of Bytes (from a single integer) to an integer
  private static int convertByteVecToInt(Vector<Byte> byteVec) {
    int res = 0;
    for (int i = 0; i < byteVec.size(); i++) {
      int index = byteVec.size() - i - 1;
      res = res + Math.abs(byteVec.get(index).byteValue()) % 128 * (int) Math.pow(2, i * 7);
    }
    return res;
  }

/* Testing correctness
  public static void main(String[] args) {
    Random rand = new Random();
    int[] a = new int[100000];
    for (int i = 0; i < 100000; i++) {
      a[i] = rand.nextInt(512);
    }
    byte[] b = encoding(a);
    System.out.println(b.length);
    int[] c = decoding(b);
    for (int i = 0; i < 100000; i++) {
      if (a[i] != c[i]) {
        System.out.println("wrong value " + a[i] + " vs " + a[i]);
      }
    }
    System.out.println("Compression rate: " + (float)b.length/(4*a.length));
  }
  */
}
