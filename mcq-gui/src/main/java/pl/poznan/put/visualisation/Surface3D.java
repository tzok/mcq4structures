package pl.poznan.put.visualisation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jcolorbrewer.ColorBrewer;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.keyboard.screenshot.AWTScreenshotKeyController;
import org.jzy3d.chart.controllers.keyboard.screenshot.IScreenshotKeyController;
import org.jzy3d.chart.controllers.mouse.AWTMouseUtilities;
import org.jzy3d.chart.controllers.mouse.camera.AWTCameraMouseController;
import org.jzy3d.chart.controllers.mouse.camera.ICameraMouseController;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.AbstractColorMap;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.axes.layout.IAxeLayout;
import org.jzy3d.plot3d.primitives.axes.layout.providers.ITickProvider;
import org.jzy3d.plot3d.primitives.axes.layout.providers.RegularTickProvider;
import org.jzy3d.plot3d.primitives.axes.layout.providers.SmartTickProvider;
import org.jzy3d.plot3d.primitives.axes.layout.providers.StaticTickProvider;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.TickLabelMap;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.SortedSet;

class Surface3D extends AbstractAnalysis {
  private static final java.awt.Color[] PALETTE = ColorBrewer.YlOrRd.getColorPalette(4);
  private static final java.awt.Color IDENTICAL = Surface3D.PALETTE[0];
  private static final java.awt.Color SIMILAR = Surface3D.PALETTE[1];
  private static final java.awt.Color DIFFERENT = Surface3D.PALETTE[2];
  private static final java.awt.Color OPPOSITE = Surface3D.PALETTE[3];

  private static final AbstractColorMap JZY3D_COLOR_MAP =
      new AbstractColorMap() {
        private final org.jzy3d.colors.Color identical =
            new org.jzy3d.colors.Color(
                Surface3D.IDENTICAL.getRed(),
                Surface3D.IDENTICAL.getGreen(),
                Surface3D.IDENTICAL.getBlue());
        private final org.jzy3d.colors.Color similar =
            new org.jzy3d.colors.Color(
                Surface3D.SIMILAR.getRed(),
                Surface3D.SIMILAR.getGreen(),
                Surface3D.SIMILAR.getBlue());
        private final org.jzy3d.colors.Color different =
            new org.jzy3d.colors.Color(
                Surface3D.DIFFERENT.getRed(),
                Surface3D.DIFFERENT.getGreen(),
                Surface3D.DIFFERENT.getBlue());
        private final org.jzy3d.colors.Color opposite =
            new org.jzy3d.colors.Color(
                Surface3D.OPPOSITE.getRed(),
                Surface3D.OPPOSITE.getGreen(),
                Surface3D.OPPOSITE.getBlue());

        @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
        @Override
        public org.jzy3d.colors.Color getColor(
            final double x, final double y, final double z, final double zMin, final double zMax) {
          final double zRel = processRelativeZValue(z, zMin, zMax);
          if (zRel < 0.15) {
            return identical;
          } else if (zRel < 0.3) {
            return similar;
          } else if (zRel < 0.6) {
            return different;
          } else {
            return opposite;
          }
        }
      };

  private final AWTChartComponentFactory factory = new MyAWTChartComponentFactory();

  private final double minZ;
  private final double maxZ;
  private final ITickProvider providerZ;
  private final ITickRenderer rendererZ;

  private final String name;
  private final double[][] matrix;
  private final List<String> ticksX;
  private final List<String> ticksY;
  private final String labelX;
  private final String labelY;
  private final String labelZ;
  private final boolean showAllTicksX;
  private final boolean showAllTicksY;

  Surface3D(
      final String name,
      final double[][] matrix,
      final List<String> ticksX,
      final List<String> ticksY,
      final NavigableMap<Double, String> valueTickZ,
      final String labelX,
      final String labelY,
      final String labelZ,
      final boolean showAllTicksX,
      final boolean showAllTicksY) {
    super();
    this.name = name;
    this.matrix = matrix.clone();
    this.ticksX = new ArrayList<>(ticksX);
    this.ticksY = new ArrayList<>(ticksY);
    this.labelX = labelX;
    this.labelY = labelY;
    this.labelZ = labelZ;
    this.showAllTicksX = showAllTicksX;
    this.showAllTicksY = showAllTicksY;

    final SortedSet<Double> sortedSet = valueTickZ.navigableKeySet();
    final double[] array = ArrayUtils.toPrimitive(sortedSet.toArray(new Double[0]));
    final ITickProvider staticTickProvider = new StaticTickProvider(array);
    final TickLabelMap tickLabelMap = new TickLabelMap();
    tickLabelMap.getMap().putAll(valueTickZ);

    minZ = sortedSet.first();
    maxZ = sortedSet.last();
    providerZ = staticTickProvider;
    rendererZ = tickLabelMap;
  }

