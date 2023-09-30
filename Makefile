JC = javac
#JC = javac -Xlint:unchecked
OUTPUT = out
MAIN = Main
PROCESSING_PATH = /Applications/Processing.app/Contents/Java/core.jar

CP_SEP = :
# CP_SEP = ;

CLASSES = \
./src/*.java \
./src/*/*.java \
./src/game/*/*.java \
./src/ui/*/*.java \
./src/ui/controllers/widgets/*.java

all:
	make build
	make run

build:
	$(JC) -d $(OUTPUT) -cp .$(CP_SEP)$(PROCESSING_PATH) $(CLASSES)

run:
	java -cp $(OUTPUT)$(CP_SEP)$(PROCESSING_PATH) $(MAIN)

clean:
	$(RM) $(OUTPUT)/*.class
