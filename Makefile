BUILD_DIR := build
TOOL_SOURCES := tool/pubspec.lock $(shell find tool -name '*.dart')
BUILD_SNAPSHOT := $(BUILD_DIR)/build.dart.snapshot
TEST_SNAPSHOT := $(BUILD_DIR)/test.dart.snapshot

default: jlox

# Run pub get on tool directory.
get:
	@ cd ./tool; pub get
# Remove all build outputs and intermediate files.
clean:
	@ rm -rf $(BUILD_DIR)


# Run the tests for the final version of jlox.
test_jlox: jlox $(TEST_SNAPSHOT)
	@ dart $(TEST_SNAPSHOT) jlox

# Compile the Java interpreter .java files to .class files.
jlox:
	@ $(MAKE) -f util/java.make DIR=java PACKAGE=lox

$(TEST_SNAPSHOT): $(TOOL_SOURCES)
	@ mkdir -p build
	@ echo "Compiling Dart snapshot..."
	@ dart --snapshot=$@ --snapshot-kind=app-jit tool/bin/test.dart jlox

.PHONY: jlox test