#! /bin/bash
curdir=$(dirname $(readlink -f $0))
classpath="$curdir/bin":/$(ls $curdir/lib/* | tr '\n' ':')
java -cp $classpath pl.poznan.put.cs.bioserver.sandbox.Clusterer 2>/dev/null
