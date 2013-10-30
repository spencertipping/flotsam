# Flotsam: floating-point serialization mechanism
Flotsam is an efficient base-94 encoding for double-precision floating-point
number arrays with implementations for Java and Javascript. It is optimized for
decoding performance, especially for Javascript. Current benchmarks on V8 show
it to be between 3x and 10x faster than JSON for decoding, and slightly faster
encoding:

```
test array with 4352479 numbers
JSON    generate/parse (ms): 10299/10706
Flotsam generate/parse (ms): 8139/1267
JSON    size: 101676722
Flotsam size: 43524790
```

Flotsam handles all floating-point values (including subnormal ones) except for
positive/negative infinity and NaN.

MIT license as usual.

## Usage
The Java and Javascript libraries each provide two functions, `encode()` and
`decode()`, that convert between double arrays and strings. In the Java API,
for example:

```java
double[] xs = ...;
String encoded = org.flotsam.Flotsam.encode(xs);
double[] ys = org.flotsam.Flotsam.decode(encoded);
```

The Javascript API is almost identical:

```js
var xs = [...];         // Float64Arrays also work
var encoded = flotsam.encode(xs);
var ys = flotsam.decode(encoded);
```

Flotsam uses the same format either way, so you can share encoded values across
languages.

## 32-bit encoding
This works identically to 64-bit float encoding, but uses only five bytes per
float, and where applicable uses `float` arrays rather than `double`s:

```java
float[] xs = ...;
String encoded = org.flotsam.Flotsam32.encode(xs);
float[] ys = org.flotsam.Flotsam32.decode(encoded);
```

And in Javascript:

```js
var xs = [...];         // Float32Arrays also work
var encoded = flotsam32.encode(xs);
var ys = flotsam32.decode(encoded);
```

Numbers are internally represented as double-precision floats in Javascript, so
you will observe a loss of precision if you use `flotsam32`.

## Sample encodings
```
           = 0
/_         = 1
O_         = -1
0 jMb |8\G = 3.141592653589793
0 _7z2bRx8 = 2.718281828459045
```
