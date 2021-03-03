var a = "a";

const a1 = 1;
var a2 = 2;
let a3 = 3;

var b = "b";
var c = "c";

// Assignment is right-associative.
a = b = c;
print a; // expect: c
print b; // expect: c
print c; // expect: c
