package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.SVDSuperimposer;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.atom.AtomName;
import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.structure.tertiary.StructureHelper;

public class FragmentSuperimposer {
    public enum AtomFilter {
        ALL, BACKBONE, MAIN
    }

    private final SelectionMatch selectionMatch;
    private final AtomFilter atomFilter;
    private final boolean onlyHeavy;
    private final SVDSuperimposer[] matchSuperimposer;
    private final Atom[][] matchAtomsTarget;
    private final Atom[][] matchAtomsModel;
    private final SVDSuperimposer totalSuperimposer;
    private final Atom[] totalAtomsTarget;
    private final Atom[] totalAtomsModel;

    public FragmentSuperimposer(SelectionMatch selectionMatch,
            AtomFilter atomFilter, boolean onlyHeavy) throws StructureException {
        super();
        this.selectionMatch = selectionMatch;
        this.atomFilter = atomFilter;
        this.onlyHeavy = onlyHeavy;

        int matchesCount = selectionMatch.getSize();
        matchSuperimposer = new SVDSuperimposer[matchesCount];
        matchAtomsTarget = new Atom[matchesCount][];
        matchAtomsModel = new Atom[matchesCount][];
        List<Atom> atomsT = new ArrayList<>();
        List<Atom> atomsM = new ArrayList<>();
        filterAtoms(atomsT, atomsM);

        totalAtomsTarget = atomsT.toArray(new Atom[atomsT.size()]);
        totalAtomsModel = atomsM.toArray(new Atom[atomsM.size()]);
        totalSuperimposer = new SVDSuperimposer(totalAtomsTarget, totalAtomsModel);
    }

    private void filterAtoms(List<Atom> atomsT, List<Atom> atomsM) throws StructureException {
        for (int i = 0; i < selectionMatch.getSize(); i++) {
            FragmentMatch fragment = selectionMatch.getFragmentMatch(i);
            FragmentComparison fragmentComparison = fragment.getFragmentComparison();
            List<Atom> atomsTarget = new ArrayList<>();
            List<Atom> atomsModel = new ArrayList<>();

            for (ResidueComparison residueComparison : fragmentComparison) {
                PdbResidueTorsionAngleValues targetAngles = residueComparison.getTargetAngles();
                PdbResidueTorsionAngleValues modelAngles = residueComparison.getModelAngles();
                ResidueType residueType = targetAngles.getResidueType();
                MoleculeType chainType = residueType.getChainType();
                AtomName[] atoms = chainType.getBackboneAtoms();
                List<AtomName> atomNames = new ArrayList<>();

                switch (atomFilter) {
                case ALL:
                    atomNames.addAll(Arrays.asList(atoms));
                    if (residueType == modelAngles.getResidueType()) {
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

                    Atom l = StructureHelper.findAtom(targetAngles.getGroup(), name);
                    Atom r = StructureHelper.findAtom(modelAngles.getGroup(), name);

                    if (l != null && r != null) {
                        atomsTarget.add(l);
                        atomsModel.add(r);
                    }
                }
            }

            atomsT.addAll(atomsTarget);
            atomsM.addAll(atomsModel);

            matchAtomsTarget[i] = atomsTarget.toArray(new Atom[atomsTarget.size()]);
            matchAtomsModel[i] = atomsModel.toArray(new Atom[atomsModel.size()]);
            matchSuperimposer[i] = new SVDSuperimposer(matchAtomsTarget[i], matchAtomsModel[i]);
        }
    }

    public double getRMSD() throws StructureException {
        double distance = 0.0;
        double count = 0.0;

        for (int i = 0; i < selectionMatch.getSize(); i++) {
            for (int j = 0; j < matchAtomsModel[i].length; j++) {
                Atom l = matchAtomsTarget[i][j];
                Atom r = (Atom) matchAtomsModel[i][j].clone();
                Calc.rotate(r, matchSuperimposer[i].getRotation());
                Calc.shift(r, matchSuperimposer[i].getTranslation());
                distance += Calc.getDistanceFast(l, r);
                count += 1.0;
            }
        }

        return Math.sqrt(distance / count);
    }

    public FragmentSuperposition getWhole() {
        StructureSelection target = selectionMatch.getTarget();
        StructureSelection model = selectionMatch.getModel();
        List<PdbCompactFragment> targetFragments = target.getCompactFragments();
        List<PdbCompactFragment> modelFragments = new ArrayList<>();

        for (PdbCompactFragment fragment : model.getCompactFragments()) {
            PdbCompactFragment modifiedFragment = new PdbCompactFragment(model, fragment.moleculeType());

            for (int i = 0; i < fragment.size(); i++) {
                Group group = fragment.getGroup(i);
                List<Atom> fragmentClones = new ArrayList<>();

                for (Atom atom : group.getAtoms()) {
                    Atom r = (Atom) atom.clone();
                    Calc.rotate(r, totalSuperimposer.getRotation());
                    Calc.shift(r, totalSuperimposer.getTranslation());
                    fragmentClones.add(r);
                }

                Group groupClone = (Group) group.clone();
                groupClone.setAtoms(fragmentClones);
                modifiedFragment.addResidue(groupClone);
            }

            modelFragments.add(modifiedFragment);
        }

        return new FragmentSuperposition(targetFragments, modelFragments);
    }

    public FragmentSuperposition getMatched() {
        List<PdbCompactFragment> newFragmentsL = new ArrayList<>();
        List<PdbCompactFragment> newFragmentsR = new ArrayList<>();

        for (int i = 0; i < selectionMatch.getSize(); i++) {
            FragmentMatch fragmentMatch = selectionMatch.getFragmentMatch(i);
            FragmentComparison fragmentComparison = fragmentMatch.getFragmentComparison();

            MoleculeType moleculeType = fragmentMatch.moleculeType();
            PdbCompactFragment fragmentL = new PdbCompactFragment(selectionMatch.getTarget(), moleculeType);
            PdbCompactFragment fragmentR = new PdbCompactFragment(selectionMatch.getModel(), moleculeType);

            for (ResidueComparison residueComparison : fragmentComparison) {
                PdbResidueTorsionAngleValues targetAngles = residueComparison.getTargetAngles();
                fragmentL.addResidue(targetAngles.getGroup());

                PdbResidueTorsionAngleValues modelAngles = residueComparison.getModelAngles();
                Group group = modelAngles.getGroup();
                List<Atom> fragmentClones = new ArrayList<>();

                for (Atom atom : group.getAtoms()) {
                    Atom r = (Atom) atom.clone();
                    Calc.rotate(r, matchSuperimposer[i].getRotation());
                    Calc.shift(r, matchSuperimposer[i].getTranslation());
                    fragmentClones.add(r);
                }

                Group groupClone = (Group) group.clone();
                groupClone.setAtoms(fragmentClones);
                fragmentR.addResidue(groupClone);
            }

            newFragmentsL.add(fragmentL);
            newFragmentsR.add(fragmentR);
        }

        return new FragmentSuperposition(newFragmentsL, newFragmentsR);
    }
}
