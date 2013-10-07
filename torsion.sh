#! /bin/bash

#1 Residue
#2 Symbol
#3 ALPHA
#4 BETA
#5 GAMMA
#6 DELTA
#7 EPSILON
#8 ZETA
#9 CHI
#0 TAU0
#1 TAU1
#2 TAU2
#3 TAU3
#4 TAU4
#5 ETA
#6 THETA
#7 ETA_PRIM
#8 THETA_PRIM
#9 MCQ_BACKBONE
#0 MCQ_BACKBONE_RIBOSE
#1 P

curdir=$(dirname $(readlink -f $0))
classpath="$curdir/bin":/$(ls $curdir/lib/* | tr '\n' ':')
LANG= java -cp $classpath pl.poznan.put.cs.bioserver.sandbox.PrintAngles $@\
    2>/dev/null | awk 'NR != 1 { print }'