  @Override
  public final String getName() {
    return name;
  }

  @Override
  public final void init() {
    // Define a function to plot
    final Mapper mapper =
        new Mapper() {
          @Override
          public double f(final double v, final double v1) {
            final int i = Math.max(Math.min((int) Math.round(v), ticksX.size() - 1), 0);
            final int j = Math.max(Math.min((int) Math.round(v1), ticksY.size() - 1), 0);
            return matrix[i][j];
          }
        };

    final Range rangeX = new Range(0, ticksX.size());
    final Range rangeY = new Range(0, ticksY.size());
    final OrthonormalGrid orthonormalGrid =
        new OrthonormalGrid(rangeX, ticksX.size(), rangeY, ticksY.size());

    final Shape surface = Builder.buildOrthonormal(orthonormalGrid, mapper);
    surface.setColorMapper(
        new ColorMapper(Surface3D.JZY3D_COLOR_MAP, minZ, maxZ, new Color(1.0F, 1.0F, 1.0F, 0.5f)));
    surface.setFaceDisplayed(true);
    surface.setWireframeDisplayed(true);
    surface.setWireframeColor(Color.GRAY);

    chart = new Chart(factory, Quality.Intermediate, "awt");
    final IAxeLayout axeLayout = chart.getAxeLayout();
    axeLayout.setXTickProvider(createProviderXY(true, showAllTicksX));
    axeLayout.setXTickRenderer(createRendererXY(true));
    axeLayout.setYTickProvider(createProviderXY(false, showAllTicksY));
    axeLayout.setYTickRenderer(createRendererXY(false));
    axeLayout.setZTickProvider(providerZ);
    axeLayout.setZTickRenderer(rendererZ);
    axeLayout.setXAxeLabel(labelX);
    axeLayout.setYAxeLabel(labelY);
    axeLayout.setZAxeLabel(labelZ);

    chart
        .getView()
        .setBoundManual(
            new BoundingBox3d(0, ticksX.size(), 0, ticksY.size(), (float) minZ, (float) maxZ));
    chart.addDrawable(surface);
  }

  private ITickProvider createProviderXY(final boolean isX, final boolean showAllTicks) {
    if (showAllTicks) {
      return new RegularTickProvider(isX ? ticksX.size() : ticksY.size());
    }
    return new SmartTickProvider(isX ? ticksX.size() : ticksY.size());
  }

  private ITickRenderer createRendererXY(final boolean isX) {
    final List<String> ticks = isX ? ticksX : ticksY;
    return x -> {
      final int i = Math.max(Math.min((int) Math.floor(x), ticks.size() - 1), 0);
      return ticks.get(i);
    };
  }

  private static class MyAWTChartComponentFactory extends AWTChartComponentFactory {
    @Override
    public final ICameraMouseController newMouseController(final Chart chart) {
      return new MyAWTCameraMouseController(chart);
    }

    @Override
    public final IScreenshotKeyController newScreenshotKeyController(final Chart chart) {
      final String nowAsIso = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date());
      final File tmpdir = FileUtils.getTempDirectory();
      final File screenshot = new File(tmpdir, nowAsIso + ".png");
      final IScreenshotKeyController controller =
          new AWTScreenshotKeyController(chart, screenshot.getAbsolutePath());
      controller.addListener(new MyIScreenshotEventListener());
      return controller;
    }

    private static class MyAWTCameraMouseController extends AWTCameraMouseController {
      MyAWTCameraMouseController(final Chart c) {
        super(c);
      }

      @Override
      public final void mouseDragged(final MouseEvent e) {
        if (AWTMouseUtilities.isRightDown(e)) {
          return;
        }
        super.mouseDragged(e);
      }

      @Override
      public void mouseWheelMoved(final MouseWheelEvent e) {
        // do nothing
      }
    }

    private static class MyIScreenshotEventListener
        implements IScreenshotKeyController.IScreenshotEventListener {
      @Override
      public final void doneScreenshot(final String s) {
        System.out.println("Screenshot: " + s);
      }

      @Override
      public final void failedScreenshot(final String s, final Exception e) {
        System.err.println("Failed to save screenshot");
      }
    }
  }
}
