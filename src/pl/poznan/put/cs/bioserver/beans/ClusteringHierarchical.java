package pl.poznan.put.cs.bioserver.beans;

import java.net.URL;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.poznan.put.cs.bioserver.clustering.ClustererHierarchical;
import pl.poznan.put.cs.bioserver.clustering.ClustererHierarchical.Cluster;
import pl.poznan.put.cs.bioserver.clustering.ClustererHierarchical.Linkage;
import pl.poznan.put.cs.bioserver.clustering.HierarchicalPlot;
import pl.poznan.put.cs.bioserver.external.Matplotlib;
import pl.poznan.put.cs.bioserver.helper.Visualizable;

@XmlRootElement
public class ClusteringHierarchical extends XMLSerializable implements Visualizable {
    private static final long serialVersionUID = -933748828643803893L;

    public static ClusteringHierarchical newInstance(ComparisonGlobal comparison, Linkage linkage) {
        List<Cluster> result = ClustererHierarchical.hierarchicalClustering(
                comparison.getDistanceMatrix(), linkage);

        ClusteringHierarchical instance = new ClusteringHierarchical();
        instance.setComparison(comparison);
        instance.setLinkage(linkage);
        instance.setClustering(result);
        return instance;
    }

    ComparisonGlobal comparison;
    Linkage linkage;
    List<Cluster> clustering;

    public List<Cluster> getClustering() {
        return clustering;
    }

    public ComparisonGlobal getComparison() {
        return comparison;
    }

    public Linkage getLinkage() {
        return linkage;
    }

    @XmlElement
    public void setClustering(List<Cluster> clustering) {
        this.clustering = clustering;
    }

    @XmlElement
    public void setComparison(ComparisonGlobal comparison) {
        this.comparison = comparison;
    }

    @XmlElement
    public void setLinkage(Linkage linkage) {
        this.linkage = linkage;
    }

    @Override
    public void visualize() {
        HierarchicalPlot plot = new HierarchicalPlot(this);
        plot.setVisible(true);
    }

    @Override
    public void visualize3D() {
        throw new UnsupportedOperationException("Method not implemented!");
    }

    @Override
    public void visualizeHighQuality() {
        URL resource = getClass().getResource(
                "/pl/poznan/put/cs/bioserver/external/MatplotlibHierarchical.xsl");
        Matplotlib.runXsltAndPython(resource, this);
    }
}
