package pl.poznan.put.matching;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.SVDSuperimposer;
import org.biojava.nbio.structure.StructureException;
import pl.poznan.put.atom.AtomName;
import pl.poznan.put.pdb.CifPdbIncompatibilityException;
import pl.poznan.put.pdb.PdbAtomLine;
import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.pdb.analysis.ResidueComponent;
import pl.poznan.put.protein.ProteinBackbone;
import pl.poznan.put.protein.aminoacid.AminoAcidType;
import pl.poznan.put.rna.Phosphate;
import pl.poznan.put.rna.Ribose;
import pl.poznan.put.rna.base.NucleobaseType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FragmentSuperimposer {
    private final SelectionMatch selectionMatch;
    private final AtomFilter atomFilter;
    private final boolean onlyHeavy;
    private final SVDSuperimposer[] matchSuperimposer;
    private final Atom[][] matchAtomsTarget;
    private final Atom[][] matchAtomsModel;
    private final SVDSuperimposer totalSuperimposer;
    private final Atom[] totalAtomsTarget;
    private final Atom[] totalAtomsModel;

    public FragmentSuperimposer(final SelectionMatch selectionMatch,
                                final AtomFilter atomFilter,
                                final boolean onlyHeavy)
            throws StructureException, CifPdbIncompatibilityException {
        super();
        this.selectionMatch = selectionMatch;
        this.atomFilter = atomFilter;
        this.onlyHeavy = onlyHeavy;

        final int matchesCount = selectionMatch.getFragmentMatches().size();
        if (matchesCount == 0) {
            throw new IllegalArgumentException(
                    "Failed to superimpose, because the set of structural " +
                    "matches is empty");
        }

        matchSuperimposer = new SVDSuperimposer[matchesCount];
        matchAtomsTarget = new Atom[matchesCount][];
        matchAtomsModel = new Atom[matchesCount][];
        final List<Atom> atomsT = new ArrayList<>();
        final List<Atom> atomsM = new ArrayList<>();
        filterAtoms(atomsT, atomsM);

        totalAtomsTarget = atomsT.toArray(new Atom[atomsT.size()]);
        totalAtomsModel = atomsM.toArray(new Atom[atomsM.size()]);
        totalSuperimposer =
                new SVDSuperimposer(totalAtomsTarget, totalAtomsModel);
    }

    private void filterAtoms(final Collection<Atom> atomsT,
                             final Collection<Atom> atomsM)
            throws StructureException, CifPdbIncompatibilityException {
        int i = 0;

        for (final FragmentMatch fragment : selectionMatch
                .getFragmentMatches()) {
            final List<Atom> atomsTarget = new ArrayList<>();
            final List<Atom> atomsModel = new ArrayList<>();

            for (final ResidueComparison residueComparison : fragment
                    .getResidueComparisons()) {
                final PdbResidue target = residueComparison.getTarget();
                final PdbResidue model = residueComparison.getModel();
                final MoleculeType moleculeType = target.getMoleculeType();
                final List<AtomName> atomNames = handleAtomFilter(moleculeType);

                for (final AtomName name : atomNames) {
                    if (onlyHeavy && !name.getType().isHeavy()) {
                        continue;
                    }

                    if (!target.hasAtom(name) || !model.hasAtom(name)) {
                        continue;
                    }

                    atomsTarget.add(target.findAtom(name).toBioJavaAtom());
                    atomsModel.add(model.findAtom(name).toBioJavaAtom());
                }
            }

            atomsT.addAll(atomsTarget);
            atomsM.addAll(atomsModel);

            matchAtomsTarget[i] =
                    atomsTarget.toArray(new Atom[atomsTarget.size()]);
            matchAtomsModel[i] =
                    atomsModel.toArray(new Atom[atomsModel.size()]);
            matchSuperimposer[i] = new SVDSuperimposer(matchAtomsTarget[i],
                                                       matchAtomsModel[i]);
            i += 1;
        }
    }

    private List<AtomName> handleAtomFilter(final MoleculeType moleculeType) {
        switch (moleculeType) {
            case PROTEIN:
                return handleAtomFilterForProtein();
            case RNA:
                return handleAtomFilterForRNA();
            case UNKNOWN:
            default:
                return Collections.emptyList();
        }
    }

    private List<AtomName> handleAtomFilterForProtein() {
        switch (atomFilter) {
            case ALL:
                final Set<AtomName> atomNames = new HashSet<>();
                for (final AminoAcidType aminoAcidType : AminoAcidType
                        .values()) {
                    for (final ResidueComponent component : aminoAcidType
                            .getAllMoleculeComponents()) {
                        atomNames.addAll(component.getAtoms());
                    }
                }
                return new ArrayList<>(atomNames);
            case BACKBONE:
                return ProteinBackbone.getInstance().getAtoms();
            case MAIN:
                return Collections.singletonList(AtomName.C);
            default:
                return Collections.emptyList();
        }
    }

    private List<AtomName> handleAtomFilterForRNA() {
        final Set<AtomName> atomNames = new HashSet<>();

        switch (atomFilter) {
            case ALL:
                for (final NucleobaseType nucleobaseType : NucleobaseType
                        .values()) {
                    for (final ResidueComponent component : nucleobaseType
                            .getAllMoleculeComponents()) {
                        atomNames.addAll(component.getAtoms());
                    }
                }
                return new ArrayList<>(atomNames);
            case BACKBONE:
                atomNames.addAll(Phosphate.getInstance().getAtoms());
                atomNames.addAll(Ribose.getInstance().getAtoms());
                return new ArrayList<>(atomNames);
            case MAIN:
                return Collections.singletonList(AtomName.P);
            default:
                return Collections.emptyList();
        }
    }

    public final int getAtomCount() {
        assert totalAtomsTarget.length == totalAtomsModel.length;
        return totalAtomsTarget.length;
    }

    public final double getRMSD() {
        double distance = 0.0;
        double count = 0.0;

        for (int i = 0; i < selectionMatch.getFragmentMatches().size(); i++) {
            for (int j = 0; j < matchAtomsModel[i].length; j++) {
                final Atom left = matchAtomsTarget[i][j];
                final Atom right = (Atom) matchAtomsModel[i][j].clone();
                Calc.rotate(right, matchSuperimposer[i].getRotation());
                Calc.shift(right, matchSuperimposer[i].getTranslation());

                final Vector3D vl =
                        new Vector3D(left.getX(), left.getY(), left.getZ());
                final Vector3D vr =
                        new Vector3D(right.getX(), right.getY(), right.getZ());
                distance += vl.distance(vr);
                count += 1.0;
            }
        }

        return Math.sqrt(distance / count);
    }

    public final FragmentSuperposition getWhole()
            throws CifPdbIncompatibilityException {
        final StructureSelection target = selectionMatch.getTarget();
        final StructureSelection model = selectionMatch.getModel();
        final List<PdbCompactFragment> targetFragments =
                target.getCompactFragments();
        final List<PdbCompactFragment> modelFragments = new ArrayList<>();

        for (final PdbCompactFragment fragment : model.getCompactFragments()) {
            final List<PdbResidue> modifiedResidues = new ArrayList<>();

            for (final PdbResidue residue : fragment.getResidues()) {
                final List<PdbAtomLine> modifiedAtoms = new ArrayList<>();

                for (final PdbAtomLine atom : residue.getAtoms()) {
                    final Atom bioJavaAtom = atom.toBioJavaAtom();
                    Calc.rotate(bioJavaAtom, totalSuperimposer.getRotation());
                    Calc.shift(bioJavaAtom, totalSuperimposer.getTranslation());
                    modifiedAtoms.add(PdbAtomLine.fromBioJavaAtom(bioJavaAtom));
                }

                final PdbResidueIdentifier identifier =
                        residue.getResidueIdentifier();
                final String residueName = residue.getDetectedResidueName();
                modifiedResidues.add(new PdbResidue(identifier, residueName,
                                                    modifiedAtoms, false));
            }

            modelFragments.add(new PdbCompactFragment(fragment.getName(),
                                                      modifiedResidues));
        }

        return new FragmentSuperposition(targetFragments, modelFragments);
    }

    public final FragmentSuperposition getMatched()
            throws CifPdbIncompatibilityException {
        final List<PdbCompactFragment> newFragmentsL = new ArrayList<>();
        final List<PdbCompactFragment> newFragmentsR = new ArrayList<>();

        for (final FragmentMatch fragmentMatch : selectionMatch
                .getFragmentMatches()) {
            final List<PdbResidue> matchedModelResiduesModified =
                    new ArrayList<>();
            final List<PdbResidue> matchedTargetResidues = new ArrayList<>();

            for (final ResidueComparison residueComparison : fragmentMatch
                    .getResidueComparisons()) {
                matchedTargetResidues.add(residueComparison.getTarget());

                final PdbResidue model = residueComparison.getModel();
                final List<PdbAtomLine> modifiedAtoms = new ArrayList<>();

                for (final PdbAtomLine atom : model.getAtoms()) {
                    final Atom bioJavaAtom = atom.toBioJavaAtom();
                    Calc.rotate(bioJavaAtom, totalSuperimposer.getRotation());
                    Calc.shift(bioJavaAtom, totalSuperimposer.getTranslation());
                    modifiedAtoms.add(PdbAtomLine.fromBioJavaAtom(bioJavaAtom));
                }

                final PdbResidueIdentifier identifier =
                        model.getResidueIdentifier();
                final String residueName = model.getDetectedResidueName();
                matchedModelResiduesModified
                        .add(new PdbResidue(identifier, residueName,
                                            modifiedAtoms, false));
            }

            newFragmentsL.add(new PdbCompactFragment(
                    fragmentMatch.getModelFragment().getName(),
                    matchedTargetResidues));
            newFragmentsR.add(new PdbCompactFragment(
                    fragmentMatch.getTargetFragment().getName(),
                    matchedModelResiduesModified));
        }

        return new FragmentSuperposition(newFragmentsL, newFragmentsR);
    }

    public enum AtomFilter {
        ALL,
        BACKBONE,
        MAIN
    }
}
