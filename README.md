# Flotsam: floating-point serialization mechanism
Flotsam is an efficient base-94 encoding for double-precision floating-point
number arrays with implementations for Java and Javascript. It is optimized for
decoding performance, especially for Javascript. Current benchmarks on V8 show
it to be between 3x and 5x faster than JSON for decoding, and about half again
as fast encoding:

```
test array with 1087730 numbers
JSON    generate/parse (ms): 2908/1200
Flotsam generate/parse (ms): 1937/661
JSON    size: 25406991
Flotsam size: 10877300
```

Flotsam handles all floating-point values except for positive/negative infinity
and NaN.
