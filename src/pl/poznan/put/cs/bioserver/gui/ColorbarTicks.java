package pl.poznan.put.cs.bioserver.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ColorbarTicks extends JPanel {
    private static final long serialVersionUID = 2174544811162082541L;
    private String sequence;

    ColorbarTicks(String sequence) {
        super();
        this.sequence = sequence;

        Dimension size = new JLabel(sequence).getPreferredSize();
        setPreferredSize(size);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        assert g != null;

        Dimension size = getSize();
        int width = size.width / sequence.length();
        int height = size.height;

        g.setColor(Color.BLACK);
        for (int i = 0; i < sequence.length(); i++) {
            g.drawString(sequence.substring(i, i + 1), i * width + width / 4,
                    height / 2 + 6);
        }
    }
}
