package pl.poznan.put.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import pl.poznan.put.beans.ClusteringHierarchical;
import pl.poznan.put.beans.ClusteringPartitional;
import pl.poznan.put.clustering.ClustererHierarchical;
import pl.poznan.put.clustering.ClustererHierarchical.Cluster;
import pl.poznan.put.clustering.ClustererHierarchical.Linkage;
import pl.poznan.put.clustering.ClustererKMedoids;
import pl.poznan.put.clustering.ClustererKMedoids.Result;
import pl.poznan.put.clustering.ClustererKMedoids.ScoringFunction;
import pl.poznan.put.comparison.GlobalComparisonResultMatrix;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.utility.InvalidInputException;

public class DialogCluster extends JDialog {
    private static final long serialVersionUID = 1L;

    JButton buttonVisualize;
    JButton buttonVisualize3D;
    JButton buttonVisualizeHighQuality;
    GlobalComparisonResultMatrix comparisonGlobal;
    JCheckBox findBestK;
    JRadioButton hierarchical;
    JSpinner kspinner;
    JComboBox<Linkage> linkage;
    JComboBox<ScoringFunction> scoringFunction;

    public DialogCluster(GlobalComparisonResultMatrix comparisonGlobal) {
        super();

        this.comparisonGlobal = comparisonGlobal;

        hierarchical = new JRadioButton("hierarchical", true);
        JRadioButton kmedoids = new JRadioButton("k-medoids", false);
        ButtonGroup group = new ButtonGroup();
        group.add(hierarchical);
        group.add(kmedoids);

        linkage = new JComboBox<>(Linkage.values());
        scoringFunction = new JComboBox<>(
                ClustererKMedoids.getScoringFunctions());
        scoringFunction.setEnabled(false);
        findBestK = new JCheckBox("Find best k?", true);
        findBestK.setEnabled(false);
        kspinner = new JSpinner();
        kspinner.setModel(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
        kspinner.setEnabled(false);

        buttonVisualize = new JButton("Visualize");
        buttonVisualizeHighQuality = new JButton("Visualize (high quality)");
        buttonVisualize3D = new JButton("Visualize in 3D");
        buttonVisualize3D.setEnabled(false);
        JButton buttonClose = new JButton("Close");

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
        container.add(scoringFunction, c);

        c.gridx = 2;
        container.add(findBestK, c);

        c.gridx = 3;
        container.add(kspinner, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        container.add(buttonVisualize, c);
        c.gridx = 1;
        container.add(buttonVisualizeHighQuality, c);
        c.gridx = 2;
        container.add(buttonVisualize3D, c);
        c.gridx = 3;
        container.add(buttonClose, c);

        ActionListener radioActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean isHierarchical = hierarchical.isSelected();
                linkage.setEnabled(isHierarchical);
                scoringFunction.setEnabled(!isHierarchical);
                findBestK.setEnabled(!isHierarchical);
                kspinner.setEnabled(!isHierarchical && !findBestK.isSelected());
                buttonVisualize3D.setEnabled(!isHierarchical);
            }
        };
        hierarchical.addActionListener(radioActionListener);
        kmedoids.addActionListener(radioActionListener);
        findBestK.addActionListener(radioActionListener);

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                assert arg0 != null;

                try {
                    Object source = arg0.getSource();
                    Visualizable visualizable = getVisualizable();
                    if (source.equals(buttonVisualize)) {
                        visualizable.visualize();
                    } else if (source.equals(buttonVisualizeHighQuality)) {
                        visualizable.visualizeHighQuality();
                    } else { // source.equals(buttonVisualize3D)
                        visualizable.visualize3D();
                    }
                } catch (InvalidInputException e) {
                    JOptionPane.showMessageDialog(DialogCluster.this,
                            e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        buttonVisualize.addActionListener(listener);
        buttonVisualizeHighQuality.addActionListener(listener);
        buttonVisualize3D.addActionListener(listener);

        buttonClose.addActionListener(new ActionListener() {
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

    Visualizable getVisualizable() throws InvalidInputException {
        if (hierarchical.isSelected()) {
            final List<Cluster> clustering = ClustererHierarchical.hierarchicalClustering(
                    comparisonGlobal.getMatrix(),
                    (Linkage) linkage.getSelectedItem());

            return new Visualizable() {
                @Override
                public void visualizeHighQuality() {
                    // TODO Auto-generated method stub
                }

                @Override
                public void visualize3D() {
                    // TODO Auto-generated method stub
                }

                @Override
                public void visualize() {
                    HierarchicalPlot plot = new HierarchicalPlot(
                            comparisonGlobal, clustering);
                    plot.setVisible(true);
                }
            };
        }

        Integer k = (Integer) (findBestK.isSelected() ? null
                : kspinner.getValue());
        ClustererKMedoids clusterer = new ClustererKMedoids();
        Result medoids = clusterer.kMedoids(comparisonGlobal.getMatrix(),
                (ScoringFunction) scoringFunction.getSelectedItem(), k);

        return new Visualizable() {
            @Override
            public void visualizeHighQuality() {
                // TODO Auto-generated method stub

            }

            @Override
            public void visualize3D() {
                // TODO Auto-generated method stub
            }

            @Override
            public void visualize() {
                new KMedoidsPlot(comparisonGlobal, medoids);
            }
        };
    }
}
