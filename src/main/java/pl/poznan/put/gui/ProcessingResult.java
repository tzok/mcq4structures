package pl.poznan.put.gui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.svg.SVGHelper;

public class ProcessingResult implements Clusterable, Visualizable, Exportable {
    public static ProcessingResult emptyInstance() {
        return new ProcessingResult(null, null, null, Collections.singletonList(SVGHelper.emptyDocument()));
    }

    private final Clusterable clusterable;
    private final Visualizable visualizable;
    private final Exportable exportable;
    private final List<SVGDocument> visualizations;

    public ProcessingResult(Clusterable clusterable, Visualizable visualizable,
            Exportable exportable, List<SVGDocument> visualization) {
        super();
        this.clusterable = clusterable;
        this.visualizable = visualizable;
        this.exportable = exportable;
        this.visualizations = visualization;
    }

    public ProcessingResult(Object object, List<SVGDocument> visualization) {
        super();
        this.clusterable = (Clusterable) (object instanceof Clusterable ? object : null);
        this.visualizable = (Visualizable) (object instanceof Visualizable ? object : null);
        this.exportable = (Exportable) (object instanceof Exportable ? object : null);
        this.visualizations = visualization;
    }

    public ProcessingResult(Object object) {
        super();
        this.clusterable = (Clusterable) (object instanceof Clusterable ? object : null);
        this.visualizable = (Visualizable) (object instanceof Visualizable ? object : null);
        this.exportable = (Exportable) (object instanceof Exportable ? object : null);
        this.visualizations = Collections.singletonList(SVGHelper.emptyDocument());
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

    public List<SVGDocument> getVisualizations() {
        return visualizations;
    }

    @Override
    public void cluster() {
        clusterable.cluster();
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
