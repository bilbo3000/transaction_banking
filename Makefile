# variable for compiler 
JC = javac

# define targets for building
.SUFFIXES: .java .class

# target entry to crate .class file from .java file
.java.class: 
	$(JC) $*.java

CLASSES = \
	ATM.java \
	Bank.java \
	BankInterface.java

# default make target entry
default: classes

# suffix replacement with a macro
classes: $(CLASSES:.java=.class)

clean: 
	$(RM) *.class	
