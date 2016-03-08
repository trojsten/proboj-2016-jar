SERVERDIR = ../server
OBSERVERDIR = ../observer
JFLAGS = -g
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Leveler.java

all: classes pack

classes: $(CLASSES:.java=.class)

pack:
	jar cfm leveler.jar Manifest.txt *
	chmod +x leveler.jar

clean:
	-rm -f *.class
