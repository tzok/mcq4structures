package pl.poznan.put.comparison.local;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.immutables.value.Value;
import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.NonEditableDefaultTableModel;
import pl.poznan.put.utility.TabularExporter;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class MCQLocalResult implements LocalResult {
  @Override
  @Value.Parameter(order = 1)
  public abstract SelectionMatch selectionMatch();

  @Override
  @Value.Parameter(order = 2)
  public abstract List<MasterTorsionAngleType> angleTypes();
}
