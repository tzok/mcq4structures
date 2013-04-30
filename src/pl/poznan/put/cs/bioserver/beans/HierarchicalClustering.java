package pl.poznan.put.cs.bioserver.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.poznan.put.cs.bioserver.external.Matplotlib.Method;

@XmlRootElement
public class HierarchicalClustering extends XMLSerializable {
    private static final long serialVersionUID = -933748828643803893L;

    GlobalComparisonResults comparison;
    Method method;

    public static HierarchicalClustering newInstance(
            GlobalComparisonResults comparison, Method method) {
        HierarchicalClustering instance = new HierarchicalClustering();
        instance.setComparison(comparison);
        instance.setMethod(method);
        return instance;
    }

    public GlobalComparisonResults getComparison() {
        return comparison;
    }

    @XmlElement
    public void setComparison(GlobalComparisonResults comparison) {
        this.comparison = comparison;
    }

    public Method getMethod() {
        return method;
    }

    @XmlElement
    public void setMethod(Method method) {
        this.method = method;
    }
}
