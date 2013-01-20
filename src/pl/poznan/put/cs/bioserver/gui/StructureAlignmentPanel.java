package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.gui.jmol.JmolPanel;
import org.jmol.api.JmolViewer;

import pl.poznan.put.cs.bioserver.alignment.AlignmentOutput;
import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
import pl.poznan.put.cs.bioserver.gui.helper.PdbChangeListener;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.PdbManager;

/**
 * A panel that contains all means necessary to align two structures.
 * 
 * @author tzok
 */
public class StructureAlignmentPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger
            .getLogger(StructureAlignmentPanel.class);

    public StructureAlignmentPanel() {
        super();

        JButton buttonLoad = new JButton("Load structure(s)");
        final JButton buttonAlignChain = new JButton("Align selected chain");
        buttonAlignChain.setEnabled(false);
        final JButton buttonAlignAll = new JButton("Align all chains");
        buttonAlignAll.setEnabled(false);

        JPanel panelButtons = new JPanel();
        panelButtons.add(buttonLoad);
        panelButtons.add(buttonAlignChain);
        panelButtons.add(buttonAlignAll);

        final PdbPanel panelPdb = new PdbPanel(new PdbChangeListener() {
            @Override
            public void pdbListChanged() {
                buttonAlignChain.setEnabled(false);
                buttonAlignAll.setEnabled(false);
            }
        });

        final JLabel labelStatus = new JLabel("Ready");

        JPanel panelStatus = new JPanel();
        panelStatus.add(labelStatus);

        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
        panelOptions.add(panelButtons, BorderLayout.NORTH);
        panelOptions.add(panelPdb, BorderLayout.CENTER);
        panelOptions.add(panelStatus, BorderLayout.SOUTH);

        final JmolPanel jmolLeft = new JmolPanel();
        final JmolPanel jmolRight = new JmolPanel();

        JPanel panelJmol = new JPanel();
        panelJmol.setLayout(new GridLayout(1, 2));
        panelJmol.add(jmolLeft);
        panelJmol.add(jmolRight);

        setLayout(new BorderLayout());
        add(panelOptions, BorderLayout.NORTH);
        add(panelJmol, BorderLayout.CENTER);

        buttonLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                panelPdb.loadStructuresWithOpenDialog();
                if (panelPdb.getListModel().size() >= 2) {
                    buttonAlignChain.setEnabled(true);
                    buttonAlignAll.setEnabled(true);
                }
            }
        });

        ActionListener actionListenerAlignment = new ActionListener() {
            private Thread thread;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (thread != null && thread.isAlive()) {
                    JOptionPane.showMessageDialog(null,
                            "Alignment calculation underway!", "Information",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                ListModel<File> model = panelPdb.getListModel();
                final Structure[] structures = new Structure[] {
                        PdbManager.getStructure(model.getElementAt(0)),
                        PdbManager.getStructure(model.getElementAt(1)) };

                if (arg0.getSource().equals(buttonAlignChain)) {
                    int chainIndexFirst = panelPdb.getComboBoxFirst()
                            .getSelectedIndex();
                    int chainIndexSecond = panelPdb.getComboBoxSecond()
                            .getSelectedIndex();
                    structures[0] = new StructureImpl(
                            structures[0].getChain(chainIndexFirst));
                    structures[1] = new StructureImpl(
                            structures[1].getChain(chainIndexSecond));
                }

                boolean isRNA = Helper.isNucleicAcid(structures[0]);
                if (isRNA != Helper.isNucleicAcid(structures[1])) {
                    String message = "Structures meant to be aligned "
                            + "represent different molecule types!";
                    StructureAlignmentPanel.LOGGER.error(message);
                    JOptionPane.showMessageDialog(null, message, "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                labelStatus.setText("Processing");
                final Timer timer = new Timer(250, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String text = labelStatus.getText();
                        int count = StringUtils.countMatches(text, ".");
                        if (count < 5) {
                            labelStatus.setText(text + ".");
                        } else {
                            labelStatus.setText("Processing");
                        }
                    }
                });
                timer.start();

                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Helper.normalizeAtomNames(structures[0]);
                            Helper.normalizeAtomNames(structures[1]);

                            AlignmentOutput output = StructureAligner.align(
                                    structures[0], structures[1]);
                            final Structure[] aligned = output.getStructures();

                            SwingUtilities.invokeLater(new Runnable() {
                                private static final String JMOL_SCRIPT = "frame 0.0; "
                                        + "cartoon only; "
                                        + "select model=1.1; color green; "
                                        + "select model=1.2; color red; ";

                                @Override
                                public void run() {
                                    StringBuilder builder = new StringBuilder();
                                    builder.append("MODEL        1                                                                  \n");
                                    builder.append(aligned[0].toPDB());
                                    builder.append("ENDMDL                                                                          \n");
                                    builder.append("MODEL        2                                                                  \n");
                                    builder.append(aligned[1].toPDB());
                                    builder.append("ENDMDL                                                                          \n");

                                    JmolViewer viewer = jmolLeft.getViewer();
                                    viewer.openStringInline(builder.toString());
                                    jmolLeft.executeCmd(JMOL_SCRIPT);

                                    builder = new StringBuilder();
                                    builder.append("MODEL        1                                                                  \n");
                                    builder.append(aligned[2].toPDB());
                                    builder.append("ENDMDL                                                                          \n");
                                    builder.append("MODEL        2                                                                  \n");
                                    builder.append(aligned[3].toPDB());
                                    builder.append("ENDMDL                                                                          \n");

                                    viewer = jmolRight.getViewer();
                                    viewer.openStringInline(builder.toString());
                                    jmolRight.executeCmd(JMOL_SCRIPT);
                                }
                            });
                        } catch (StructureException e1) {
                            StructureAlignmentPanel.LOGGER.error(e1);
                            JOptionPane.showMessageDialog(getParent(),
                                    e1.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } finally {
                            timer.stop();
                            labelStatus.setText("Ready");
                        }
                    }
                });
                thread.start();
            }
        };
        buttonAlignChain.addActionListener(actionListenerAlignment);
        buttonAlignAll.addActionListener(actionListenerAlignment);
    }
}
