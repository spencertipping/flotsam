package org.flotsam;

/**
 * Flotsam encoder/decoder. For example:
 * {@pre
 *   double[] xs      = {1.0, 2.0, 3.0};
 *   String   encoded = Flotsam.encode(xs);
 *   double[] ys      = Flotsam.decode(encoded);
 * }
 *
 * @author Spencer Tipping
 */
public abstract class Flotsam {
  /** Number of string characters emitted per double-precision number. */
  public static final int CHARS_PER_ELEMENT = 10;

  /** Internal lookup table for base-94 decoding. */
  protected static final long[] BASE94_DECODE;

  static {
    BASE94_DECODE = new long[128 * 8];

    long base = 1;
    for (int i = 0; i < 8; ++i) {
      for (int j = 0; j < 94; ++j)
        BASE94_DECODE[i << 7 | j + 32] = base * j;
      base *= 94;
    }
  }

  /**
   * Encodes an array of {@code double}s, returning a string. The string's
   * length is always 10 * N, where N is the number of elements in the array.
   * Each character in the string will be within the ASCII range 32-126,
   * inclusive.
   *
   * @throws IllegalArgumentException if any input is NaN or Infinity
   */
  public static String encode(final double[] xs) {
    final StringBuffer result = new StringBuffer((xs.length + 1) * 10);
    for (final double x : xs) encodeSingle(x, result);
    return result.toString();
  }

  /**
   * Decodes a string, returning an array of doubles.
   */
  public static double[] decode(final String s) {
    final double[] result = new double[s.length() / 10];
    for (int i = 0; i < result.length; ++i)
      result[i] = decodeSingle(s, i);
    return result;
  }

  /**
   * Encodes a single numeric quantity, adding the result to the specified
   * {@link StringBuffer} object. This function always adds exactly 10
   * characters.
   *
   * @throws IllegalArgumentException if the input is NaN or Infinity
   */
  public static void encodeSingle(final double x,
                                  final StringBuffer destination) {

    if (Double.isInfinite(x) || Double.isNaN(x))
      throw new IllegalArgumentException(
        "Flotsam encoding cannot be used with +/- infinity or NaN");

    // Encode the 12-bit exponent and sign in the first two characters.
    final long bits = Double.doubleToLongBits(x);
    destination.append((char) (32 + (bits >>> 58)))
               .append((char) (32 + (bits >>> 52 & 0x3f)));

    // Now encode the mantissa in base-94, least-significant digits first.
    long mantissa = bits & 0x000fffffffffffffl;
    for (int i = 0; i < 8; ++i) {
      destination.append((char) (32 + mantissa % 94));
      mantissa /= 94;
    }
  }

  /**
   * Decodes the entry at the specified location within the string. We can use
   * the fast bitwise decoder here, handing the conversion off to Java.
   */
  public static double decodeSingle(final String s,
                                    final int element) {
    long bits   = 0;
    int  offset = element * 10;

    for (int i = 0; i < 8; ++i)
      bits += BASE94_DECODE[i << 7 | s.charAt(offset + i + 2)];

    bits |= (long) (s.charAt(offset)     - 32) << 58
         |  (long) (s.charAt(offset + 1) - 32) << 52;

    return Double.longBitsToDouble(bits);
  }
}
