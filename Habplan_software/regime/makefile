#makefile for compiling java programs

PACKAGE =regime  #the package name
SOURCE = *.java #a list of the java source files
CLASS = ${SOURCE:.java=.class} #list of non-inner class files

.SUFFIXES : .java .class  #important suffixes

${PACKAGE} : ${CLASS}

.java.class :       #how to make a .class file from a .java file
	cd ../; make -k #need to make from  parent directory

r run:
	cd ../; make -k r
