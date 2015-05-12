package pl.poznan.put.gui;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.axes.layout.IAxeLayout;
import org.jzy3d.plot3d.primitives.axes.layout.providers.RegularTickProvider;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.TickLabelMap;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class Surface3D extends AbstractAnalysis {
    private final double[][] matrix;
    private final String[] namesX;
    private final String[] namesY;

    public Surface3D(double[][] matrix, String[] namesX, String[] namesY) {
        super();
        this.matrix = matrix;
        this.namesX = namesX;
        this.namesY = namesY;
    }

    @Override
    public void init() throws Exception {
        // Define a function to plot
        Mapper mapper = new Mapper() {
            @Override
            public double f(double x, double y) {
                int i = (int) Math.round(x);
                int j = (int) Math.round(y);
                int n = matrix.length;
                i = Math.max(Math.min(i, n - 1), 0);
                j = Math.max(Math.min(j, n - 1), 0);
                return matrix[i][j];
            }
        };

        int n = matrix.length;
        Range range = new Range(0, n);
        Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, n, range, n), mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), 0, Math.PI, new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
        IAxeLayout axeLayout = chart.getAxeLayout();
        axeLayout.setXTickProvider(new RegularTickProvider(n));
        axeLayout.setXTickRenderer(Surface3D.convertNamesToTicks(namesX));
        axeLayout.setYTickProvider(new RegularTickProvider(n));
        axeLayout.setYTickRenderer(Surface3D.convertNamesToTicks(namesY));
        chart.getScene().getGraph().add(surface);
    }

    private static TickLabelMap convertNamesToTicks(String[] names) {
        TickLabelMap map = new TickLabelMap();
        for (int i = 0; i < names.length; i++) {
            map.register(i, names[i]);
        }
        return map;
    }
}
