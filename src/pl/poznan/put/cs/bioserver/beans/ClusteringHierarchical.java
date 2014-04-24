package pl.poznan.put.cs.bioserver.beans;

import java.net.URL;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.poznan.put.clustering.ClustererHierarchical;
import pl.poznan.put.clustering.ClustererHierarchical.Cluster;
import pl.poznan.put.clustering.ClustererHierarchical.Linkage;
import pl.poznan.put.cs.bioserver.clustering.HierarchicalPlot;
import pl.poznan.put.cs.bioserver.external.Matplotlib;
import pl.poznan.put.cs.bioserver.helper.Visualizable;

@XmlRootElement
public class ClusteringHierarchical extends XMLSerializable implements
        Visualizable {
    private static final long serialVersionUID = -933748828643803893L;

    public static ClusteringHierarchical newInstance(
            ComparisonGlobal comparison, Linkage linkage) {
        List<Cluster> result =
                ClustererHierarchical.hierarchicalClustering(
                        comparison.getDistanceMatrix(), linkage);
        return new ClusteringHierarchical(result, comparison, linkage);
    }

    private List<Cluster> clustering;
    private ComparisonGlobal comparison;
    private Linkage linkage;

    public ClusteringHierarchical() {
    }

    private ClusteringHierarchical(List<Cluster> clustering,
            ComparisonGlobal comparison, Linkage linkage) {
        super();
        this.clustering = clustering;
        this.comparison = comparison;
        this.linkage = linkage;
    }

    // FIXME: Cluster class is not serialized into XML (not needed at the
    // moment, but still...)
    public List<Cluster> getClustering() {
        return clustering;
    }

    @XmlElement
    public void setClustering(List<Cluster> clustering) {
        this.clustering = clustering;
    }

    public ComparisonGlobal getComparison() {
        return comparison;
    }

    @XmlElement
    public void setComparison(ComparisonGlobal comparison) {
        this.comparison = comparison;
    }

    public Linkage getLinkage() {
        return linkage;
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
        URL resource =
                getClass().getResource(
                        "/pl/poznan/put/cs/bioserver/external/MatplotlibHierarchical.xsl");
        Matplotlib.runXsltAndPython(resource, this);
    }
}
