BUILD_DIR := build


# Remove all build outputs and intermediate files.
clean:
	@ rm -rf $(BUILD_DIR)
	@ rm -rf gen


# Run the tests for the final version of jlox.
test_jlox: jlox $(TEST_SNAPSHOT)
	@ dart $(TEST_SNAPSHOT) jlox

# Compile the Java interpreter .java files to .class files.
jlox:
	@ $(MAKE) -f util/java.make DIR=src PACKAGE=lox

.PHONY: jlox