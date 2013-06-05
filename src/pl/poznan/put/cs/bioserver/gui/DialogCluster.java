package pl.poznan.put.cs.bioserver.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

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

import org.eclipse.jdt.annotation.Nullable;

import com.sun.media.sound.InvalidDataException;

import pl.poznan.put.cs.bioserver.beans.ClusteringHierarchical;
import pl.poznan.put.cs.bioserver.beans.ClusteringPartitional;
import pl.poznan.put.cs.bioserver.beans.ComparisonGlobal;
import pl.poznan.put.cs.bioserver.beans.XMLSerializable;
import pl.poznan.put.cs.bioserver.clustering.Clusterer;
import pl.poznan.put.cs.bioserver.clustering.Clusterer.Result;
import pl.poznan.put.cs.bioserver.clustering.HierarchicalPlot;
import pl.poznan.put.cs.bioserver.clustering.KMedoidsPlot;
import pl.poznan.put.cs.bioserver.external.Matplotlib;
import pl.poznan.put.cs.bioserver.external.Matplotlib.Method;

public class DialogCluster extends JDialog {
    private static final long serialVersionUID = 1L;

    public DialogCluster(final ComparisonGlobal comparisonGlobal, final String plotTitlePrefix) {
        super();

        final JRadioButton hierarchical = new JRadioButton("hierarchical", true);
        JRadioButton kmedoids = new JRadioButton("k-medoids", false);
        ButtonGroup group = new ButtonGroup();
        group.add(hierarchical);
        group.add(kmedoids);

        final JComboBox<String> linkage = new JComboBox<>(new String[] { "Complete", "Single",
                "Average" });
        final JComboBox<String> method = new JComboBox<>(new String[] { "PAM", "PAMSIL" });
        method.setEnabled(false);
        final JCheckBox findBestK = new JCheckBox("Find best k?", true);
        findBestK.setEnabled(false);
        final JSpinner kspinner = new JSpinner();
        kspinner.setModel(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
        kspinner.setEnabled(false);

        JButton buttonExternal = new JButton("Via Matplotlib");
        JButton buttonOk = new JButton("Ok");
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
        container.add(method, c);

        c.gridx = 2;
        container.add(findBestK, c);

        c.gridx = 3;
        container.add(kspinner, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        container.add(buttonExternal, c);

        c.gridx = 1;
        container.add(buttonOk, c);

        c.gridx = 2;
        container.add(buttonClose, c);

        ActionListener radioActionListener = new ActionListener() {
            @Override
            public void actionPerformed(@Nullable ActionEvent arg0) {
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

        buttonExternal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(@Nullable ActionEvent arg0) {
                URL resource;
                XMLSerializable xmlSerializable;
                if (hierarchical.isSelected()) {
                    resource = DialogCluster.class
                            .getResource("/pl/poznan/put/cs/bioserver/external/MatplotlibHierarchical.xsl");
                    Method linkageMethod = new Method[] { Method.COMPLETE, Method.SINGLE,
                            Method.AVERAGE }[linkage.getSelectedIndex()];
                    xmlSerializable = ClusteringHierarchical.newInstance(comparisonGlobal,
                            linkageMethod);
                } else { // partitional.isSelected() == true
                    resource = DialogCluster.class
                            .getResource("/pl/poznan/put/cs/bioserver/external/MatplotlibPartitional.xsl");
                    Result clustering;
                    double[][] distanceMatrix = comparisonGlobal.getDistanceMatrix();
                    if (method.getSelectedItem().equals("PAM")) {
                        if (findBestK.isSelected()) {
                            clustering = Clusterer.clusterPAM(distanceMatrix);
                        } else {
                            clustering = Clusterer.clusterPAM(distanceMatrix,
                                    (Integer) kspinner.getValue());
                        }
                    } else {
                        if (findBestK.isSelected()) {
                            clustering = Clusterer.clusterPAMSIL(distanceMatrix);
                        } else {
                            clustering = Clusterer.clusterPAMSIL(distanceMatrix,
                                    (Integer) kspinner.getValue());
                        }
                    }

                    try {
                        xmlSerializable = ClusteringPartitional.newInstance(comparisonGlobal,
                                clustering);
                    } catch (InvalidDataException e) {
                        JOptionPane.showMessageDialog(DialogCluster.this, e.getMessage(),
                                "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                Matplotlib.runXsltAndPython(resource, xmlSerializable);
            }
        });

        buttonOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(@Nullable ActionEvent e) {
                JFrame plot;
                String plotTitle = plotTitlePrefix;
                double[][] comparisonResults = comparisonGlobal.getDistanceMatrix();
                List<String> structureNames = comparisonGlobal.getLabels();
                if (hierarchical.isSelected()) {
                    plot = new HierarchicalPlot(comparisonResults, structureNames, linkage
                            .getSelectedIndex());
                    plotTitle += "hierarchical clustering (";
                    plotTitle += linkage.getSelectedItem();
                    plotTitle += " linkage)";
                } else {
                    int k;
                    if (findBestK.isSelected()) {
                        k = 0;
                    } else {
                        k = (Integer) kspinner.getValue();
                        if (k > comparisonResults.length) {
                            JOptionPane.showMessageDialog(null, "k parameter "
                                    + "(k-medoids) must be equal or less than "
                                    + "the number of input structures", "Error!",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    try {
                        plot = new KMedoidsPlot(comparisonResults, structureNames, k,
                                (String) method.getSelectedItem());
                    } catch (InvalidDataException e1) {
                        JOptionPane.showMessageDialog(DialogCluster.this, e1.getMessage(),
                                "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    plotTitle += "k-medoids (";
                    plotTitle += method.getSelectedItem();
                    plotTitle += ", k =";
                    plotTitle += k == 0 ? "auto" : Integer.toString(k);
                    plotTitle += ")";
                }
                plot.setTitle(plotTitle);
                plot.setVisible(true);
            }
        });

        buttonClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(@Nullable ActionEvent e) {
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
