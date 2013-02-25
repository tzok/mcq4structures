#! /bin/bash
classpath=bin:/$(ls lib/* | tr '\n' ':')
java -cp $classpath pl.poznan.put.cs.bioserver.comparison.TorsionLocalComparison $@
