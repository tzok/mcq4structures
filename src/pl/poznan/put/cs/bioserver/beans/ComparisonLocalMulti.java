package pl.poznan.put.cs.bioserver.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.cs.bioserver.comparison.MCQ;

@XmlRootElement
public class ComparisonLocalMulti extends XMLSerializable {
    private static final long serialVersionUID = -6549267536864184480L;

    List<ComparisonLocal> results;

    public List<ComparisonLocal> getResults() {
        return results;
    }

    @XmlElement
    public void setResults(List<ComparisonLocal> results) {
        this.results = results;
    }

    public static ComparisonLocalMulti newInstance(Chain[] chains,
            Chain reference) throws StructureException {
        List<ComparisonLocal> list = new ArrayList<>();
        for (int i = 0; i < chains.length; i++) {
            if (reference.equals(chains[i])) {
                continue;
            }
            list.add(ComparisonLocal.newInstance(reference, chains[i],
                    MCQ.USED_ANGLES_NAMES));
        }

        ComparisonLocalMulti instance = new ComparisonLocalMulti();
        instance.setResults(list);
        return instance;
    }
}
