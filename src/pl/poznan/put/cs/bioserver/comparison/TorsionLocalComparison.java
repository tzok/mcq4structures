package pl.poznan.put.cs.bioserver.comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.torsion.AminoAcidDihedral;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;
import pl.poznan.put.cs.bioserver.torsion.AngleType;
import pl.poznan.put.cs.bioserver.torsion.DihedralAngles;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;

/**
 * Implementation of local dissimilarity measure based on torsion angles.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class TorsionLocalComparison extends LocalComparison {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(TorsionLocalComparison.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        if (args.length != 2 && args.length != 4) {
            System.out.println("ERROR");
            System.out.println("Incorrect number of arguments provided");
            return;
        }

        try {
            PDBFileReader reader = new PDBFileReader();
            Structure[] structures = new Structure[] {
                    reader.getStructure(args[0]), reader.getStructure(args[1]) };
            TorsionLocalComparison comparison = new TorsionLocalComparison();

            Map<String, List<AngleDifference>> result;
            if (args.length == 4) {
                result = TorsionLocalComparison.compare(
                        structures[0].getChainByPDB(args[2]),
                        structures[1].getChainByPDB(args[3]), false);
            } else {
                result = (Map<String, List<AngleDifference>>) comparison
                        .compare(structures[0], structures[1]);
            }

            List<AngleDifference> list = result.get("MCQ");
            for (AngleDifference ad : list) {
                System.out.println(ad.difference);
            }
        } catch (IOException | IncomparableStructuresException
                | StructureException e) {
            System.out.println("ERROR");
            System.out.println(e.getMessage());
        }
    }

    public static Map<String, List<AngleDifference>> compare(Chain c1,
            Chain c2, boolean alignFirst) throws StructureException {
        Atom[][] atoms;
        if (alignFirst) {
            atoms = StructureAligner.align(c1, c2).getAtoms();
        } else {
            atoms = Helper.getCommonAtomArray(c1, c2);
        }
        AngleType[] angles = Helper.isNucleicAcid(c1) ? NucleotideDihedral.ANGLES
                : AminoAcidDihedral.ANGLES;
        return compare(atoms, angles);
    }

    public static Map<String, List<AngleDifference>> compare(Structure s1,
            Structure s2, boolean alignFirst) throws StructureException {
        Atom[][] atoms;
        if (alignFirst) {
            atoms = StructureAligner.align(s1, s2).getAtoms();
        } else {
            atoms = Helper.getCommonAtomArray(s1, s2);
        }
        AngleType[] angles = Helper.isNucleicAcid(s1) ? NucleotideDihedral.ANGLES
                : AminoAcidDihedral.ANGLES;
        return compare(atoms, angles);
    }

    public static Map<String, List<AngleDifference>> compare(Atom[][] atoms,
            AngleType[] angles) {
        Atom[][] equalized = Helper.equalize(atoms);

        Map<String, List<AngleDifference>> map = new HashMap<>();
        List<AngleDifference> allDiffs = new ArrayList<>();
        for (AngleType at : angles) {
            List<AngleDifference> diffs = DihedralAngles.calculateAngleDiff(
                    equalized, at);
            map.put(at.getAngleName(), diffs);
            allDiffs.addAll(diffs);
        }

        Map<ResidueNumber, List<AngleDifference>> mapResidueToDiffs = new HashMap<>();
        Map<ResidueNumber, Atom[][]> mapResidueToQuads = new HashMap<>();
        for (AngleDifference ad : allDiffs) {
            ResidueNumber residue = ad.quad1[0].getGroup().getResidueNumber();
            if (!mapResidueToDiffs.containsKey(residue)) {
                mapResidueToDiffs
                        .put(residue, new ArrayList<AngleDifference>());
                mapResidueToQuads.put(residue, new Atom[][] { ad.quad1,
                        ad.quad2 });
            }
            List<AngleDifference> list = mapResidueToDiffs.get(residue);
            list.add(ad);
        }

        List<AngleDifference> mcqList = new ArrayList<>();
        for (ResidueNumber residue : mapResidueToDiffs.keySet()) {
            Atom[][] quads = mapResidueToQuads.get(residue);
            AngleDifference difference = new AngleDifference(quads[0],
                    quads[1], Double.NaN, Double.NaN,
                    MCQ.calculate(mapResidueToDiffs.get(residue)));
            mcqList.add(difference);
        }

        map.put("MCQ", mcqList);
        return map;
    }

    //
    // AlternativeAlignment alignment = null;
    // // try {
    // // alignment = StructureAligner.align(c1, c2)[0];
    // // } catch (StructureException e) {
    // // TorsionLocalComparison.logger.warn("Failed to align chains prior "
    // // + "to comparison. Will try to compare without it", e);
    // // // FIXME ???
    // // }
    //
    // DihedralContainer[] containers = new DihedralContainer[2];
    // for (int i = 0; i < 2; i++) {
    // containers[i] = DihedralAngles.getDihedrals(alignment, i);
    // }
    //
    // Dihedral[][][] all = new Dihedral[2][][];
    // List<? extends Dihedral> dihedrals;
    // for (int i = 0; i < 2; i++) {
    // all[i] = new Dihedral[2][];
    // dihedrals = containers[i].getAminoAcidDihedrals();
    // all[i][0] = dihedrals.toArray(new Dihedral[dihedrals.size()]);
    //
    // dihedrals = containers[i].getNucleotideDihedrals();
    // all[i][1] = dihedrals.toArray(new Dihedral[dihedrals.size()]);
    // }
    //
    // double[][][] result = new double[2][][];
    // for (int i = 0; i < 2; i++) { // 0 = aminoacid, 1 = nucleotide
    // result[i] = new double[all[0][1].length][];
    // for (int j = 0; j < all[0][i].length; j++) {
    // double[][] current = new double[][] { all[0][i][j].angles,
    // all[1][i][j].angles };
    //
    // int count = current[0].length;
    // double[] diffs = new double[count + 1];
    // double sine = 0, cosine = 0;
    // for (int k = 0; k < count; k++) {
    // diffs[k] += Dihedral.subtractDihedral(current[0][k],
    // current[1][k]);
    // sine += Math.sin(diffs[k]);
    // cosine += Math.cos(diffs[k]);
    // }
    // diffs[count] = Math.atan2(sine / count, cosine / count);
    // result[i][j] = diffs;
    // }
    // }
    // return result;

    // // iterate independently over amino acids and nucleotides
    // StringWriter writer = new StringWriter();
    // for (int j = 0; j < 2; ++j) {
    // result[j] = new double[dihedrals[0][j].length][];
    // // iterate over each group of this type
    // for (int k = 0; k < dihedrals[0][j].length; ++k) {
    // Dihedral d1 = dihedrals[0][j][k];
    // Dihedral d2 = dihedrals[1][j][k];
    // int angleCount = d1.angles.length;
    // double[] difference = new double[angleCount + 1];
    // double[] sum = new double[2];
    // // iterate over each angle in this group
    // for (int l = 0; l < angleCount; ++l) {
    // double a1 = d1.angles[l];
    // double a2 = d2.angles[l];
    // difference[l] = DihedralAngles.subtract(a1, a2);
    // writer.append(Double.toString(difference[l]));
    // writer.append('\t');
    // // sine and cosine are for MCQ for this group
    // sum[0] += Math.sin(difference[l]);
    // sum[1] += Math.cos(difference[l]);
    // }
    // difference[angleCount] = Math.atan2(sum[0] / angleCount, sum[1]
    // / angleCount);
    // result[j][k] = difference;
    // writer.append(Double.toString(difference[angleCount]));
    // writer.append('\n');
    // }
    // writer.append('\n');
    // }
    // TorsionLocalComparison.logger
    // .trace("The result of local comparison in torsional angle space:\n"
    // + writer.toString());
    // return result;
    // }

    // public static void main(String[] args) {
    // if (args.length < 6) {
    // System.out.println("ERROR");
    // System.out.println("Incorrect number of arguments provided");
    // return;
    // }
    //
    // try {
    // PDBFileReader reader = new PDBFileReader();
    // Structure s1 = reader.getStructure(args[0]);
    // Structure s2 = reader.getStructure(args[1]);
    // int c1 = Integer.parseInt(args[2]);
    // int c2 = Integer.parseInt(args[3]);
    // int type = Integer.parseInt(args[4]);
    // Vector<Integer> anglesToShow = new Vector<>();
    // for (String angle : args[5].split(",")) {
    // anglesToShow.add(Integer.parseInt(angle));
    // }
    //
    // if (type != 0 && type != 1) {
    // System.out.println("ERROR");
    // System.out.println("Type must be set to 0 for proteins and 1 "
    // + "for RNAs");
    // return;
    // }
    //
    // double[][][] compare = TorsionLocalComparison.compare(
    // s1.getChain(c1), s2.getChain(c2));
    //
    // if (compare[type].length == 0) {
    // System.out.println("ERROR");
    // System.out.println("Nothing to plot. Probably incorrect "
    // + "structure type given");
    // return;
    // }
    //
    // if (args.length == 7 && args[6].equals("dump")) {
    // for (int i = 0; i < compare[type].length; ++i) {
    // System.out.print(i);
    // System.out.print('\t');
    // for (int angle : anglesToShow) {
    // System.out.print(compare[type][i][angle]);
    // System.out.print('\t');
    // }
    // System.out.println();
    // }
    // return;
    // }
    //
    // String[][] angleNames = new String[][] {
    // { "phi", "psi", "omega", "MCQ" },
    // { "alpha", "beta", "gamma", "delta", "epsilon", "zeta",
    // "chi", "P", "MCQ" } };
    //
    // DefaultXYDataset dataset = new DefaultXYDataset();
    // for (int angle : anglesToShow) {
    // if (angle >= compare[type][0].length) {
    // continue;
    // }
    // double[] x = new double[compare[type].length];
    // double[] y = new double[compare[type].length];
    // for (int i = 0; i < compare[type].length; ++i) {
    // x[i] = i + 1;
    // y[i] = compare[type][i][angle];
    // }
    // dataset.addSeries(angleNames[type][angle], new double[][] { x,
    // y });
    // }
    // /*
    // * draw a plot and replace the previous one
    // */
    // TickUnitSource tickUnitSource = NumberAxis.createIntegerTickUnits();
    // NumberTickUnit tickUnit = (NumberTickUnit) tickUnitSource
    // .getCeilingTickUnit(5);
    // NumberAxis xAxis = new NumberAxis();
    // xAxis.setLabel("Group index");
    // xAxis.setTickUnit(tickUnit);
    //
    // NumberAxis yAxis = new NumberAxis();
    // yAxis.setAutoRange(false);
    // yAxis.setRange(0, Math.PI);
    // yAxis.setLabel("Distance [rad]");
    //
    // int width = Integer.parseInt(System.getProperty("plot.width",
    // "1024"));
    // int height = Integer.parseInt(System.getProperty("plot.height",
    // "768"));
    //
    // XYPlot plot = new XYPlot(dataset, xAxis, yAxis,
    // new DefaultXYItemRenderer());
    // JFreeChart chart = new JFreeChart(plot);
    // BufferedImage image = chart.createBufferedImage(width, height);
    // File tempFile = File.createTempFile("molcmp", ".png");
    // try (FileOutputStream stream = new FileOutputStream(tempFile)) {
    // ImageIO.write(image, "png", stream);
    // }
    //
    // System.out.println("OK");
    // System.out.println(tempFile);
    // } catch (IOException e) {
    // System.out.println("ERROR");
    // System.out.println(e.getMessage());
    // }
    // }

    @Override
    public Object compare(Structure s1, Structure s2)
            throws IncomparableStructuresException {
        try {
            return TorsionLocalComparison.compare(s1, s2, false);
        } catch (StructureException e) {
            throw new IncomparableStructuresException(e);
        }
    }
}
