package pl.poznan.put.cs.bioserver.gui;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MainWindow.class);

    public MainWindow() throws HeadlessException {
        super();
        /*
         * Set L&F
         */
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException
                        | UnsupportedLookAndFeelException e) {
                    // do nothing
                }
                break;
            }
        }

        /*
         * Create menu
         */
        JMenuItem itemOpen = new JMenuItem("Open structure(s)",
                loadIcon("/toolbarButtonGraphics/general/Open16.gif"));
        JMenuItem itemSave = new JMenuItem("Save results",
                loadIcon("/toolbarButtonGraphics/general/Save16.gif"));
        itemSave.setEnabled(false);
        JMenuItem itemExit = new JMenuItem("Exit");
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);
        menuFile.add(itemOpen);
        menuFile.add(itemSave);
        menuFile.add(itemExit);

        JRadioButton radioMcq = new JRadioButton("MCQ", true);
        JRadioButton radioRmsd = new JRadioButton("RMSD", false);
        ButtonGroup group = new ButtonGroup();
        group.add(radioMcq);
        group.add(radioRmsd);
        JMenu menuMeasure = new JMenu("Distance measure");
        menuMeasure.setEnabled(false);
        menuMeasure.add(radioMcq);
        menuMeasure.add(radioRmsd);

        JMenuItem itemSelectStructures = new JMenuItem("Select structures");
        itemSelectStructures.setEnabled(false);
        JMenuItem itemComputeGlobal = new JMenuItem("Compute distance matrix");
        itemComputeGlobal.setEnabled(false);
        JMenuItem itemVisualise = new JMenuItem("Visualise results");
        itemVisualise.setEnabled(false);
        JMenuItem itemCluster = new JMenuItem("Cluster results");
        itemCluster.setEnabled(false);
        JMenu menuGlobal = new JMenu("Global comparison");
        menuGlobal.add(itemSelectStructures);
        menuGlobal.add(menuMeasure);
        menuGlobal.add(itemComputeGlobal);
        menuGlobal.add(itemVisualise);
        menuGlobal.add(itemVisualise);
        menuGlobal.add(itemCluster);

        JMenuItem itemSelectChainsCompare = new JMenuItem("Select chains");
        itemSelectChainsCompare.setEnabled(false);
        JMenuItem itemSelectTorsion = new JMenuItem("Select torsion angles");
        itemSelectTorsion.setEnabled(false);
        JMenuItem itemComputeLocal = new JMenuItem("Compute distances");
        itemComputeLocal.setEnabled(false);
        JMenu menuLocal = new JMenu("Local comparison");
        menuLocal.add(itemSelectChainsCompare);
        menuLocal.add(itemSelectTorsion);
        menuLocal.add(itemComputeLocal);

        JMenu menuDistance = new JMenu("Distance computation");
        menuDistance.setMnemonic(KeyEvent.VK_D);
        menuDistance.add(menuGlobal);
        menuDistance.addSeparator();
        menuDistance.add(menuLocal);

        JRadioButton radioAlignGlobal = new JRadioButton("Global", true);
        JRadioButton radioAlignLocal = new JRadioButton("Local", false);
        ButtonGroup groupAlign = new ButtonGroup();
        groupAlign.add(radioAlignGlobal);
        groupAlign.add(radioAlignLocal);
        JMenu menuSelectAlignType = new JMenu("Select alignment type");
        menuSelectAlignType.setEnabled(false);
        menuSelectAlignType.add(radioAlignGlobal);
        menuSelectAlignType.add(radioAlignLocal);

        JMenuItem itemSelectChainsAlignSeq = new JMenuItem("Select chains");
        itemSelectChainsAlignSeq.setEnabled(false);
        JMenuItem itemComputeAlignSeq = new JMenuItem("Compute alignment");
        itemComputeAlignSeq.setEnabled(false);
        JMenu menuAlignSeq = new JMenu("Sequence");
        menuAlignSeq.add(itemSelectChainsAlignSeq);
        menuAlignSeq.add(menuSelectAlignType);
        menuAlignSeq.add(itemComputeAlignSeq);

        JMenuItem itemSelectChainsAlignStruc = new JMenuItem("Select chains");
        itemSelectChainsAlignStruc.setEnabled(false);
        JMenuItem itemComputeAlignStruc = new JMenuItem("Compute alignment");
        itemComputeAlignStruc.setEnabled(false);
        JMenu menuAlignStruc = new JMenu("3D structure");
        menuAlignStruc.add(itemSelectChainsAlignStruc);
        menuAlignStruc.add(itemComputeAlignStruc);

        JMenu menuAlignment = new JMenu("Alignment");
        menuAlignment.setMnemonic(KeyEvent.VK_A);
        menuAlignment.add(menuAlignSeq);
        menuAlignment.addSeparator();
        menuAlignment.add(menuAlignStruc);

        JMenuItem itemGuide = new JMenuItem("Quick guide");
        itemGuide.setMnemonic(KeyEvent.VK_Q);
        itemGuide.setMaximumSize(new Dimension(
                itemGuide.getPreferredSize().width,
                itemGuide.getMaximumSize().height));

        JMenuItem itemAbout = new JMenuItem("About");
        itemAbout.setMnemonic(KeyEvent.VK_O);
        itemAbout.setMaximumSize(new Dimension(
                itemAbout.getPreferredSize().width,
                itemAbout.getMaximumSize().height));

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menuFile);
        menuBar.add(menuDistance);
        menuBar.add(menuAlignment);
        menuBar.add(itemGuide);
        menuBar.add(itemAbout);

        setJMenuBar(menuBar);

        /*
         * Set window properties
         */
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("MCQ4Structures: computing similarity of 3D RNA / protein structures");

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        setSize(size.width * 3 / 4, size.height * 3 / 4);
        setLocation(size.width / 8, size.height / 8);
    }

    private ImageIcon loadIcon(String name) {
        URL resource = getClass().getResource(name);
        if (resource == null) {
            MainWindow.LOGGER.error("Failed to load icon: " + name);
            return null;
        }
        return new ImageIcon(resource);
    }
}
