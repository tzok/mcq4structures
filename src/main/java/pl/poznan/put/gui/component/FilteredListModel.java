package pl.poznan.put.gui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.AbstractListModel;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;

public class FilteredListModel extends AbstractListModel<PdbCompactFragment> {
  private static final long serialVersionUID = 2878196330323395518L;

  private final List<PdbCompactFragment> listProteins = new ArrayList<>();
  private final List<PdbCompactFragment> listRNAs = new ArrayList<>();

  private boolean isProtein = true;
  private boolean isRNA = true;

  public final void setProtein(final boolean isProtein) {
    this.isProtein = isProtein;
  }

  public final void setRNA(final boolean isRNA) {
    this.isRNA = isRNA;
  }

  public final List<PdbCompactFragment> getElements() {
    return Stream.concat(listRNAs.stream(), listProteins.stream()).collect(Collectors.toList());
  }

  public final List<PdbCompactFragment> getSelectedElements() {
    final List<PdbCompactFragment> list = new ArrayList<>();
    if (isRNA) {
      list.addAll(listRNAs);
    }
    if (isProtein) {
      list.addAll(listProteins);
    }
    return list;
  }

  public final void addElements(final Iterable<PdbCompactFragment> list) {
    for (final PdbCompactFragment element : list) {
      addElement(element);
    }
  }

  public final void addElement(final PdbCompactFragment element) {
    final MoleculeType moleculeType = element.getMoleculeType();

    if (moleculeType == MoleculeType.RNA) {
      listRNAs.add(element);
    } else if (moleculeType == MoleculeType.PROTEIN) {
      listProteins.add(element);
    }
  }

  public final void removeElements(final Iterable<PdbCompactFragment> list) {
    for (final PdbCompactFragment element : list) {
      removeElement(element);
    }
  }

  public final void removeElement(final PdbCompactFragment element) {
    if (listRNAs.contains(element)) {
      listRNAs.remove(element);
    } else {
      listProteins.remove(element);
    }
  }

  public final boolean canAddElement(
      final PdbCompactFragment element, final boolean isSizeConstrained) {
    final MoleculeType moleculeType = element.getMoleculeType();
    if ((getSize() > 0) && (getElementAt(0).getMoleculeType() != moleculeType)) {
      return false;
    }

    if (isSizeConstrained) {
      final List<PdbCompactFragment> list =
          (moleculeType == MoleculeType.RNA) ? listRNAs : listProteins;
      if ((!list.isEmpty()) && (list.get(0).getResidues().size() != element.getResidues().size())) {
        return false;
      }
    }

    return true;
  }

  @Override
  public final int getSize() {
    return (isRNA ? listRNAs.size() : 0) + (isProtein ? listProteins.size() : 0);
  }

  @Override
  public final PdbCompactFragment getElementAt(final int i) {
    if (isRNA) {
      if (i < listRNAs.size()) {
        return listRNAs.get(i);
      }
      return listProteins.get(i - listRNAs.size());
    }
    return listProteins.get(i);
  }
}
