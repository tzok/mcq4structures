#! /bin/bash
classpath=lib/AAProperties-jar-with-dependencies.jar:lib/biojava3-alignment-3.0.2.jar:lib/biojava3-core-3.0.2.jar:lib/biojava3-structure-3.0.2.jar:lib/biojava3-structure-gui-3.0.2.jar:lib/diffutils-1.2.1.jar:lib/jcommon-1.0.17.jar:lib/jfreechart-1.0.14.jar:lib/JmolData.jar:lib/Jmol.jar:lib/log4j.jar:bin/
java -cp $classpath pl.poznan.put.cs.bioserver.comparison.RMSD $@
