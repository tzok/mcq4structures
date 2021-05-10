package pl.poznan.put.datamodel;

import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.types.DistanceMatrix;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class ProcessingResult implements Clusterable, Visualizable, Exportable {
  private final Object object;

  public ProcessingResult(final Object object) {
    super();
    this.object = object;
  }

  public static ProcessingResult emptyInstance() {
    return new ProcessingResult(new Object());
  }

  public final boolean canCluster() {
    return object instanceof Clusterable;
  }

  public final boolean canVisualize() {
    return object instanceof Visualizable;
  }

  public final boolean canExport() {
    return object instanceof Exportable;
  }

  @Override
  public final DistanceMatrix distanceMatrix() {
    if (!(object instanceof Clusterable)) {
      throw new IllegalArgumentException("Processing result not clusterable");
    }
    return ((Clusterable) object).distanceMatrix();
  }

  @Override
  public final SVGDocument visualize() {
    if (!(object instanceof Visualizable)) {
      throw new IllegalArgumentException("Processing result not visualizable");
    }
    return ((Visualizable) object).visualize();
  }

  @Override
  public final void visualize3D() {
    if (!(object instanceof Visualizable)) {
      throw new IllegalArgumentException("Processing result not visualizable");
    }
    ((Visualizable) object).visualize3D();
  }

  @Override
  public final void export(final OutputStream stream) throws IOException {
    if (!(object instanceof Exportable)) {
      throw new IllegalArgumentException("Processing result not exportable");
    }
    ((Exportable) object).export(stream);
  }

  @Override
  public final File suggestName() {
    if (!(object instanceof Exportable)) {
      throw new IllegalArgumentException("Processing result not exportable");
    }
    return ((Exportable) object).suggestName();
  }
}
