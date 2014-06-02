package pl.poznan.put.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;

import pl.poznan.put.structure.Residue;
import pl.poznan.put.structure.Sequence;

public class ColorbarTicks extends JPanel {
    private static final long serialVersionUID = 2174544811162082541L;

    private final Sequence sequence;

    ColorbarTicks(Sequence sequence) {
        super();
        this.sequence = sequence;

        Dimension size = new JLabel(sequence.toString() + sequence.toString()).getPreferredSize();
        setPreferredSize(size);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);

        Dimension size = getSize();
        int length = sequence.getSize();
        int width = size.width / length;
        int height = size.height;

        for (int i = 0; i < sequence.getSize(); i++) {
            Residue residue = sequence.getResidue(i);
            g.drawString(Character.toString(residue.getResidueNameOneLetter()),
                    i * width, height / 2 + 6);
        }
    }
}
