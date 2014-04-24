package pl.poznan.put.sandbox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.helper.Helper;
import pl.poznan.put.helper.UniTypeQuadruplet;
import pl.poznan.put.torsion.AngleType;
import pl.poznan.put.torsion.DihedralAngles;
import pl.poznan.put.torsion.NucleotideDihedral;
import pl.poznan.put.torsion.Quadruplet;

public class PrintAngles {
    private static final Set<String> BACKBONE_ANGLES = new HashSet<>(
            Arrays.asList(new String[] { "ALPHA", "BETA", "GAMMA", "DELTA",
                    "EPSILON", "ZETA" }));
    private static final Set<String> ALL_BUT_CHI_ANGLES =
            new HashSet<>(Arrays.asList(new String[] { "ALPHA", "BETA",
                    "GAMMA", "DELTA", "EPSILON", "ZETA", "TAU0", "TAU1",
                    "TAU2", "TAU3", "TAU4" }));

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: PrintAngles <PDB> <PDB> ... <PDB>");
            return;
        }

        Locale.setDefault(Locale.US);
        DecimalFormat format = new DecimalFormat("0.0");
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setNaN("NaN");
        format.setDecimalFormatSymbols(symbols);

        System.out.print("PDB\tMethod\tResolution\tModel\tChain\tResidue\tiCode\tSymbol\t");
        for (AngleType type : NucleotideDihedral.getAngles()) {
            System.out.print(type.getAngleName());
            System.out.print('\t');
        }
        System.out.println("MCQ_BACKBONE\tMCQ_BACKBONE_RIBOSE\tP");

        PDBFileReader pdbFileReader = new PDBFileReader();

        for (int i = 0; i < args.length; i++) {
            try {
                Structure structure = pdbFileReader.getStructure(args[i]);

                for (int j = 0; j < structure.nrModels(); j++) {
                    List<Chain> model = structure.getModel(j);

                    Helper.normalizeAtomNames(model);
                    List<Atom> atomArray =
                            Helper.getAtomArray(model,
                                    NucleotideDihedral.getUsedAtoms());

                    MultiKeyMap<Object, Double> map = new MultiKeyMap<>();
                    SortedSet<ResidueNumber> set = new TreeSet<>();

                    for (AngleType type : NucleotideDihedral.getAngles()) {
                        for (Quadruplet quadruplet : DihedralAngles.getQuadruplets(
                                atomArray, type)) {
                            UniTypeQuadruplet<Atom> atoms =
                                    quadruplet.getAtoms();
                            double dihedral =
                                    DihedralAngles.calculateDihedral(atoms);
                            Atom b1 = atoms.b;
                            assert b1 != null;

                            ResidueNumber residueNumber =
                                    b1.getGroup().getResidueNumber();
                            set.add(residueNumber);
                            map.put(residueNumber, type, dihedral);
                        }
                    }

                    for (ResidueNumber residueNumber : set) {
                        System.out.print(structure.getPDBCode());
                        System.out.print('\t');
                        System.out.print(structure.getPDBHeader().getTechnique());
                        System.out.print('\t');
                        System.out.print(format.format(structure.getPDBHeader().getResolution()));
                        System.out.print('\t');

                        System.out.print(j + 1);
                        System.out.print('\t');

                        System.out.print(residueNumber.getChainId());
                        System.out.print('\t');
                        System.out.print(residueNumber.getSeqNum());
                        System.out.print('\t');
                        System.out.print(residueNumber.getInsCode() != null
                                ? residueNumber.getInsCode() : '_');
                        System.out.print('\t');

                        try {
                            System.out.print(structure.getChainByPDB(
                                    residueNumber.getChainId()).getGroupByPDB(
                                    residueNumber).getPDBName().trim());
                        } catch (StructureException e) {
                            System.out.print('N');
                        }

                        System.out.print('\t');

                        List<Double> valuesBackbone = new ArrayList<>();
                        List<Double> valuesAllButChi = new ArrayList<>();
                        double[] taus = new double[5];

                        for (AngleType type : NucleotideDihedral.getAngles()) {
                            Double dihedral = map.get(residueNumber, type);

                            if (dihedral == null) {
                                dihedral = Double.NaN;
                            }

                            if (dihedral < 0) {
                                dihedral += 2 * Math.PI;
                            }

                            if (type.getAngleName().startsWith("TAU")) {
                                taus[Integer.valueOf(type.getAngleName().substring(
                                        3))] = dihedral;
                            }

                            System.out.print(format.format(Math.toDegrees(dihedral)));
                            System.out.print('\t');

                            if (PrintAngles.BACKBONE_ANGLES.contains(type.getAngleName())) {
                                valuesBackbone.add(dihedral);
                            }

                            if (PrintAngles.ALL_BUT_CHI_ANGLES.contains(type.getAngleName())) {
                                valuesAllButChi.add(dihedral);
                            }
                        }

                        double mcq = MCQ.calculate(valuesBackbone);
                        mcq = mcq < 0 ? mcq + 2 * Math.PI : mcq;
                        System.out.print(format.format(Math.toDegrees(mcq)));
                        System.out.print('\t');

                        mcq = MCQ.calculate(valuesAllButChi);
                        mcq = mcq < 0 ? mcq + 2 * Math.PI : mcq;
                        System.out.print(format.format(Math.toDegrees(mcq)));
                        System.out.print('\t');

                        double scale =
                                2 * (Math.sin(Math.toRadians(36.0)) + Math.sin(Math.toRadians(72.0)));
                        double y1 = taus[1] + taus[4] - taus[0] - taus[3];
                        double x1 = taus[2] * scale;
                        double p = Math.atan2(y1, x1);
                        p = p < 0 ? p + 2 * Math.PI : p;
                        System.out.println(format.format(Math.toDegrees(p)));
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed for: " + args[i]);
                e.printStackTrace();
            }
        }
    }
}
