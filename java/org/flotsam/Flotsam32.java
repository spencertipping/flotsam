package org.flotsam;

/**
 * 32-bit single-precision Flotsam encoder/decoder. For example:
 * {@pre
 *   float[] xs      = {1.0f, 2.0f, 3.0f};
 *   String  encoded = Flotsam.encode(xs);
 *   float[] ys      = Flotsam.decode(encoded);
 * }
 *
 * @author Spencer Tipping
 */
public abstract class Flotsam32 {
  /** Number of string characters emitted per single-precision number. */
  public static final int CHARS_PER_ELEMENT = 5;

  /** Internal lookup table for base-94 decoding. */
  protected static final int[] BASE94_DECODE;

  static {
    BASE94_DECODE = new int[128 * 5];

    int base = 1;
    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 94; ++j)
        BASE94_DECODE[i << 7 | j + 32] = base * j;
      base *= 94;
    }
  }

  /**
   * Encodes an array of {@code float}s, returning a string. The string's
   * length is always 5 * N, where N is the number of elements in the array.
   * Each character in the string will be within the ASCII range 32-126,
   * inclusive.
   *
   * @throws IllegalArgumentException if any input is NaN or Infinity
   */
  public static String encode(final float[] xs) {
    final StringBuffer result = new StringBuffer((xs.length + 1) * 10);
    for (final float x : xs) encodeSingle(x, result);
    return result.toString();
  }

  /**
   * Decodes a string, returning an array of floats.
   */
  public static float[] decode(final String s) {
    final float[] result = new float[s.length() / 5];
    for (int i = 0; i < result.length; ++i)
      result[i] = decodeSingle(s, i);
    return result;
  }

  /**
   * Encodes a single numeric quantity, adding the result to the specified
   * {@link StringBuffer} object. This function always adds exactly 5
   * characters.
   *
   * @throws IllegalArgumentException if the input is NaN or Infinity
   */
  public static void encodeSingle(final float x,
                                  final StringBuffer destination) {

    if (Float.isInfinite(x) || Float.isNaN(x))
      throw new IllegalArgumentException(
        "Flotsam encoding cannot be used with +/- infinity or NaN");

    // Floats are simply radix-encoded; there is no sign/exponent separation
    // because that would cause the encoding to overflow into a sixth byte.
    long bits = Float.floatToIntBits(x) & 0xffffffffl;
    for (int i = 0; i < 5; ++i) {
      destination.append((char) (32 + bits % 94));
      bits /= 94;
    }
  }

  /**
   * Decodes the entry at the specified location within the string. We can use
   * the fast bitwise decoder here, handing the conversion off to Java.
   */
  public static float decodeSingle(final String s, final int element) {
    int bits   = 0;
    int offset = element * 5;

    for (int i = 0; i < 5; ++i)
      bits += BASE94_DECODE[i << 7 | s.charAt(offset + i)];

    return Float.intBitsToFloat(bits);
  }
}
