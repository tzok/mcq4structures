package pl.poznan.put.visualisation;

import java.util.List;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.torsion.MasterTorsionAngleType;

/**
 * An interface for different methods which use information about residue comparison results and map
 * this into 0-1 range for Varna to visualize.
 */
public interface ComparisonMapper {
  /**
   * Iterate over a list of results of residue comparisons and for each residue give a double value
   * from range 0-1 where 0 equals identical and 1 equals totally dissimilar.
   *
   * @param residueComparisons List of results of comparison for single residues.
   * @param angleTypes List of angle types available for each residue.
   * @return An array which must have the same length as input list. Each item in the array must be
   *     a value from 0-1 range.
   */
  Double[] map(List<ResidueComparison> residueComparisons, List<MasterTorsionAngleType> angleTypes);
}
