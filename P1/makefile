JAR = ./lib/*
SRC = src/*.java
BIN = bin

all: compile run

compile:
	@mkdir -p $(BIN)
	javac -cp "$(JAR)" -d $(BIN) $(SRC)

run: compile
	java -cp "$(BIN):$(JAR)" Gui

clean:
	rm -rf $(BIN) *.odb
