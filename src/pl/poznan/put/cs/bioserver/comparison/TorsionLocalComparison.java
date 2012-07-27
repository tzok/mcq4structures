
package pl.poznan.put.cs.bioserver.comparison;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.io.PDBFileReader;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import pl.poznan.put.cs.bioserver.torsion.DihedralAngles;
import pl.poznan.put.cs.bioserver.torsion.DihedralAngles.Dihedral;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;

import javax.imageio.ImageIO;

/**
 * Implementation of local dissimilarity measure based on torsion angles.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class TorsionLocalComparison extends LocalComparison {
    private static final Logger logger = Logger
            .getLogger(TorsionLocalComparison.class);

    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("ERROR");
            System.out.println("Incorrect number of arguments provided");
            return;
        }
        PDBFileReader reader = new PDBFileReader();
        try {
            Structure s1 = reader.getStructure(args[0]);
            Structure s2 = reader.getStructure(args[1]);
            int c1 = Integer.parseInt(args[2]);
            int c2 = Integer.parseInt(args[3]);
            int type = Integer.parseInt(args[4]);
            Vector<Integer> anglesToShow = new Vector<Integer>();
            for (String angle : args[5].split(",")) {
                anglesToShow.add(Integer.parseInt(angle));
            }

            if (type != 0 && type != 1) {
                System.out.println("ERROR");
                System.out.println("Type must be set to 0 for proteins and 1 "
                        + "for RNAs");
                return;
            }

            TorsionLocalComparison comparison = new TorsionLocalComparison();
            comparison.checkValidity(new Structure[] {
                    s1, s2
            });
            double[][][] compare = comparison.compare(s1.getChain(c1),
                    s2.getChain(c2));

            if (compare[type].length == 0) {
                System.out.println("ERROR");
                System.out.println("Nothing to plot. Probably incorrect "
                        + "structure type given");
                return;
            }

            if (args.length == 7 && args[6].equals("dump")) {
                for (int i = 0; i < compare[type].length; ++i) {
                    System.out.print(i);
                    System.out.print('\t');
                    for (int angle : anglesToShow) {
                        System.out.print(compare[type][i][angle]);
                        System.out.print('\t');
                    }
                    System.out.println();
                }
                return;
            }

            String[][] angleNames = new String[][] {
                    {
                            "phi", "psi", "omega", "MCQ"
                    },
                    {
                            "alpha", "beta", "gamma", "delta", "epsilon", "zeta",
                            "chi", "P", "MCQ"
                    }
            };

            DefaultXYDataset dataset = new DefaultXYDataset();
            for (int angle : anglesToShow) {
                if (angle >= compare[type][0].length) {
                    continue;
                }
                double[] x = new double[compare[type].length];
                double[] y = new double[compare[type].length];
                for (int i = 0; i < compare[type].length; ++i) {
                    x[i] = i + 1;
                    y[i] = compare[type][i][angle];
                }
                dataset.addSeries(angleNames[type][angle], new double[][] {
                        x,
                        y
                });
            }
            /*
             * draw a plot and replace the previous one
             */
            TickUnitSource tickUnitSource = NumberAxis.createIntegerTickUnits();
            NumberTickUnit tickUnit = (NumberTickUnit) tickUnitSource
                    .getCeilingTickUnit(5);
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel("Group index");
            xAxis.setTickUnit(tickUnit);

            NumberAxis yAxis = new NumberAxis();
            yAxis.setAutoRange(false);
            yAxis.setRange(0, Math.PI);
            yAxis.setLabel("Distance [rad]");

            int width = Integer.parseInt(System.getProperty("plot.width",
                    "1024"));
            int height = Integer.parseInt(System.getProperty("plot.height",
                    "768"));

            XYPlot plot = new XYPlot(dataset, xAxis, yAxis,
                    new DefaultXYItemRenderer());
            JFreeChart chart = new JFreeChart(plot);
            BufferedImage image = chart.createBufferedImage(width, height);
            File tempFile = File.createTempFile("molcmp", ".png");
            ImageIO.write(image, "png", new FileOutputStream(tempFile));

            System.out.println("OK");
            System.out.println(tempFile);
        } catch (IOException e) {
            System.out.println("ERROR");
            System.out.println(e.getMessage());
        } catch (IncomparableStructuresException e) {
            System.out.println("ERROR");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Compare specified pair of chains.
     * 
     * @throws IncomparableStructuresException
     */
    public double[][][] compare(Chain c1, Chain c2)
            throws IncomparableStructuresException {
        checkValidity(new Structure[] {
                new StructureImpl(c1),
                new StructureImpl(c2)
        });
        DihedralAngles dihedralAngles = new DihedralAngles();
        Dihedral[][][] dihedrals = new Dihedral[2][][];
        dihedrals[0] = dihedralAngles.getDihedrals(c1);
        dihedrals[1] = dihedralAngles.getDihedrals(c2);

        double[][][] result = new double[2][][];
        // iterate independently over amino acids and nucleotides
        StringWriter writer = new StringWriter();
        for (int j = 0; j < 2; ++j) {
            result[j] = new double[dihedrals[0][j].length][];
            // iterate over each group of this type
            for (int k = 0; k < dihedrals[0][j].length; ++k) {
                Dihedral d1 = dihedrals[0][j][k];
                Dihedral d2 = dihedrals[1][j][k];
                int angleCount = d1.angles.length;
                double[] difference = new double[angleCount + 1];
                double[] sum = new double[2];
                // iterate over each angle in this group
                for (int l = 0; l < angleCount; ++l) {
                    double a1 = d1.angles[l];
                    double a2 = d2.angles[l];
                    difference[l] = DihedralAngles.subtract(a1, a2);
                    writer.append(Double.toString(difference[l]));
                    writer.append('\t');
                    // sine and cosine are for MCQ for this group
                    sum[0] += Math.sin(difference[l]);
                    sum[1] += Math.cos(difference[l]);
                }
                difference[angleCount] = Math.atan2(sum[0] / angleCount, sum[1]
                        / angleCount);
                result[j][k] = difference;
                writer.append(Double.toString(difference[angleCount]));
                writer.append('\n');
            }
            writer.append('\n');
        }
        TorsionLocalComparison.logger
                .trace("The result of local comparison in torsional angle space:\n"
                        + writer.toString());
        return result;
    }

    /**
     * Compare two structures using local measure based on torsion angle
     * representation.
     * 
     * @param s1 First structure.
     * @param s2 Second structure.
     * @return An array "double[][][][] result" which must be interpreted this
     *         way: result[i] is for i-th chain, result[i][0] is for amino acids
     *         and result[i][1] is for nucleotides, result[i][j][k] is k-th
     *         group and result[i][j][k][l] is difference on l-th angle. The
     *         last value in array result[i][j][k] is an MCQ from this k-th
     *         group.
     */
    @Override
    public double[][][][] compare(Structure s1, Structure s2)
            throws IncomparableStructuresException {
        Structure[] structures = new Structure[] {
                s1, s2
        };
        checkValidity(structures);

        double[][][][] result = new double[s1.size()][][][];
        for (int i = 0; i < s1.size(); ++i) {
            result[i] = compare(s1.getChain(i), s2.getChain(i));
        }
        return result;
    }
}
