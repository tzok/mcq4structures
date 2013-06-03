package pl.poznan.put.cs.bioserver.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import pl.poznan.put.cs.bioserver.beans.ComparisonLocal;

public class Colorbar extends JPanel {
    private static final long serialVersionUID = -2199465714158200574L;
    private ComparisonLocal local;
    private float[] green;
    private float[] red;
    double min;
    double max;

    public Colorbar(ComparisonLocal local) {
        super();
        this.local = local;

        green = Color.RGBtoHSB(0, 255, 0, null);
        red = Color.RGBtoHSB(255, 0, 0, null);
        min = 0;
        max = Math.PI;

        Dimension dimension = new Dimension(local.getTicks().length * 32, 64);
        setMinimumSize(dimension);
        setPreferredSize(dimension);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int i = 0;
        for (double delta : local.getAngles().get("AVERAGE").getDeltas()) {
            g.setColor(getColor(delta));
            g.fillRect(i * 32, 0, 32, 64);
            g.setColor(Color.BLACK);
            g.drawRect(i * 32, 0, 32, 64);
            i++;
        }
    }

    private Color getColor(double delta) {
        double hue = (delta - min) / (max - min) * (red[0] - green[0])
                + green[0];
        return Color.getHSBColor((float) hue, 0.75f, 0.75f);
    }

    public void setMinMax(double min, double max) {
        this.min = min;
        this.max = max;
        repaint();
    }
}
