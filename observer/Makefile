JFLAGS = -g
JC = javac
SERVERDIR = ../server

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Observer.java

all: classes pack

classes: $(CLASSES:.java=.class)

pack:
	jar -cfm observer.jar Manifest.txt *
	chmod +x observer.jar
	mv observer.jar $(SERVERDIR)

clean:
	-rm -f *.class
