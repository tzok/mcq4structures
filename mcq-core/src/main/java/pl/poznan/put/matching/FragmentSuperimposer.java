package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.biojava.nbio.structure.geometry.CalcPoint;
import org.biojava.nbio.structure.geometry.SuperPositions;
import pl.poznan.put.atom.AtomName;
import pl.poznan.put.pdb.ImmutablePdbAtomLine;
import pl.poznan.put.pdb.PdbAtomLine;
import pl.poznan.put.pdb.analysis.ImmutableDefaultPdbResidue;
import pl.poznan.put.pdb.analysis.ImmutablePdbCompactFragment;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.pdb.analysis.ResidueComponent;
import pl.poznan.put.protein.AminoAcid;
import pl.poznan.put.protein.ImmutableBackbone;
import pl.poznan.put.rna.ImmutablePhosphate;
import pl.poznan.put.rna.ImmutableRibose;
import pl.poznan.put.rna.Nucleotide;

public class FragmentSuperimposer {

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

    totalAtomsTarget = atomsT.toArray(new Point3d[0]);
    totalAtomsModel = atomsM.toArray(new Point3d[0]);

    totalSuperposition = SuperPositions.superposeAndTransform(totalAtomsTarget, totalAtomsModel);
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

      for (final PdbResidue residue : fragment.residues()) {
        final List<PdbAtomLine> atoms = residue.atoms();
        final Point3d[] points = new Point3d[atoms.size()];

        for (int i = 0, size = atoms.size(); i < size; i++) {
          final PdbAtomLine atom = atoms.get(i);
          points[i] = new Point3d(atom.x(), atom.y(), atom.z());
        }

        CalcPoint.transform(totalSuperposition, points);
        final List<PdbAtomLine> modifiedAtoms = new ArrayList<>();

        for (int i = 0, size = atoms.size(); i < size; i++) {
          final PdbAtomLine atom = atoms.get(i);
          modifiedAtoms.add(
              ImmutablePdbAtomLine.copyOf(atom)
                  .withX(points[i].x)
                  .withY(points[i].y)
                  .withZ(points[i].z));
        }

        final PdbResidue modifiedResidue =
            ImmutableDefaultPdbResidue.of(
                residue.identifier(),
                residue.standardResidueName(),
                residue.modifiedResidueName(),
                modifiedAtoms);
        modifiedResidues.add(modifiedResidue);
      }

      modelFragments.add(
          ImmutablePdbCompactFragment.of(modifiedResidues).withName(fragment.name()));
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
        matchedTargetResidues.add(residueComparison.target());

        final PdbResidue model = residueComparison.model();
        final List<PdbAtomLine> atoms = model.atoms();
        final Point3d[] points = new Point3d[atoms.size()];

        for (int i = 0; i < atoms.size(); i++) {
          final PdbAtomLine atom = atoms.get(i);
          points[i] = new Point3d(atom.x(), atom.y(), atom.z());
        }

        CalcPoint.transform(matchedSuperpositions[j], points);
        final List<PdbAtomLine> modifiedAtoms = new ArrayList<>();

        for (int i = 0; i < atoms.size(); i++) {
          final PdbAtomLine atom = atoms.get(i);
          modifiedAtoms.add(
              ImmutablePdbAtomLine.copyOf(atom)
                  .withX(points[i].x)
                  .withY(points[i].y)
                  .withZ(points[i].z));
        }

        matchedModelResiduesModified.add(
            ImmutableDefaultPdbResidue.of(
                model.identifier(),
                model.standardResidueName(),
                model.modifiedResidueName(),
                modifiedAtoms));
      }

      newFragmentsL.add(
          ImmutablePdbCompactFragment.of(matchedTargetResidues)
              .withName(fragmentMatch.getModelFragment().name()));
      newFragmentsR.add(
          ImmutablePdbCompactFragment.of(matchedModelResiduesModified)
              .withName(fragmentMatch.getTargetFragment().name()));
    }

    return new FragmentSuperposition(newFragmentsL, newFragmentsR);
  }

  private void filterAtoms(
      final Collection<? super Point3d> atomsTargetAll,
      final Collection<? super Point3d> atomsModelAll) {
    final List<FragmentMatch> fragmentMatches = selectionMatch.getFragmentMatches();

    for (int i = 0, size = fragmentMatches.size(); i < size; i++) {
      final FragmentMatch fragment = fragmentMatches.get(i);
      final Collection<Point3d> atomsTargetMatch = new ArrayList<>();
      final Collection<Point3d> atomsModelMatch = new ArrayList<>();

      for (final ResidueComparison residueComparison : fragment.getResidueComparisons()) {
        final PdbResidue target = residueComparison.target();
        final PdbResidue model = residueComparison.model();
        final MoleculeType moleculeType = target.residueInformationProvider().moleculeType();
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
          atomsTargetMatch.add(new Point3d(atomTarget.x(), atomTarget.y(), atomTarget.z()));
          atomsTargetAll.add(new Point3d(atomTarget.x(), atomTarget.y(), atomTarget.z()));

          // the same as above
          final PdbAtomLine atomModel = model.findAtom(name);
          atomsModelMatch.add(new Point3d(atomModel.x(), atomModel.y(), atomModel.z()));
          atomsModelAll.add(new Point3d(atomModel.x(), atomModel.y(), atomModel.z()));
        }
      }

      final Point3d[] matchedAtomsTarget = atomsTargetMatch.toArray(new Point3d[0]);
      final Point3d[] matchedAtomsModel = atomsModelMatch.toArray(new Point3d[0]);
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
        final Set<AtomName> atomNames = EnumSet.noneOf(AtomName.class);
        for (final AminoAcid aminoAcidType : AminoAcid.values()) {
          for (final ResidueComponent component : aminoAcidType.moleculeComponents()) {
            atomNames.addAll(component.requiredAtoms());
          }
        }
        return new ArrayList<>(atomNames);
      case BACKBONE:
        return new ArrayList<>(ImmutableBackbone.of().requiredAtoms());
      case MAIN:
        return Collections.singletonList(AtomName.C);
      default:
        return Collections.emptyList();
    }
  }

  private List<AtomName> handleAtomFilterForRNA() {
    final Set<AtomName> atomNames = EnumSet.noneOf(AtomName.class);

    switch (atomFilter) {
      case ALL:
        for (final Nucleotide nucleobaseType : Nucleotide.values()) {
          for (final ResidueComponent component : nucleobaseType.moleculeComponents()) {
            atomNames.addAll(component.requiredAtoms());
          }
        }
        return new ArrayList<>(atomNames);
      case BACKBONE:
        atomNames.addAll(ImmutablePhosphate.of().requiredAtoms());
        atomNames.addAll(ImmutableRibose.of().requiredAtoms());
        return new ArrayList<>(atomNames);
      case MAIN:
        return Collections.singletonList(AtomName.P);
      default:
        return Collections.emptyList();
    }
  }

  public enum AtomFilter {
    ALL,
    BACKBONE,
    MAIN
  }
}
