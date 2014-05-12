package pl.poznan.put.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.tuple.Pair;
import org.biojava.bio.structure.ResidueNumber;

public class ColorbarTicks extends JPanel {
    private static final long serialVersionUID = 2174544811162082541L;
    private Pair<String, List<ResidueNumber>> reference;

    ColorbarTicks(Pair<String, List<ResidueNumber>> pair) {
        super();
        reference = pair;

        Dimension size =
                new JLabel(pair.getLeft() + pair.getLeft()).getPreferredSize();
        setPreferredSize(size);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        assert g != null;

        String sequence = reference.getLeft();
        List<ResidueNumber> resids = reference.getRight();

        Dimension size = getSize();
        int length = reference.getLeft().length();
        int width = size.width / length;
        int height = size.height;

        g.setColor(Color.BLACK);
        for (int i = 0; i < length; i++) {
            g.drawString(sequence.substring(i, i + 1)
                    + resids.get(i).getSeqNum(), i * width, height / 2 + 6);
        }
    }
}
