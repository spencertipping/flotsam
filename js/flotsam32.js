/**
 * Flotsam 32-bit single precision encoder/decoder. For example:
 *
 * var xs = [1.0, 2.0, Math.PI];
 * var s  = flotsam.encode(xs);
 * var ys = flotsam.decode(s);
 *
 * 'ys' will be a copy of 'xs', but with each number encoded at only single
 * precision.
 *
 * Note that this module does all of its math using doubles so that runtimes
 * without Float32Array are supported.
 */
flotsam32 = (function () {

var flotsam = {};

/** Set to true if the environment supports typed arrays, false otherwise. */
flotsam.typed_array_support = typeof Float64Array !== 'undefined';

/**
 * Encodes an array of numbers and returns a string. Throws an error if any
 * number is infinite or NaN. xs may be a normal or typed array.
 */
flotsam.encode = function (xs) {
  // According to jsperf.com, consing strings one element at a time is faster
  // than building an array and joining.
  var result = '';
  for (var i = 0, l = xs.length; i < l; ++i)
    result += flotsam.encode_single(xs[i]);
  return result;
};

/**
 * Decodes a string into an array of numbers. Uses a Float64Array if supported,
 * otherwise uses a regular Javascript array.
 */
flotsam.decode = function (s) {
  var result = flotsam.typed_array_support
             ? new Float64Array(s.length / 5)
             : new Array(s.length / 5);
  for (var i = 0, l = result.length; i < l; ++i)
    result[i] = flotsam.decode_single(s, i);
  return result;
};

// Floating point constants
///////////////////////////

var float_powers = flotsam.typed_array_support
                 ? new Float64Array(256)
                 : new Array(256);
float_powers[127] = 1.0;
for (var i = 128, base = 1.0; i <  255; ++i) float_powers[i] = base *= 2.0;
for (var i = 126, base = 1.0; i >= 1;   --i) float_powers[i] = base *= 0.5;

// Same exponent, different interpretation.
float_powers[0]   = float_powers[1];
float_powers[255] = float_powers[254];

var mantissa_norm = float_powers[127 + 22];

var base94_decode = new Array(128 * 5);
var base = 1;
for (var i = 0; i < 5; ++i) {
  for (var j = 0; j < 94; ++j)
    base94_decode[i << 7 | j + 32] = base * j;
  base *= 94;
}

var digits = new Array(94);
for (var i = 0; i < 94; ++i) digits[i] = String.fromCharCode(i + 32);

var digit_pairs = new Array(94 * 94);
for (var i = 0; i < 94; ++i)
  for (var j = 0; j < 94; ++j)
    digit_pairs[j * 94 + i] = digits[i] + digits[j];

flotsam.encode_single = function (x) {
  // Enabling the following decreases performance by about 10%, but is useful
  // if you're unsure of your inputs.
  /*
    if (!isFinite(x) || isNaN(x))
      throw new Error(
        'Flotsam encode_single cannot be used with +/- infinity or NaN');
  */

  if (x === 0) return '     ';

  var sign = x < 0;
  if (sign) x = -x;

  // Binary-split for the observed exponent. We converge on the exact value in
  // 8 iterations.
  var l = 0, u = 256, m;
  if (x < float_powers[m = l + u >> 1]) u = m; else l = m;
  if (x < float_powers[m = l + u >> 1]) u = m; else l = m;
  if (x < float_powers[m = l + u >> 1]) u = m; else l = m;
  if (x < float_powers[m = l + u >> 1]) u = m; else l = m;      // 4
  if (x < float_powers[m = l + u >> 1]) u = m; else l = m;
  if (x < float_powers[m = l + u >> 1]) u = m; else l = m;
  if (x < float_powers[m = l + u >> 1]) u = m; else l = m;
  if (x < float_powers[m = l + u >> 1]) u = m; else l = m;      // 8

  var exponent = l;

  // At this point we have an exponent such that float_powers[exponent] is no
  // larger than the quantity, with equality iff all mantissa bits are zero. So
  // exponent is the logical position of the implied high bit, or zero for
  // subnormal numbers.
  //
  // In order to get to the bits, we need to first shift the number to set the
  // exponent to 127 (the encoding of 0). Then we subtract off the implied
  // mantissa norm, at which point we have just the remaining bits that we can
  // access by casting to an integer or using FP modular arithmetic.
  var mantissa = exponent !== 0
               ? (x * float_powers[254 - exponent] - 1.0) * mantissa_norm
               :  x * float_powers[253]                   * mantissa_norm;

  // 32-bit uses radix encoding for all of the bits, rather than encoding
  // sign+exponent separately. This means we have two pairs and one stray
  // digit.
  var bits = (+sign << 31 | exponent << 23 | mantissa) >>> 0;
  var d45  =  bits                  * (1.0 / 830584) | 0;
  var d23  = (bits -= d45 * 830584) * (1.0 / 94)     | 0;
  var d1   = (bits -= d23 * 94)                      | 0;

  return digits[d1] + digit_pairs[d23] + digit_pairs[d45];
};

/**
 * Decodes a single number from the position specified within the given string.
 * The position is the logical element position, not the character offset.
 */
flotsam.decode_single = function (s, n) {
  var i    = n * 5;
  var bits = base94_decode[0 << 7 | s.charCodeAt(i)]
           + base94_decode[1 << 7 | s.charCodeAt(i + 1)]
           + base94_decode[2 << 7 | s.charCodeAt(i + 2)]
           + base94_decode[3 << 7 | s.charCodeAt(i + 3)]
           + base94_decode[4 << 7 | s.charCodeAt(i + 4)];

  var exponent = bits >>> 23 & 0xff;
  var result   = (bits & 0x003fffff) / mantissa_norm + (exponent ? 1 : 0);

  if (bits & 0x80000000) result = -result;
  result *= float_powers[exponent];
  return result;
};

return flotsam;

})();
