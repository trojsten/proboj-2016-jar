CXXFLAGS = -g -MMD -MP -Wall -Wextra -std=gnu++11

all: observer

observer: 
	javac Observer.java
	jar cfm observer.jar Manifest.txt *
	chmod a+x observer.jar
	cp -f observer.jar ../server/observer.jar
clean:


