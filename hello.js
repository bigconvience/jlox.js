function assert(actual, expected, message) {
    Error("assertion failed: got |" + actual + "|" +
                    ", expected |" + expected + "|" +
                    (message ? " (" + message + ")" : ""));
}
