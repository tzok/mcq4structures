#! /bin/bash
for alg in mcq rmsd
do
    rm $alg-time-*.dat 2> /dev/null
    for j in $(seq 10)
    do
        echo $alg $j
        for i in $(seq 100)
        do
            args=$(yes ~/pdb/1EHZ.pdb | head -n $i | tr '\n' ' ')
            /usr/bin/time -f %e -o $alg-time-$(printf '%02d' $j).dat --append ./$alg.sh "$args" &> /dev/null
        done
    done
done
