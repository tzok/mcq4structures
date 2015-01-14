package pl.poznan.put.visualisation;

import java.io.File;

import pl.poznan.put.clustering.hierarchical.HierarchicalClusteringResult;

public class DendrogramComponent extends SVGComponent {
    public DendrogramComponent(HierarchicalClusteringResult clustering,
            String[] names) {
        super(clustering.toSVG(names, true));
    }

    @Override
    public File suggestName() {
        return new File("dendrogram.svg");
    }
}
