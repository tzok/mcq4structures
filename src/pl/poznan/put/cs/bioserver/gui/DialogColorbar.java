package pl.poznan.put.cs.bioserver.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.math3.stat.StatUtils;

import pl.poznan.put.cs.bioserver.beans.ComparisonLocal;
import pl.poznan.put.cs.bioserver.beans.ComparisonLocalMulti;
import pl.poznan.put.cs.bioserver.torsion.AngleAverageAll;

public class DialogColorbar extends JDialog {
    private static final long serialVersionUID = 2659329749184089277L;

    JCheckBox checkRelative = new JCheckBox("Use real min/max");
    JTextField editMin = new JTextField("0", 8);
    JTextField editMax = new JTextField("180", 8);
    List<Colorbar> listColorbars = new ArrayList<>();
    List<ComparisonLocal> listResults;

    public DialogColorbar(ComparisonLocalMulti localMulti) {
        super();
        listResults = localMulti.getResults();

        setTitle("Colorbar");
        setLayout(new GridBagLayout());

        editMin.setMaximumSize(new Dimension(128, 32));
        editMax.setMaximumSize(new Dimension(128, 32));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel("Min:"));
        panel.add(editMin);
        panel.add(new JLabel("Max:"));
        panel.add(editMax);
        panel.add(checkRelative);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        add(panel, c);

        c.gridy = 1;
        add(new ColorbarTicks(localMulti.getReferenceSequence()), c);

        for (ComparisonLocal local : listResults) {
            Colorbar colorbar = new Colorbar(local);
            listColorbars.add(colorbar);

            c.gridx = 0;
            c.gridy += 1;
            c.weightx = 1;
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            add(colorbar, c);

            c.gridx += 1;
            c.weightx = 0;
            c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            add(new JLabel(local.getTitle()), c);
        }

        c.gridx = 0;
        c.gridy += 1;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        add(new ColorbarTicks(localMulti.getReferenceSequence()), c);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        setSize(size.width * 2 / 3, size.height * 2 / 3);
        setLocation(size.width / 6, size.height / 6);

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double min = Math.toRadians(Double.valueOf(editMin.getText()));
                double max = Math.toRadians(Double.valueOf(editMax.getText()));

                for (Colorbar colorbar : listColorbars) {
                    colorbar.setMinMax(min, max);
                }
            }
        };
        editMin.addActionListener(actionListener);
        editMax.addActionListener(actionListener);

        checkRelative.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                double min = 0;
                double max = Math.PI;

                if (checkRelative.isSelected()) {
                    double lmin = Math.PI;
                    double lmax = 0;
                    for (ComparisonLocal local : listResults) {
                        double[] deltas =
                                local.getAngles()
                                        .get(AngleAverageAll.getInstance()
                                                .getAngleName()).getDeltas();
                        lmin = Math.min(lmin, StatUtils.min(deltas));
                        lmax = Math.max(lmax, StatUtils.max(deltas));
                    }
                    min = lmin;
                    max = lmax;
                }

                for (Colorbar colorbar : listColorbars) {
                    colorbar.setMinMax(min, max);
                }

                DecimalFormat format = new DecimalFormat("0.000");
                DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
                symbols.setDecimalSeparator('.');
                format.setDecimalFormatSymbols(symbols);

                editMin.setText(format.format(Math.toDegrees(min)));
                editMax.setText(format.format(Math.toDegrees(max)));
                editMin.setEnabled(!checkRelative.isSelected());
                editMax.setEnabled(!checkRelative.isSelected());

                repaint();
            }
        });
    }
}
