#! /bin/bash
classpath=$(ls lib/* | tr '\n' ':')
java -cp bin/:$classpath pl.poznan.put.cs.bioserver.gui.Gui
