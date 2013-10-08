package pl.poznan.put.cs.bioserver.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Map;

import javax.swing.JPanel;

import pl.poznan.put.cs.bioserver.beans.ComparisonLocal;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Angle;
import pl.poznan.put.cs.bioserver.torsion.AngleAverageAll;

class Colorbar extends JPanel {
    private static final long serialVersionUID = -2199465714158200574L;
    private float[] green;
    private ComparisonLocal local;
    private float[] red;
    private double max;
    private double min;

    Colorbar(ComparisonLocal local) {
        super();
        this.local = local;

        green = Color.RGBtoHSB(0, 255, 0, null);
        red = Color.RGBtoHSB(255, 0, 0, null);
        min = 0;
        max = Math.PI;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        assert g != null;

        Dimension size = getSize();
        int width = size.width / local.getTicks().size();
        int height = size.height;

        Map<String, Angle> angles = local.getAngles();
        Angle average =
                angles.get(AngleAverageAll.getInstance().getAngleName());
        int i = 0;
        for (double delta : average.getDeltas()) {
            g.setColor(getColor(delta));
            g.fillRect(i * width, 0, width, height - 1);
            g.setColor(Color.BLACK);
            g.drawRect(i * width, 0, width, height - 1);
            i++;
        }
    }

    void setMinMax(double min, double max) {
        this.min = min;
        this.max = max;
        repaint();
    }

    private Color getColor(double delta) {
        double hue =
                (delta - min) / (max - min) * (red[0] - green[0]) + green[0];
        return Color.getHSBColor((float) hue, 0.75f, 0.75f);
    }
}
