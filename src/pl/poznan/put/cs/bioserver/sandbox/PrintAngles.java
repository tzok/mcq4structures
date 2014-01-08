package pl.poznan.put.cs.bioserver.sandbox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections15.map.MultiKeyMap;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.UniTypeQuadruplet;
import pl.poznan.put.cs.bioserver.torsion.AngleType;
import pl.poznan.put.cs.bioserver.torsion.DihedralAngles;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;
import pl.poznan.put.cs.bioserver.torsion.Quadruplet;

public class PrintAngles {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: PrintAngles <PDB>");
            return;
        }

        try {
            Structure structure = new PDBFileReader().getStructure(args[0]);
            Helper.normalizeAtomNames(structure);
            List<Atom> atomArray = Helper.getAtomArray(structure,
                    NucleotideDihedral.getUsedAtoms());

            MultiKeyMap<Object, Double> map = new MultiKeyMap<>();
            SortedSet<ResidueNumber> set = new TreeSet<>();
            for (AngleType type : NucleotideDihedral.getAngles()) {
                for (Quadruplet quadruplet : DihedralAngles.getQuadruplets(
                        atomArray, type)) {
                    UniTypeQuadruplet<Atom> atoms = quadruplet.getAtoms();
                    double dihedral = DihedralAngles.calculateDihedral(atoms);
                    Atom b1 = atoms.b;
                    assert b1 != null;

                    ResidueNumber residueNumber = b1.getGroup()
                            .getResidueNumber();
                    set.add(residueNumber);
                    map.put(residueNumber, type, dihedral);
                }
            }

            System.out.print("Residue\tSymbol\t");
            for (AngleType type : NucleotideDihedral.getAngles()) {
                System.out.print(type.getAngleName());
                System.out.print('\t');
            }
            System.out.println("MCQ_BACKBONE\tMCQ_BACKBONE_RIBOSE\tP");

            Set<String> setBackbone = new HashSet<>(Arrays.asList(new String[] {
                    "ALPHA", "BETA", "GAMMA", "DELTA", "EPSILON", "ZETA" }));
            Set<String> setAllButChi = new HashSet<>(
                    Arrays.asList(new String[] { "ALPHA", "BETA", "GAMMA",
                            "DELTA", "EPSILON", "ZETA", "TAU0", "TAU1", "TAU2",
                            "TAU3", "TAU4" }));

            DecimalFormat format = new DecimalFormat("0.0");
            DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
            symbols.setNaN("NaN");
            format.setDecimalFormatSymbols(symbols);

            for (ResidueNumber number : set) {
                System.out
                        .print(number.getChainId() + "." + number.getSeqNum());
                System.out.print('\t');
                try {
                    System.out.print(structure
                            .getChainByPDB(number.getChainId())
                            .getGroupByPDB(number).getPDBName());
                } catch (StructureException e) {
                    System.out.print('N');
                }
                System.out.print('\t');
                List<Double> valuesBackbone = new ArrayList<>();
                List<Double> valuesAllButChi = new ArrayList<>();
                double[] taus = new double[5];
                for (AngleType type : NucleotideDihedral.getAngles()) {
                    Double dihedral = map.get(number, type);
                    if (dihedral == null) {
                        dihedral = Double.NaN;
                    }
                    if (dihedral < 0) {
                        dihedral += 2 * Math.PI;
                    }
                    if (type.getAngleName().startsWith("TAU")) {
                        taus[Integer.valueOf(type.getAngleName().substring(3))] = dihedral;
                    }
                    System.out.print(format.format(Math.toDegrees(dihedral)));
                    System.out.print('\t');

                    if (setBackbone.contains(type.getAngleName())) {
                        valuesBackbone.add(dihedral);
                    }
                    if (setAllButChi.contains(type.getAngleName())) {
                        valuesAllButChi.add(dihedral);
                    }
                }
                double mcq = MCQ.calculate(valuesBackbone);
                if (mcq < 0) {
                    mcq += 2 * Math.PI;
                }
                System.out.print(format.format(Math.toDegrees(mcq)));
                System.out.print('\t');

                mcq = MCQ.calculate(valuesAllButChi);
                if (mcq < 0) {
                    mcq += 2 * Math.PI;
                }
                System.out.print(format.format(Math.toDegrees(mcq)));
                System.out.print('\t');

                double scale = 2 * (Math.sin(Math.toRadians(36.0)) + Math
                        .sin(Math.toRadians(72.0)));
                double y1 = taus[1] + taus[4] - taus[0] - taus[3];
                double x1 = taus[2] * scale;
                double p = Math.atan2(y1, x1);
                if (p < 0) {
                    p += 2 * Math.PI;
                }
                System.out.println(format.format(Math.toDegrees(p)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
