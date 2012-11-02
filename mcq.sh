#! /bin/bash
curdir=$(dirname $(readlink -f $0))
classpath=$curdir/lib/AAProperties-jar-with-dependencies.jar:$curdir/lib/biojava3-alignment-3.0.2.jar:$curdir/lib/biojava3-core-3.0.2.jar:$curdir/lib/biojava3-structure-3.0.2.jar:$curdir/lib/biojava3-structure-gui-3.0.2.jar:$curdir/lib/diffutils-1.2.1.jar:$curdir/lib/jcommon-1.0.17.jar:$curdir/lib/jfreechart-1.0.14.jar:$curdir/lib/JmolData.jar:$curdir/lib/Jmol.jar:$curdir/lib/log4j.jar:$curdir/bin/:$curdir
java -cp $classpath pl.poznan.put.cs.bioserver.comparison.MCQ $@
