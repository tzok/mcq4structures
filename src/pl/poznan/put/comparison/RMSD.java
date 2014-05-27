package pl.poznan.put.comparison;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.SVDSuperimposer;
import org.biojava.bio.structure.StructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.atoms.AtomName;
import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.common.ResidueType;
import pl.poznan.put.helper.StructureHelper;
import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MCQMatcher;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.structure.ResidueAngles;
import pl.poznan.put.structure.StructureSelection;

/**
 * Implementation of RMSD global similarity measure.
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class RMSD implements GlobalComparator {
    public enum AtomFilter {
        ALL, BACKBONE, MAIN
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RMSD.class);

    private AtomFilter filter;
    private boolean onlyHeavy;

    public RMSD(AtomFilter filter, boolean onlyHeavy) {
        super();
        this.filter = filter;
        this.onlyHeavy = onlyHeavy;
    }

    public AtomFilter getFilter() {
        return filter;
    }

    public void setFilter(AtomFilter filter) {
        this.filter = filter;
    }

    public boolean isOnlyHeavy() {
        return onlyHeavy;
    }

    public void setOnlyHeavy(boolean onlyHeavy) {
        this.onlyHeavy = onlyHeavy;
    }

    @Override
    public GlobalComparisonResult compareGlobally(StructureSelection s1,
            StructureSelection s2) throws IncomparableStructuresException {
        MCQMatcher matcher = new MCQMatcher(true,
                MCQ.getAllAvailableTorsionAngles());
        SelectionMatch matches = matcher.matchSelections(s1, s2);

        if (matches == null || matches.getSize() == 0) {
            throw new IncomparableStructuresException("No matching fragments "
                    + "found");
        }

        List<Atom> atomsL = new ArrayList<>();
        List<Atom> atomsR = new ArrayList<>();

        for (int i = 0; i < matches.getSize(); i++) {
            FragmentMatch fragment = matches.getFragmentMatch(i);
            RMSD.LOGGER.debug("Taking into account fragments: " + fragment);
            FragmentComparison fragmentComparison = fragment.getBestResult();

            for (ResidueComparison residueComparison : fragmentComparison) {
                ResidueAngles left = residueComparison.getLeft();
                ResidueAngles right = residueComparison.getRight();
                ResidueType residueType = left.getResidueType();
                MoleculeType chainType = residueType.getChainType();
                List<AtomName> atomNames = new ArrayList<>();
                AtomName[] atoms = chainType.getBackboneAtoms();

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

        try {
            Atom[] atomSetL = atomsL.toArray(new Atom[atomsL.size()]);
            Atom[] atomSetR = atomsR.toArray(new Atom[atomsR.size()]);
            SVDSuperimposer superimposer = new SVDSuperimposer(atomSetL,
                    atomSetR);

            List<Atom> clones = new ArrayList<>();

            for (Atom atom : atomsR) {
                Atom r = (Atom) atom.clone();
                Calc.rotate(r, superimposer.getRotation());
                Calc.shift(r, superimposer.getTranslation());
                clones.add(r);
            }

            double rmsd = SVDSuperimposer.getRMS(atomSetL,
                    clones.toArray(new Atom[clones.size()]));
            return new GlobalComparisonResult(getName(), s1.getName(),
                    s2.getName(), matches, rmsd, false);
        } catch (StructureException e) {
            throw new IncomparableStructuresException(
                    "Failed to calculate RMSD", e);
        }
    }

    @Override
    public String getName() {
        return "RMSD";
    }

    // @formatter:off
//    public double compare(Structure s1, Structure s2)
//            throws IncomparableStructuresException {
//        RMSD.LOGGER.debug("Comparing: " + s1.getPDBCode() + " and "
//                + s2.getPDBCode());
//
//        if (McqHelper.isNucleicAcid(s1) != McqHelper.isNucleicAcid(s2)) {
//            return Double.NaN;
//        }
//
//        try {
//            Structure[] structures = new Structure[] { s1.clone(), s2.clone() };
//            Pair<List<Atom>, List<Atom>> atoms =
//                    McqHelper.getCommonAtomArray(structures[0], structures[1],
//                            false);
//            if (atoms == null) {
//                atoms =
//                        McqHelper.getCommonAtomArray(structures[0], structures[1],
//                                true);
//            }
//            assert atoms != null;
//
//            List<Atom> left = atoms.getLeft();
//            List<Atom> right = atoms.getRight();
//
//            if (left.size() != right.size()) {
//                RMSD.LOGGER.info("Atom sets have different sizes. Must use "
//                        + "alignment before calculating RMSD");
//                AlignmentOutput output =
//                        AlignerStructure.align(structures[0], structures[1], "");
//                return output.getAFPChain().getTotalRmsdOpt();
//            }
//            RMSD.LOGGER.debug("Atom set size: " + left.size());
//
//            Atom[] leftArray = left.toArray(new Atom[left.size()]);
//            Atom[] rightArray = right.toArray(new Atom[right.size()]);
//            SVDSuperimposer superimposer =
//                    new SVDSuperimposer(leftArray, rightArray);
//            Calc.rotate(structures[1], superimposer.getRotation());
//            Calc.shift(structures[1], superimposer.getTranslation());
//            return SVDSuperimposer.getRMS(leftArray, rightArray);
//        } catch (StructureException e) {
//            RMSD.LOGGER.error("Failed to compare structures", e);
//            throw new IncomparableStructuresException(e);
//        }
//    }
    // @formatter:on

}
