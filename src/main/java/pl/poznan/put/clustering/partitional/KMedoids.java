package pl.poznan.put.clustering.partitional;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMedoids implements PrototypeBasedClusterer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KMedoids.class);

    private final int retries;

    public KMedoids(int retries) {
        this.retries = retries;
    }

    /**
     * Find indices of objects that are best prototypes according to chosen
     * score.
     * 
     * @param matrix
     *            Distance matrix.
     * @param sf
     *            Scoring function to use, either PAM or PAMSIL.
     * @param k
     *            If null, then find optimal 'k', otherwise use the one
     *            specified.
     * @return Indices of objects found to be best prototypes for clustering.
     */
    @Override
    public ScoredClusteringResult findPrototypes(double[][] matrix,
            ScoringFunction sf, int k) {
        double overallBestScore = Double.NEGATIVE_INFINITY;
        ClusterPrototypes overallBestMedoids = null;

        for (int trial = 0; trial < retries; trial++) {
            ClusterPrototypes medoids = ClusterPrototypes.initializeRandomly(
                    matrix, k);
            double score = sf.score(medoids, matrix);

            while (true) {
                Set<Integer> nonmedoids = new HashSet<Integer>();

                for (int i = 0; i < matrix.length; i++) {
                    if (!medoids.isPrototype(i)) {
                        nonmedoids.add(i);
                    }
                }

                double bestScore = score;
                ClusterPrototypes bestMedoids = medoids;

                for (int i : medoids) {
                    for (int j : nonmedoids) {
                        ClusterPrototypes swapped = medoids.swap(i, j);
                        double newScore = sf.score(swapped, matrix);

                        if (newScore > bestScore) {
                            bestScore = newScore;
                            bestMedoids = swapped;
                        }
                    }
                }

                if (bestScore > score) {
                    score = bestScore;
                    medoids = bestMedoids;
                } else {
                    break;
                }
            }

            if (score > overallBestScore) {
                overallBestScore = score;
                overallBestMedoids = medoids;
            }
        }

        assert overallBestMedoids != null;
        double silhouette = PAMSIL.getInstance().score(overallBestMedoids,
                matrix);

        DecimalFormat format = new DecimalFormat("0.000");
        KMedoids.LOGGER.debug("Final score for clustering (k=" + k
                + "): score=" + format.format(overallBestScore) + " PAMSIL="
                + format.format(silhouette));

        return new ScoredClusteringResult(overallBestMedoids, sf,
                overallBestScore, silhouette);
    }
}
