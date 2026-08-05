JAR = ./lib/*:./lib/junit-platform-console-standalone.jar
SRC = src/*.java
BIN = bin

all: compile run

compile:
	@mkdir -p $(BIN)
	javac -cp "lib/*:$(BIN)" -d $(BIN) $(SRC)

run: compile
	java -cp "$(BIN):lib/*" Gui

test: compile
	java -cp "$(BIN):lib/*" org.junit.platform.console.ConsoleLauncher execute --details=none --select-class APITest

clean:
	rm -rf $(BIN) *.odb

.PHONY: all compile run test clean
