package pl.poznan.put.comparison.local;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.matching.SelectionMatch;

@Data
@NoArgsConstructor
public abstract class LocalResult implements Exportable, Tabular {
  protected SelectionMatch selectionMatch;

  protected LocalResult(final SelectionMatch selectionMatch) {
    super();
    this.selectionMatch = selectionMatch;
  }
}
