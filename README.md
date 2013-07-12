# Flotsam: floating-point serialization mechanism
Flotsam is an efficient base-94 encoding for double-precision floating-point
number arrays with implementations for Java and Javascript. It is optimized for
decoding performance, especially for Javascript.

## Encoding details
Flotsam arrays are encoded as base-94 ASCII strings, each character of which is
within the range 32-126, inclusive. Each 64-bit double occupies 10 encoded
characters. The first two encode the 12 bits for sign and exponent (six bits
per character), and the next eight are a base-94 encoding of the mantissa.
