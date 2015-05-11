package pl.poznan.put.visualisation;

import java.io.File;
import java.util.List;

import pl.poznan.put.clustering.hierarchical.HierarchicalClusteringResult;

public class DendrogramComponent extends SVGComponent {
    public DendrogramComponent(HierarchicalClusteringResult clustering,
            List<String> list) {
        super(clustering.toSVG(list, true));
    }

    @Override
    public File suggestName() {
        return new File("dendrogram.svg");
    }
}
