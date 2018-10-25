package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.biojava.nbio.structure.geometry.CalcPoint;
import org.biojava.nbio.structure.geometry.SuperPositions;
import pl.poznan.put.atom.AtomName;
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

public class FragmentSuperimposer {

  public enum AtomFilter {
    ALL,
    BACKBONE,
    MAIN
  }

  private final SelectionMatch selectionMatch;
  private final AtomFilter atomFilter;
  private final boolean onlyHeavy;

  private final Matrix4d totalSuperposition;
  private final Point3d[] totalAtomsTarget;
  private final Point3d[] totalAtomsModel;
  private final Matrix4d[] matchedSuperpositions;

  public FragmentSuperimposer(
      final SelectionMatch selectionMatch, final AtomFilter atomFilter, final boolean onlyHeavy) {
    super();
    this.selectionMatch = selectionMatch;
    this.atomFilter = atomFilter;
    this.onlyHeavy = onlyHeavy;

    final int matchesCount = selectionMatch.getFragmentMatches().size();
    if (matchesCount == 0) {
      throw new IllegalArgumentException(
          "Failed to superimpose, because the set of structural matches is empty");
    }

    matchedSuperpositions = new Matrix4d[matchesCount];

    final List<Point3d> atomsT = new ArrayList<>();
    final List<Point3d> atomsM = new ArrayList<>();
    filterAtoms(atomsT, atomsM);

    totalAtomsTarget = atomsT.toArray(new Point3d[atomsT.size()]);
    totalAtomsModel = atomsM.toArray(new Point3d[atomsM.size()]);

    totalSuperposition = SuperPositions.superposeAndTransform(totalAtomsTarget, totalAtomsModel);
  }

  private void filterAtoms(
      final Collection<Point3d> atomsTargetAll, final Collection<Point3d> atomsModelAll) {
    final List<FragmentMatch> fragmentMatches = selectionMatch.getFragmentMatches();

    for (int i = 0, size = fragmentMatches.size(); i < size; i++) {
      final FragmentMatch fragment = fragmentMatches.get(i);
      final Collection<Point3d> atomsTargetMatch = new ArrayList<>();
      final Collection<Point3d> atomsModelMatch = new ArrayList<>();

      for (final ResidueComparison residueComparison : fragment.getResidueComparisons()) {
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

          // call PdbAtomLine.toPoint3d twice, because each time a
          // new Point3d object is created and this is required
          final PdbAtomLine atomTarget = target.findAtom(name);
          atomsTargetMatch.add(atomTarget.toPoint3d());
          atomsTargetAll.add(atomTarget.toPoint3d());

          // the same as above
          final PdbAtomLine atomModel = model.findAtom(name);
          atomsModelMatch.add(atomModel.toPoint3d());
          atomsModelAll.add(atomModel.toPoint3d());
        }
      }

      final Point3d[] matchedAtomsTarget =
          atomsTargetMatch.toArray(new Point3d[atomsTargetMatch.size()]);
      final Point3d[] matchedAtomsModel =
          atomsModelMatch.toArray(new Point3d[atomsModelMatch.size()]);
      matchedSuperpositions[i] =
          SuperPositions.superposeAndTransform(matchedAtomsTarget, matchedAtomsModel);
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
        for (final AminoAcidType aminoAcidType : AminoAcidType.values()) {
          for (final ResidueComponent component : aminoAcidType.getAllMoleculeComponents()) {
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
        for (final NucleobaseType nucleobaseType : NucleobaseType.values()) {
          for (final ResidueComponent component : nucleobaseType.getAllMoleculeComponents()) {
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
    return CalcPoint.rmsd(totalAtomsTarget, totalAtomsModel);
  }

  public final FragmentSuperposition getWhole() {
    final StructureSelection target = selectionMatch.getTarget();
    final StructureSelection model = selectionMatch.getModel();
    final List<PdbCompactFragment> targetFragments = target.getCompactFragments();
    final List<PdbCompactFragment> modelFragments = new ArrayList<>();

    for (final PdbCompactFragment fragment : model.getCompactFragments()) {
      final List<PdbResidue> modifiedResidues = new ArrayList<>();

      for (final PdbResidue residue : fragment.getResidues()) {
        final List<PdbAtomLine> atoms = residue.getAtoms();
        final Point3d[] points = new Point3d[atoms.size()];

        for (int i = 0, size = atoms.size(); i < size; i++) {
          final PdbAtomLine atom = atoms.get(i);
          points[i] = new Point3d(atom.getX(), atom.getY(), atom.getZ());
        }

        CalcPoint.transform(totalSuperposition, points);
        final List<PdbAtomLine> modifiedAtoms = new ArrayList<>();

        for (int i = 0, size = atoms.size(); i < size; i++) {
          final PdbAtomLine atom = atoms.get(i);
          modifiedAtoms.add(atom.replaceCoordinates(points[i].x, points[i].y, points[i].z));
        }

        final PdbResidueIdentifier identifier = residue.getResidueIdentifier();
        final String residueName = residue.getDetectedResidueName();
        final PdbResidue modifiedResidue =
            new PdbResidue(identifier, residueName, modifiedAtoms, false);
        modifiedResidues.add(modifiedResidue);
      }

      modelFragments.add(new PdbCompactFragment(fragment.getName(), modifiedResidues));
    }

    return new FragmentSuperposition(targetFragments, modelFragments);
  }

  public final FragmentSuperposition getMatched() {
    final List<PdbCompactFragment> newFragmentsL = new ArrayList<>();
    final List<PdbCompactFragment> newFragmentsR = new ArrayList<>();
    final List<FragmentMatch> fragmentMatches = selectionMatch.getFragmentMatches();

    for (int j = 0, size = fragmentMatches.size(); j < size; j++) {
      final FragmentMatch fragmentMatch = fragmentMatches.get(j);
      final List<PdbResidue> matchedModelResiduesModified = new ArrayList<>();
      final List<PdbResidue> matchedTargetResidues = new ArrayList<>();

      for (final ResidueComparison residueComparison : fragmentMatch.getResidueComparisons()) {
        matchedTargetResidues.add(residueComparison.getTarget());

        final PdbResidue model = residueComparison.getModel();
        final List<PdbAtomLine> atoms = model.getAtoms();
        final Point3d[] points = new Point3d[atoms.size()];

        for (int i = 0; i < atoms.size(); i++) {
          final PdbAtomLine atom = atoms.get(i);
          points[i] = new Point3d(atom.getX(), atom.getY(), atom.getZ());
        }

        CalcPoint.transform(matchedSuperpositions[j], points);
        final List<PdbAtomLine> modifiedAtoms = new ArrayList<>();

        for (int i = 0; i < atoms.size(); i++) {
          final PdbAtomLine atom = atoms.get(i);
          modifiedAtoms.add(atom.replaceCoordinates(points[i].x, points[i].y, points[i].z));
        }

        final PdbResidueIdentifier identifier = model.getResidueIdentifier();
        final String residueName = model.getDetectedResidueName();
        matchedModelResiduesModified.add(
            new PdbResidue(identifier, residueName, modifiedAtoms, false));
      }

      newFragmentsL.add(
          new PdbCompactFragment(
              fragmentMatch.getModelFragment().getName(), matchedTargetResidues));
      newFragmentsR.add(
          new PdbCompactFragment(
              fragmentMatch.getTargetFragment().getName(), matchedModelResiduesModified));
    }

    return new FragmentSuperposition(newFragmentsL, newFragmentsR);
  }
}
