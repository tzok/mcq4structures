package pl.poznan.put.visualisation;

import pl.poznan.put.clustering.hierarchical.HierarchicalClusteringResult;

public class DendrogramComponent extends SVGComponent {
    public DendrogramComponent(HierarchicalClusteringResult clustering,
            String[] names) {
        super(clustering.toSVG(names, true));
    }
}
