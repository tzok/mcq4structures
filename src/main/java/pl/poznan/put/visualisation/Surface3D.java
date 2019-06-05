package pl.poznan.put.visualisation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.SortedSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jcolorbrewer.ColorBrewer;
import org.jmol.c.PAL;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.keyboard.screenshot.AWTScreenshotKeyController;
import org.jzy3d.chart.controllers.keyboard.screenshot.IScreenshotKeyController;
import org.jzy3d.chart.controllers.keyboard.screenshot.IScreenshotKeyController.IScreenshotEventListener;
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

public class Surface3D extends AbstractAnalysis {
  private static final java.awt.Color[] PALETTE = ColorBrewer.YlOrRd.getColorPalette(4);
  private static final java.awt.Color IDENTICAL = PALETTE[0];
  private static final java.awt.Color SIMILAR = PALETTE[1];
  private static final java.awt.Color DIFFERENT = PALETTE[2];
  private static final java.awt.Color OPPOSITE = PALETTE[3];

  private static final AbstractColorMap JZY3D_COLOR_MAP =
      new AbstractColorMap() {
        private final org.jzy3d.colors.Color identical =
            new org.jzy3d.colors.Color(
                IDENTICAL.getRed(), IDENTICAL.getGreen(), IDENTICAL.getBlue());
        private final org.jzy3d.colors.Color similar =
            new org.jzy3d.colors.Color(SIMILAR.getRed(), SIMILAR.getGreen(), SIMILAR.getBlue());
        private final org.jzy3d.colors.Color different =
            new org.jzy3d.colors.Color(
                DIFFERENT.getRed(), DIFFERENT.getGreen(), DIFFERENT.getBlue());
        private final org.jzy3d.colors.Color opposite =
            new org.jzy3d.colors.Color(OPPOSITE.getRed(), OPPOSITE.getGreen(), OPPOSITE.getBlue());

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

  private final AWTChartComponentFactory factory =
      new AWTChartComponentFactory() {
        @Override
        public ICameraMouseController newMouseController(Chart c) {
          return new AWTCameraMouseController(c) {
            @Override
            public void mouseDragged(MouseEvent e) {
              if (AWTMouseUtilities.isRightDown(e)) {
                return;
              }
              super.mouseDragged(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
              // do nothing
            }
          };
        }

        @Override
        public IScreenshotKeyController newScreenshotKeyController(final Chart chart) {
          String nowAsIso = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date());
          File tmpdir = new File(System.getProperty("java.io.tmpdir"));
          File screenshot = new File(tmpdir, nowAsIso + ".png");
          AWTScreenshotKeyController controller =
              new AWTScreenshotKeyController(chart, screenshot.getAbsolutePath());
          controller.addListener(
              new IScreenshotEventListener() {
                @Override
                public void doneScreenshot(final String s) {
                  System.out.println("Screenshot: " + s);
                }

                @Override
                public void failedScreenshot(final String s, final Exception e) {
                  System.err.println("Failed to save screenshot");
                }
              });
          return controller;
        }
      };

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

  public Surface3D(
      String name,
      double[][] matrix,
      List<String> ticksX,
      List<String> ticksY,
      NavigableMap<Double, String> valueTickZ,
      String labelX,
      String labelY,
      String labelZ,
      boolean showAllTicksX,
      boolean showAllTicksY) {
    super();
    this.name = name;
    this.matrix = matrix.clone();
    this.ticksX = ticksX;
    this.ticksY = ticksY;
    this.labelX = labelX;
    this.labelY = labelY;
    this.labelZ = labelZ;
    this.showAllTicksX = showAllTicksX;
    this.showAllTicksY = showAllTicksY;

    SortedSet<Double> sortedSet = valueTickZ.navigableKeySet();
    double[] array = ArrayUtils.toPrimitive(sortedSet.toArray(new Double[sortedSet.size()]));
    StaticTickProvider staticTickProvider = new StaticTickProvider(array);
    TickLabelMap tickLabelMap = new TickLabelMap();
    tickLabelMap.getMap().putAll(valueTickZ);

    minZ = sortedSet.first();
    maxZ = sortedSet.last();
    providerZ = staticTickProvider;
    rendererZ = tickLabelMap;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void init() throws Exception {
    // Define a function to plot
    Mapper mapper =
        new Mapper() {
          @Override
          public double f(double x, double y) {
            int i = Math.max(Math.min((int) Math.round(x), ticksX.size() - 1), 0);
            int j = Math.max(Math.min((int) Math.round(y), ticksY.size() - 1), 0);
            return matrix[i][j];
          }
        };

    Range rangeX = new Range(0, ticksX.size());
    Range rangeY = new Range(0, ticksY.size());
    OrthonormalGrid orthonormalGrid =
        new OrthonormalGrid(rangeX, ticksX.size(), rangeY, ticksY.size());

    Shape surface = Builder.buildOrthonormal(orthonormalGrid, mapper);
    surface.setColorMapper(new ColorMapper(JZY3D_COLOR_MAP, minZ, maxZ, new Color(1, 1, 1, .5f)));
    surface.setFaceDisplayed(true);
    surface.setWireframeDisplayed(true);
    surface.setWireframeColor(Color.GRAY);

    chart = new Chart(factory, Quality.Intermediate, "awt");
    IAxeLayout axeLayout = chart.getAxeLayout();
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

  private ITickProvider createProviderXY(boolean isX, boolean showAllTicks) {
    if (showAllTicks) {
      return new RegularTickProvider(isX ? ticksX.size() : ticksY.size());
    }
    return new SmartTickProvider(isX ? ticksX.size() : ticksY.size());
  }

  private ITickRenderer createRendererXY(boolean isX) {
    final List<String> ticks = isX ? ticksX : ticksY;
    return new ITickRenderer() {
      @Override
      public String format(double x) {
        int i = Math.max(Math.min((int) Math.floor(x), ticks.size() - 1), 0);
        return ticks.get(i);
      }
    };
  }
}
