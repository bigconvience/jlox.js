function assert(actual, expected, message) {
    throw Error("assertion failed: got |" + actual + "|" +
                    ", expected |" + expected + "|" +
                    (message ? " (" + message + ")" : ""));
}
