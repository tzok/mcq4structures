package pl.poznan.put.datamodel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.types.DistanceMatrix;

@RequiredArgsConstructor
public class ProcessingResult implements Clusterable, Visualizable, Exportable {
  private final @Nullable Clusterable clusterable;
  private final @Nullable Visualizable visualizable;
  private final @Nullable Exportable exportable;

  public ProcessingResult(final Object object) {
    super();
    clusterable = (Clusterable) ((object instanceof Clusterable) ? object : null);
    visualizable = (Visualizable) ((object instanceof Visualizable) ? object : null);
    exportable = (Exportable) ((object instanceof Exportable) ? object : null);
  }

  public static ProcessingResult emptyInstance() {
    return new ProcessingResult(null, null, null);
  }

  public final boolean canCluster() {
    return clusterable != null;
  }

  public final boolean canVisualize() {
    return visualizable != null;
  }

  public final boolean canExport() {
    return exportable != null;
  }

  @Override
  public final DistanceMatrix getDataForClustering() {
    if (clusterable == null) {
      throw new IllegalArgumentException("Processing result not clusterable");
    }
    return clusterable.getDataForClustering();
  }

  @Override
  public final SVGDocument visualize() {
    if (visualizable == null) {
      throw new IllegalArgumentException("Processing result not visualizable");
    }
    return visualizable.visualize();
  }

  @Override
  public final void visualize3D() {
    if (visualizable == null) {
      throw new IllegalArgumentException("Processing result not visualizable");
    }
    visualizable.visualize3D();
  }

  @Override
  public final void export(final OutputStream stream) throws IOException {
    if (exportable == null) {
      throw new IllegalArgumentException("Processing result not exportable");
    }
    exportable.export(stream);
  }

  @Override
  public final File suggestName() {
    if (exportable == null) {
      throw new IllegalArgumentException("Processing result not exportable");
    }
    return exportable.suggestName();
  }
}
