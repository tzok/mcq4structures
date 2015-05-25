package pl.poznan.put.gui.window;

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
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.clustering.hierarchical.Clusterer;
import pl.poznan.put.clustering.hierarchical.Linkage;
import pl.poznan.put.clustering.partitional.KMedoids;
import pl.poznan.put.clustering.partitional.KScanner;
import pl.poznan.put.clustering.partitional.PAM;
import pl.poznan.put.clustering.partitional.PAMSIL;
import pl.poznan.put.clustering.partitional.ScoredClusteringResult;
import pl.poznan.put.clustering.partitional.ScoringFunction;
import pl.poznan.put.datamodel.DistanceMatrix;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.visualisation.PartitionalClustering;

public class DialogCluster extends JDialog {
    private final JButton buttonVisualize = new JButton("Visualize");
    private final JButton buttonClose = new JButton("Close");
    private final JCheckBox findBestK = new JCheckBox("Find best k?", true);
    private final JRadioButton hierarchical = new JRadioButton("hierarchical", true);
    private final JRadioButton kmedoids = new JRadioButton("k-medoids", false);
    private final JSpinner kspinner = new JSpinner(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
    private final JComboBox<Linkage> linkageComboBox = new JComboBox<>(Linkage.values());
    private final JComboBox<ScoringFunction> scoringFunction = new JComboBox<>(new ScoringFunction[] { PAM.getInstance(), PAMSIL.getInstance() });

    private final DistanceMatrix distanceMatrix;

    public DialogCluster(DistanceMatrix distanceMatrix) {
        super();
        this.distanceMatrix = distanceMatrix;

        setTitle("MCQ4Structures: clustering method");

        scoringFunction.setEnabled(false);
        findBestK.setEnabled(false);
        kspinner.setEnabled(false);

        ButtonGroup group = new ButtonGroup();
        group.add(hierarchical);
        group.add(kmedoids);

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
        container.add(linkageComboBox, c);

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
        c.gridx = 3;
        container.add(buttonClose, c);

        pack();

        Dimension preferredSize = getPreferredSize();
        int width = preferredSize.width;
        int height = preferredSize.height;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - width;
        int y = screenSize.height - height;
        setSize(width, height);
        setLocation(x / 2, y / 2);

        ActionListener radioActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean isHierarchical = hierarchical.isSelected();
                linkageComboBox.setEnabled(isHierarchical);
                scoringFunction.setEnabled(!isHierarchical);
                findBestK.setEnabled(!isHierarchical);
                kspinner.setEnabled(!isHierarchical && !findBestK.isSelected());
            }
        };
        hierarchical.addActionListener(radioActionListener);
        kmedoids.addActionListener(radioActionListener);
        findBestK.addActionListener(radioActionListener);

        buttonVisualize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Visualizable visualizable = getVisualizable();
                SVGDocument document = visualizable.visualize();
                SVGDialog dialog = new SVGDialog("Clustering visualization", document);
                dialog.setVisible(true);
            }
        });

        buttonClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    public Visualizable getVisualizable() {
        List<String> names = distanceMatrix.getNames();

        if (hierarchical.isSelected()) {
            Linkage linkage = (Linkage) linkageComboBox.getSelectedItem();
            Clusterer clusterer = new Clusterer(names, distanceMatrix.getMatrix(), linkage);
            return clusterer.cluster();
        }

        // FIXME
        KMedoids clusterer = new KMedoids();
        ScoringFunction sf = (ScoringFunction) scoringFunction.getSelectedItem();
        ScoredClusteringResult result;

        if (findBestK.isSelected()) {
            result = KScanner.parallelScan(clusterer, distanceMatrix.getMatrix(), sf);
        } else {
            int k = (int) kspinner.getValue();
            result = clusterer.findPrototypes(distanceMatrix.getMatrix(), sf, k);
        }

        return new PartitionalClustering(distanceMatrix, result);
    }
}
