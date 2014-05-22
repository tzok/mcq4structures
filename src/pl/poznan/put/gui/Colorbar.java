package pl.poznan.put.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import pl.poznan.put.common.TorsionAngle;
import pl.poznan.put.comparison.ModelsComparisonResult;
import pl.poznan.put.matching.ResidueComparisonResult;

class Colorbar extends JPanel {
    private static final long serialVersionUID = -2199465714158200574L;

    private final ModelsComparisonResult result;
    private final int index;
    private final TorsionAngle torsionAngle;

    private final float[] green = Color.RGBtoHSB(0, 255, 0, null);
    private final float[] red = Color.RGBtoHSB(255, 0, 0, null);

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

        List<ResidueComparisonResult> residueResults = result.getResidueResults(index);

        for (int i = 0; i < residueResults.size(); i++) {
            ResidueComparisonResult residueResult = residueResults.get(i);
            double delta = residueResult.getDelta(torsionAngle).getDelta();
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

        double hue = (delta - min) / (max - min) * (red[0] - green[0])
                + green[0];
        return Color.getHSBColor((float) hue, 0.75f, 0.75f);
    }
}
