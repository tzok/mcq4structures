#! /bin/bash
rm rmsd-time.dat
for i in $(seq 100)
do
    args=$(yes ~/pdb/1EHZ.pdb | head -n $i | tr '\n' ' ')
    /usr/bin/time -f %e -o rmsd-time.dat --append ./rmsd.sh "$args" &> /dev/null
done
