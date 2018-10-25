# Mean of Circular Quantities (MCQ)

Mean of Circular Quantities (MCQ) is a dissimilarity measure useful in comparison of 3D protein and/or RNA structures. It calculates an average difference between corresponding torsion angle values (rotations around bonds). More information can be found in:
> Zok, T., Popenda, M., & Szachniuk, M. (2014). MCQ4Structures to compute similarity of molecule structures. Central European Journal of Operations Research, 22(3), 457–473. https://doi.org/10.1007/s10100-013-0296-5

## Contents

This project consists of a few subprojets:
- `mcq-common`: base functionality
- `mcq-clustering`: partitional and hierarchical clustering
- `mcq-cli`: command-line interface
- `mcq-gui`: graphical interface

## Main Ideas

- Use `pl.poznan.put.comparison.MCQ#compareGlobally` to compare two 3D structures and obtain a global value of dissimilarity. You can use `pl.poznan.put.comparison.global.ParallelGlobalComparator` to process multiple inputs in parallel
- Use `pl.poznan.put.comparison.MCQ#comparePair` to obtain detailed information about dissimilarity of two 3D structures
- Use `pl.poznan.put.comparison.MCQ#compareModels` in a situation where a distinguished reference 3D structure is known and you want to know how 3D models compare to it

## Clustering

- Use `pl.poznan.put.clustering.hierarchical.Clusterer` to construct dendrograms from a distance matrix (with `COMPLETE`, `SINGLE` or `AVERAGE` linkage option)
- Use `pl.poznan.put.clustering.partitional.KMedoids` to perform partitional clustering based on distance matrix
- Use `pl.poznan.put.clustering.partitional.KScanner#parallelScan` to find optimum number of clusters with respect to silhouette score