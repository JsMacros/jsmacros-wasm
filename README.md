# JsMacros-WASM

wasm for JsMacros,

## Some Lang Spec Stuff

### declaring extern on jsmacros modules

to declare jsmacros stuff, you need to know the rules for how we deal with java objects.
if it has a direct wasm equivelent (like an integer), it uses that,
if it has an indirect equivelent (like short/char/byte/boolean) it's an i32.
if it's a String: as a parameter it's a c-string pointer, as a return value,
it will return a jPtr to a java string.
otherwise it's a jPtr.

### handling overloads

wasmtime doesn't like method overloads, so if there are multiple of the same method in a class, it will get the number of args appended to the method name, for example there are 2 `post` methods in `Request`, 
hence they will be `post2` and `post3`,
if this still isn't enough to distinguish them, they will simply be numbered in the natural ordering (top to bottom),
so for example there are 3 total `getBlock` methods in `World`.
these would be `getBlock3`, `getBlock1_1` and `getBlock1_2`

### so what the f is a jPtr?

a jPtr is a pointer to a java object, to actually use it, see the
`Java` module, which provides manipulation and visibility for java objects.

## Java module

### convertJavaString(jPtr, offset, maxLength) -> void

converts a java string to a c-string, you must provide a ptr for an array to `offset`
the array length is required to be exact for a c-string, meaning you must include room for the null terminator.

### convertJavaPrimitiveInt(jPtr) -> i32

### convertJavaPrimitiveLong(jPtr) -> i64

### convertJavaPrimitiveFloat(jPtr) -> f32

### convertJavaPrimitiveDouble(jPtr) -> f64

### getJavaType(jPtr) -> jPtr

prints the canonical class name of the java object at the ptr to a new jPtr that 
contains a java string.

### freeJavaObject(jPtr) -> void

-_-