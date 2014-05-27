package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.SVDSuperimposer;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.atoms.AtomName;
import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.common.ResidueType;
import pl.poznan.put.comparison.IncomparableStructuresException;
import pl.poznan.put.helper.StructureHelper;
import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.ResidueAngles;

public class FragmentSuperimposer {
    public enum AtomFilter {
        ALL, BACKBONE, MAIN
    }

    public static FragmentSuperposition superimpose(
            SelectionMatch selectionMatch, AtomFilter filter, boolean onlyHeavy)
            throws IncomparableStructuresException {
        List<Atom> atomsL = new ArrayList<>();
        List<Atom> atomsR = new ArrayList<>();

        for (int i = 0; i < selectionMatch.getSize(); i++) {
            FragmentMatch fragment = selectionMatch.getFragmentMatch(i);
            FragmentComparison fragmentComparison = fragment.getBestResult();

            for (ResidueComparison residueComparison : fragmentComparison) {
                ResidueAngles left = residueComparison.getLeft();
                ResidueAngles right = residueComparison.getRight();
                ResidueType residueType = left.getResidueType();
                MoleculeType chainType = residueType.getChainType();
                AtomName[] atoms = chainType.getBackboneAtoms();
                List<AtomName> atomNames = new ArrayList<>();

                switch (filter) {
                case ALL:
                    atomNames.addAll(Arrays.asList(atoms));
                    if (residueType == right.getResidueType()) {
                        atoms = residueType.getResidueAtoms();
                        atomNames.addAll(Arrays.asList(atoms));
                    }
                    break;
                case BACKBONE:
                    atomNames.addAll(Arrays.asList(atoms));
                    break;
                case MAIN:
                    atomNames.add(chainType.getMainAtom());
                    break;
                default:
                    break;
                }

                for (AtomName name : atomNames) {
                    if (onlyHeavy && !name.getType().isHeavy()) {
                        continue;
                    }

                    Atom l = StructureHelper.findAtom(left.getGroup(), name);
                    Atom r = StructureHelper.findAtom(right.getGroup(), name);

                    if (l != null && r != null) {
                        atomsL.add(l);
                        atomsR.add(r);
                    }
                }
            }
        }

        Atom[] atomSetL = atomsL.toArray(new Atom[atomsL.size()]);
        Atom[] atomSetR = atomsR.toArray(new Atom[atomsR.size()]);
        List<Atom> clones = new ArrayList<>();

        try {
            SVDSuperimposer superimposer = new SVDSuperimposer(atomSetL,
                    atomSetR);

            /*
             * Rotate and shift atoms needed for RMSD calculation
             */
            for (Atom atom : atomsR) {
                Atom r = (Atom) atom.clone();
                Calc.rotate(r, superimposer.getRotation());
                Calc.shift(r, superimposer.getTranslation());
                clones.add(r);
            }

            /*
             * Rotate and shift ALL atoms in the fragment
             */
            List<CompactFragment> newFragmentsL = new ArrayList<>();
            List<CompactFragment> newFragmentsR = new ArrayList<>();

            for (int i = 0; i < selectionMatch.getSize(); i++) {
                FragmentMatch fragmentMatch = selectionMatch.getFragmentMatch(i);
                FragmentComparison fragmentComparison = fragmentMatch.getBestResult();

                MoleculeType moleculeType = fragmentMatch.getSmaller().getMoleculeType();
                CompactFragment fragmentL = new CompactFragment(
                        selectionMatch.getParentLeft(), moleculeType);
                CompactFragment fragmentR = new CompactFragment(
                        selectionMatch.getParentRight(), moleculeType);

                for (ResidueComparison residueComparison : fragmentComparison) {
                    ResidueAngles left = residueComparison.getLeft();
                    fragmentL.addGroup(left.getGroup());

                    ResidueAngles right = residueComparison.getRight();
                    Group group = right.getGroup();
                    List<Atom> fragmentClones = new ArrayList<>();

                    for (Atom atom : group.getAtoms()) {
                        Atom r = (Atom) atom.clone();
                        Calc.rotate(r, superimposer.getRotation());
                        Calc.shift(r, superimposer.getTranslation());
                        fragmentClones.add(r);
                    }

                    Group groupClone = (Group) group.clone();
                    groupClone.setAtoms(fragmentClones);
                    fragmentR.addGroup(groupClone);
                }

                newFragmentsL.add(fragmentL);
                newFragmentsR.add(fragmentR);
            }

            Atom[] array = clones.toArray(new Atom[clones.size()]);
            double rmsd = SVDSuperimposer.getRMS(atomSetL, array);
            return new FragmentSuperposition(newFragmentsL, newFragmentsR, rmsd);
        } catch (StructureException e) {
            throw new IncomparableStructuresException(
                    "Failed to superimpose structures and calculate RMSD", e);
        }
    }
}
