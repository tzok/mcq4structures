package pl.poznan.put.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import pl.poznan.put.comparison.ModelsComparisonResult;
import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.torsion.TorsionAngle;

class Colorbar extends JPanel {
    private static final long serialVersionUID = -2199465714158200574L;
    private static final float[] GREEN_HSB = Color.RGBtoHSB(0, 255, 0, null);
    private static final float[] RED_HSB = Color.RGBtoHSB(255, 0, 0, null);

    private final ModelsComparisonResult result;
    private final int index;
    private final TorsionAngle torsionAngle;

    private double max = Math.PI;
    private double min = 0;

    Colorbar(ModelsComparisonResult result, int index) {
        super();
        this.result = result;
        this.index = index;
        torsionAngle = result.getTorsionAngle();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Dimension size = getSize();
        int width = size.width / result.getFragmentSize();
        int height = size.height;

        FragmentComparison residueResults = result.getFragmentComparison(index);

        for (int i = 0; i < residueResults.getSize(); i++) {
            ResidueComparison residueResult = residueResults.getResidueComparison(i);
            double delta = residueResult.getAngleDelta(torsionAngle).getDelta();
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
        if (delta < min) {
            delta = min;
        }

        if (delta > max) {
            delta = max;
        }

        double hue = (delta - min) / (max - min) * (RED_HSB[0] - GREEN_HSB[0])
                + GREEN_HSB[0];
        return Color.getHSBColor((float) hue, 0.75f, 0.75f);
    }
}
