package pl.poznan.put.gui.window;

import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.clustering.hierarchical.Clusterer;
import pl.poznan.put.clustering.hierarchical.Linkage;
import pl.poznan.put.clustering.partitional.KMedoids;
import pl.poznan.put.clustering.partitional.KScanner;
import pl.poznan.put.clustering.partitional.PAM;
import pl.poznan.put.clustering.partitional.PAMSIL;
import pl.poznan.put.clustering.partitional.PrototypeBasedClusterer;
import pl.poznan.put.clustering.partitional.ScoredClusteringResult;
import pl.poznan.put.clustering.partitional.ScoringFunction;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.types.DistanceMatrix;
import pl.poznan.put.visualisation.PartitionalClustering;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.List;

public class DialogCluster extends JDialog {
  private static final long serialVersionUID = -4544656737734548208L;

  private final JCheckBox findBestK = new JCheckBox("Find best k?", true);
  private final JRadioButton hierarchical = new JRadioButton("hierarchical", true);
  private final JSpinner kspinner = new JSpinner(new SpinnerNumberModel(2, 2, 12, 1));
  private final JComboBox<Linkage> linkageComboBox = new JComboBox<>(Linkage.values());
  private final JComboBox<ScoringFunction> scoringFunction =
      new JComboBox<>(new ScoringFunction[] {PAM.getInstance(), PAMSIL.getInstance()});

  private final DistanceMatrix distanceMatrix;

  public DialogCluster(final DistanceMatrix distanceMatrix) {
    super();
    this.distanceMatrix = distanceMatrix;

    setTitle("MCQ4Structures: clustering method");

    scoringFunction.setEnabled(false);
    findBestK.setEnabled(false);
    kspinner.setEnabled(false);

    final ButtonGroup group = new ButtonGroup();
    group.add(hierarchical);
    final JRadioButton kmedoids = new JRadioButton("k-medoids", false);
    group.add(kmedoids);

    final Container container = getContentPane();
    container.setLayout(new GridBagLayout());

    final GridBagConstraints c = new GridBagConstraints();
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
    final JButton buttonVisualize = new JButton("Visualize");
    container.add(buttonVisualize, c);
    c.gridx = 3;
    final JButton buttonClose = new JButton("Close");
    container.add(buttonClose, c);

    pack();

    final Dimension preferredSize = getPreferredSize();
    final int width = preferredSize.width;
    final int height = preferredSize.height;

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int x = screenSize.width - width;
    final int y = screenSize.height - height;
    setSize(width, height);
    setLocation(x / 2, y / 2);

    final ActionListener radioActionListener =
        arg0 -> {
          final boolean isHierarchical = hierarchical.isSelected();
          linkageComboBox.setEnabled(isHierarchical);
          scoringFunction.setEnabled(!isHierarchical);
          findBestK.setEnabled(!isHierarchical);
          kspinner.setEnabled(!isHierarchical && !findBestK.isSelected());
        };
    hierarchical.addActionListener(radioActionListener);
    kmedoids.addActionListener(radioActionListener);
    findBestK.addActionListener(radioActionListener);

    buttonVisualize.addActionListener(
        arg0 -> {
          final Visualizable visualizable = getVisualizable();
          final SVGDocument document = visualizable.visualize();
          final SVGDialog dialog = new SVGDialog("Clustering visualization", document);
          dialog.setVisible(true);
        });

    buttonClose.addActionListener(e -> dispose());
  }

  private Visualizable getVisualizable() {
    final List<String> names = distanceMatrix.getNames();

    if (hierarchical.isSelected()) {
      final Linkage linkage = (Linkage) linkageComboBox.getSelectedItem();
      final Clusterer clusterer = new Clusterer(names, distanceMatrix.getMatrix(), linkage);
      return clusterer.cluster();
    }

    // FIXME
    final PrototypeBasedClusterer clusterer = new KMedoids();
    final ScoringFunction sf = (ScoringFunction) scoringFunction.getSelectedItem();
    final ScoredClusteringResult result;

    if (findBestK.isSelected()) {
      result = KScanner.parallelScan(clusterer, distanceMatrix.getMatrix(), sf);
    } else {
      final int k = (int) kspinner.getValue();
      result = clusterer.findPrototypes(distanceMatrix.getMatrix(), sf, k);
    }

    return new PartitionalClustering(distanceMatrix, result);
  }
}
