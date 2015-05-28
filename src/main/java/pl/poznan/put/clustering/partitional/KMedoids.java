package pl.poznan.put.clustering.partitional;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMedoids implements PrototypeBasedClusterer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KMedoids.class);
    private static final int DEFAULT_RETRIES = 8;

    private final int retries;

    public KMedoids() {
        retries = KMedoids.DEFAULT_RETRIES;
    }

    public KMedoids(int retries) {
        this.retries = retries;
    }

    /**
     * Find indices of objects that are best prototypes according to chosen
     * score.
     *
     * @param distanceMatrix
     *            Distance matrix.
     * @param scoringFunction
     *            Scoring function to use, either PAM or PAMSIL.
     * @param clusterCount
     *            If null, then find optimal 'k', otherwise use the one
     *            specified.
     * @return Indices of objects found to be best prototypes for clustering.
     */
    @Override
    public ScoredClusteringResult findPrototypes(double[][] distanceMatrix,
            ScoringFunction scoringFunction, int clusterCount) {
        double overallBestScore = Double.NEGATIVE_INFINITY;
        ClusterPrototypes overallBestPrototypes = null;

        for (int trial = 0; trial < retries; trial++) {
            ClusterPrototypes initialPrototypes = ClusterPrototypes.initializeRandomly(distanceMatrix, clusterCount);
            ScoredClusteringResult clusteringResult = findPrototypes(distanceMatrix, scoringFunction, initialPrototypes);
            double score = clusteringResult.getScore();

            if (score > overallBestScore) {
                overallBestScore = score;
                overallBestPrototypes = clusteringResult.getPrototypes();
            }
        }
        assert overallBestPrototypes != null;

        PAMSIL pamsil = PAMSIL.getInstance();
        double silhouette = pamsil.score(overallBestPrototypes, distanceMatrix);

        DecimalFormat format = new DecimalFormat("0.000");
        KMedoids.LOGGER.debug("Final score for clustering (k=" + clusterCount + "): score=" + format.format(overallBestScore) + " PAMSIL=" + format.format(silhouette));

        return new ScoredClusteringResult(overallBestPrototypes, scoringFunction, overallBestScore, silhouette);
    }

    @Override
    public ScoredClusteringResult findPrototypes(double[][] distanceMatrix,
            ScoringFunction scoringFunction, ClusterPrototypes initialPrototypes) {
        ClusterPrototypes prototypes = initialPrototypes;
        double score = scoringFunction.score(prototypes, distanceMatrix);

        while (true) {
            Set<Integer> nonmedoids = new HashSet<>();

            for (int i = 0; i < distanceMatrix.length; i++) {
                if (!prototypes.isPrototype(i)) {
                    nonmedoids.add(i);
                }
            }

            double bestScore = score;
            ClusterPrototypes bestMedoids = prototypes;

            for (int i : prototypes) {
                for (int j : nonmedoids) {
                    ClusterPrototypes swapped = prototypes.swap(i, j);
                    double newScore = scoringFunction.score(swapped, distanceMatrix);

                    if (newScore > bestScore) {
                        bestScore = newScore;
                        bestMedoids = swapped;
                    }
                }
            }

            if (bestScore > score) {
                score = bestScore;
                prototypes = bestMedoids;
            } else {
                break;
            }
        }

        PAMSIL pamsil = PAMSIL.getInstance();
        double silhouette = pamsil.score(prototypes, distanceMatrix);
        return new ScoredClusteringResult(prototypes, scoringFunction, score, silhouette);
    }
}
