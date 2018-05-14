package pl.poznan.put.datamodel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.DistanceMatrix;
import pl.poznan.put.interfaces.ExportFormat;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;

public class ProcessingResult implements Clusterable, Visualizable, Exportable {
  private final Clusterable clusterable;
  private final Visualizable visualizable;
  private final Exportable exportable;

  public ProcessingResult(
      Clusterable clusterable, Visualizable visualizable, Exportable exportable) {
    super();
    this.clusterable = clusterable;
    this.visualizable = visualizable;
    this.exportable = exportable;
  }

  public ProcessingResult(Object object) {
    super();
    clusterable = (Clusterable) (object instanceof Clusterable ? object : null);
    visualizable = (Visualizable) (object instanceof Visualizable ? object : null);
    exportable = (Exportable) (object instanceof Exportable ? object : null);
  }

  public static ProcessingResult emptyInstance() {
    return new ProcessingResult(null, null, null);
  }

  public boolean canCluster() {
    return clusterable != null;
  }

  public boolean canVisualize() {
    return visualizable != null;
  }

  public boolean canExport() {
    return exportable != null;
  }

  @Override
  public DistanceMatrix getDataForClustering() {
    return clusterable.getDataForClustering();
  }

  @Override
  public SVGDocument visualize() {
    return visualizable.visualize();
  }

  @Override
  public void visualize3D() {
    visualizable.visualize3D();
  }

  @Override
  public void export(OutputStream stream) throws IOException {
    exportable.export(stream);
  }

  @Override
  public ExportFormat getExportFormat() {
    return exportable.getExportFormat();
  }

  @Override
  public File suggestName() {
    return exportable.suggestName();
  }
}
