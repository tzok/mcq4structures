package pl.poznan.put.cs.bioserver.gui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.UIManager;

import org.apache.commons.math3.stat.StatUtils;

import pl.poznan.put.cs.bioserver.beans.ComparisonLocal;
import pl.poznan.put.cs.bioserver.beans.ComparisonLocalMulti;
import sun.font.FontDesignMetrics;

public class DialogColorbar extends JDialog {
    private static final long serialVersionUID = 2659329749184089277L;

    public DialogColorbar(final ComparisonLocalMulti localMulti) {
        super();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        final List<Colorbar> list = new ArrayList<>();
        final List<ComparisonLocal> results = localMulti.getResults();
        for (ComparisonLocal local : results) {
            c.gridx = 0;
            c.weightx = 1;
            c.fill = GridBagConstraints.BOTH;
            Colorbar colorbar = new Colorbar(local);
            list.add(colorbar);
            add(colorbar, c);
            c.gridx++;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            add(new JLabel(local.getTitle()), c);
            c.gridy++;
        }

        final JCheckBox checkRelative = new JCheckBox("Relative");
        c.gridx = 1;
        add(checkRelative, c);

        setTitle("Colorbar");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        FontMetrics metrics = FontDesignMetrics.getMetrics(UIManager.getFont("Label.font"));
        ComparisonLocal any = results.get(0);
        int n = any.getTicks().length;

        int width = n * 8 + metrics.stringWidth(any.getTitle()) + 16;
        if (width >= screenSize.width) {
            width -= n * 4;
        }
        int height = metrics.getHeight() * (results.size() + 1) + 16;

        setPreferredSize(new Dimension(width, height));
        int x = screenSize.width - width;
        int y = screenSize.height - height;
        setLocation(x / 2, y / 2);
        pack();

        checkRelative.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                double min = 0;
                double max = Math.PI;

                if (checkRelative.isSelected()) {
                    double lmin = Math.PI;
                    double lmax = 0;
                    for (ComparisonLocal local : results) {
                        double[] deltas = local.getAngles().get("AVERAGE").getDeltas();
                        lmin = Math.min(lmin, StatUtils.min(deltas));
                        lmax = Math.max(lmax, StatUtils.max(deltas));
                    }
                    min = lmin;
                    max = lmax;
                }

                for (Colorbar colorbar : list) {
                    colorbar.setMinMax(min, max);
                }
                repaint();
            }
        });
    }
}
