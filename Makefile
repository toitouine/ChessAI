JC = javac
# JC = javac -Xlint:unchecked
OUTPUT = out
MAIN = Main
PROCESSING = ./processing/core.jar

CP_SEP = :
# CP_SEP = ;

CLASSES = \
./src/*.java \
./src/util/*.java \
./src/ui/*.java \
./src/ui/menu/*.java \
./src/ui/editor/*.java \
./src/ui/game/*.java \
./src/ui/pscene/*.java \
./src/ui/pscene/controllers/*.java \
./src/ui/pscene/controllers/widgets/*.java \
./src/game/*.java \
./src/game/players/*.java \
./src/game/hacker/*.java \
./src/game/board/*.java \
./src/game/board/move/*.java

all:
	make build
	make run

build:
	$(JC) -d $(OUTPUT) -cp .$(CP_SEP)$(PROCESSING) $(CLASSES)

run:
	java -cp $(OUTPUT)$(CP_SEP)$(PROCESSING) $(MAIN)

clean:
	$(RM) $(OUTPUT)/*.class
