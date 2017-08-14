package pl.poznan.put.comparison.local;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.SelectionMatch;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@XmlRootElement
public abstract class LocalResult implements Exportable, Visualizable, Tabular {
    @XmlElement protected SelectionMatch selectionMatch;

    protected LocalResult(final SelectionMatch selectionMatch) {
        super();
        this.selectionMatch = selectionMatch;
    }
}
