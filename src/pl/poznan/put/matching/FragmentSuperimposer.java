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
import pl.poznan.put.helper.StructureHelper;
import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.ResidueAngles;
import pl.poznan.put.structure.StructureSelection;

public class FragmentSuperimposer {
    public enum AtomFilter {
        ALL, BACKBONE, MAIN
    }

    private final SelectionMatch selectionMatch;
    private final AtomFilter atomFilter;
    private final boolean onlyHeavy;
    private final SVDSuperimposer svdSuperimposer;
    private final Atom[] atomsTarget;
    private final Atom[] atomsModel;

    public FragmentSuperimposer(SelectionMatch selectionMatch,
            AtomFilter atomFilter, boolean onlyHeavy) throws StructureException {
        super();
        this.selectionMatch = selectionMatch;
        this.atomFilter = atomFilter;
        this.onlyHeavy = onlyHeavy;

        List<Atom> atomsT = new ArrayList<>();
        List<Atom> atomsM = new ArrayList<>();
        filterAtoms(atomsT, atomsM);

        atomsTarget = atomsT.toArray(new Atom[atomsT.size()]);
        atomsModel = atomsM.toArray(new Atom[atomsM.size()]);
        svdSuperimposer = new SVDSuperimposer(atomsTarget, atomsModel);
    }

    private void filterAtoms(List<Atom> atomsT, List<Atom> atomsM) {
        for (int i = 0; i < selectionMatch.getSize(); i++) {
            FragmentMatch fragment = selectionMatch.getFragmentMatch(i);
            FragmentComparison fragmentComparison = fragment.getFragmentComparison();

            for (ResidueComparison residueComparison : fragmentComparison) {
                ResidueAngles targetAngles = residueComparison.getTargetAngles();
                ResidueAngles modelAngles = residueComparison.getModelAngles();
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

                    Atom l = StructureHelper.findAtom(targetAngles.getGroup(),
                            name);
                    Atom r = StructureHelper.findAtom(modelAngles.getGroup(),
                            name);

                    if (l != null && r != null) {
                        atomsT.add(l);
                        atomsM.add(r);
                    }
                }
            }
        }
    }

    public double getRMSD() throws StructureException {
        List<Atom> clones = new ArrayList<>();

        for (Atom atom : atomsModel) {
            Atom r = (Atom) atom.clone();
            Calc.rotate(r, svdSuperimposer.getRotation());
            Calc.shift(r, svdSuperimposer.getTranslation());
            clones.add(r);
        }

        return SVDSuperimposer.getRMS(atomsTarget, atomsModel);
    }

    public FragmentSuperposition getWholeSelections() {
        StructureSelection target = selectionMatch.getTarget();
        StructureSelection model = selectionMatch.getModel();
        List<CompactFragment> targetFragments = Arrays.asList(target.getCompactFragments());
        List<CompactFragment> modelFragments = new ArrayList<>();

        for (CompactFragment fragment : model.getCompactFragments()) {
            CompactFragment modifiedFragment = new CompactFragment(model,
                    fragment.getMoleculeType());

            for (int i = 0; i < fragment.getSize(); i++) {
                Group group = fragment.getGroup(i);
                List<Atom> fragmentClones = new ArrayList<>();

                for (Atom atom : group.getAtoms()) {
                    Atom r = (Atom) atom.clone();
                    Calc.rotate(r, svdSuperimposer.getRotation());
                    Calc.shift(r, svdSuperimposer.getTranslation());
                    fragmentClones.add(r);
                }

                Group groupClone = (Group) group.clone();
                groupClone.setAtoms(fragmentClones);
                modifiedFragment.addGroup(groupClone);
            }

            modelFragments.add(modifiedFragment);
        }

        return new FragmentSuperposition(targetFragments, modelFragments);
    }

    public FragmentSuperposition getOnlyMatched() {
        List<CompactFragment> newFragmentsL = new ArrayList<>();
        List<CompactFragment> newFragmentsR = new ArrayList<>();

        for (int i = 0; i < selectionMatch.getSize(); i++) {
            FragmentMatch fragmentMatch = selectionMatch.getFragmentMatch(i);
            FragmentComparison fragmentComparison = fragmentMatch.getFragmentComparison();

            MoleculeType moleculeType = fragmentMatch.getMoleculeType();
            CompactFragment fragmentL = new CompactFragment(
                    selectionMatch.getTarget(), moleculeType);
            CompactFragment fragmentR = new CompactFragment(
                    selectionMatch.getModel(), moleculeType);

            for (ResidueComparison residueComparison : fragmentComparison) {
                ResidueAngles targetAngles = residueComparison.getTargetAngles();
                fragmentL.addGroup(targetAngles.getGroup());

                ResidueAngles modelAngles = residueComparison.getModelAngles();
                Group group = modelAngles.getGroup();
                List<Atom> fragmentClones = new ArrayList<>();

                for (Atom atom : group.getAtoms()) {
                    Atom r = (Atom) atom.clone();
                    Calc.rotate(r, svdSuperimposer.getRotation());
                    Calc.shift(r, svdSuperimposer.getTranslation());
                    fragmentClones.add(r);
                }

                Group groupClone = (Group) group.clone();
                groupClone.setAtoms(fragmentClones);
                fragmentR.addGroup(groupClone);
            }

            newFragmentsL.add(fragmentL);
            newFragmentsR.add(fragmentR);
        }

        return new FragmentSuperposition(newFragmentsL, newFragmentsR);
    }
}
