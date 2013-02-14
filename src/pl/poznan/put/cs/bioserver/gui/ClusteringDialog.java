package pl.poznan.put.cs.bioserver.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import pl.poznan.put.cs.bioserver.clustering.HierarchicalPlot;
import pl.poznan.put.cs.bioserver.clustering.KMedoidsPlot;

class ClusteringDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    ClusteringDialog(final String[] structureNames,
            final double[][] comparisonResults) {
        super();

        final JRadioButton hierarchical = new JRadioButton("hierarchical", true);
        JRadioButton kmedoids = new JRadioButton("k-medoids", false);
        ButtonGroup group = new ButtonGroup();
        group.add(hierarchical);
        group.add(kmedoids);

        final JComboBox<String> linkage = new JComboBox<>(new String[] {
                "Single", "Complete", "Average" });
        final JComboBox<String> method = new JComboBox<>(new String[] { "PAM",
                "PAMSIL" });
        method.setEnabled(false);
        final JCheckBox findBestK = new JCheckBox("Find best k?", true);
        findBestK.setEnabled(false);
        final JSpinner kspinner = new JSpinner();
        kspinner.setModel(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
        kspinner.setEnabled(false);

        JButton ok = new JButton("Ok");
        JButton close = new JButton("Close");

        Container container = getContentPane();
        container.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        container.add(hierarchical, c);

        c.gridx = 1;
        c.gridwidth = 3;
        container.add(linkage, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        container.add(kmedoids, c);

        c.gridx = 1;
        container.add(method, c);

        c.gridx = 2;
        container.add(findBestK, c);

        c.gridx = 3;
        container.add(kspinner, c);

        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        container.add(ok, c);

        c.gridx = 2;
        container.add(close, c);

        ActionListener radioActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean isHierarchical = hierarchical.isSelected();
                linkage.setEnabled(isHierarchical);
                method.setEnabled(!isHierarchical);
                findBestK.setEnabled(!isHierarchical);
                kspinner.setEnabled(!isHierarchical && !findBestK.isSelected());
            }
        };
        hierarchical.addActionListener(radioActionListener);
        kmedoids.addActionListener(radioActionListener);
        findBestK.addActionListener(radioActionListener);

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame plot;
                if (hierarchical.isSelected()) {
                    plot = new HierarchicalPlot(comparisonResults,
                            structureNames, linkage.getSelectedIndex());
                } else {
                    int k;
                    if (findBestK.isSelected()) {
                        k = 0;
                    } else {
                        k = (Integer) kspinner.getValue();
                        if (k > comparisonResults.length) {
                            JOptionPane.showMessageDialog(null,
                                    "k in k-medoids must be less or equal "
                                            + "to the number of input "
                                            + "structures", "Error!",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    plot = new KMedoidsPlot(comparisonResults, structureNames,
                            k, (String) method.getSelectedItem());
                }
                plot.setVisible(true);
            }
        });

        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        pack();
        int width = getPreferredSize().width;
        int height = getPreferredSize().height;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - width;
        int y = screenSize.height - height;
        setSize(width, height);
        setLocation(x / 2, y / 2);

        setTitle("MCQ4Structures: clustering method");
    }
}
