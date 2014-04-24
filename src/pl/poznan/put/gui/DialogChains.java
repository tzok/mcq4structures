package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import org.apache.commons.lang3.tuple.Pair;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.helper.Helper;
import pl.poznan.put.helper.StructureManager;

final class DialogChains extends JDialog {
    private class PanelChains extends JPanel {
        private static final long serialVersionUID = 1L;

        DefaultComboBoxModel<Structure> model = new DefaultComboBoxModel<>();
        JComboBox<Structure> combo = new JComboBox<>(model);
        JPanel[] panels = new JPanel[] { new JPanel(), new JPanel() };

        public PanelChains() {
            super(new BorderLayout());

            panels[0].setLayout(new BoxLayout(panels[0], BoxLayout.Y_AXIS));
            panels[1].setLayout(new BoxLayout(panels[1], BoxLayout.Y_AXIS));

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Select structure"), BorderLayout.NORTH);
            panel.add(combo, BorderLayout.CENTER);
            add(panel, BorderLayout.NORTH);

            panel = new JPanel(new GridLayout(1, 2));
            panel.setBorder(BorderFactory.createTitledBorder("Select chain(s)"));
            panel.add(panels[0]);
            panel.add(panels[1]);
            add(new JScrollPane(panel), BorderLayout.CENTER);

            final ListCellRenderer<? super Structure> renderer = combo
                    .getRenderer();
            combo.setRenderer(new ListCellRenderer<Structure>() {
                @Override
                public Component getListCellRendererComponent(
                        JList<? extends Structure> list, Structure value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) renderer
                            .getListCellRendererComponent(list, value, index,
                                    isSelected, cellHasFocus);
                    if (value != null) {
                        label.setText(StructureManager.getName(value));
                    }
                    return label;
                }
            });

            combo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Structure structure = (Structure) combo.getSelectedItem();
                    if (structure == null) {
                        return;
                    }

                    panels[0].removeAll();
                    panels[1].removeAll();
                    panels[0].add(new JLabel("RNAs:"));
                    panels[1].add(new JLabel("Proteins:"));
                    for (Chain chain : structure.getChains()) {
                        JCheckBox checkBox = new JCheckBox(chain.getChainID());
                        int index = Helper.isNucleicAcid(chain) ? 0 : 1;
                        panels[index].add(checkBox);
                    }
                    panels[0].updateUI();
                    panels[1].updateUI();
                }
            });
        }

        public List<Chain> getSelectedChains() {
            List<Chain> list = new ArrayList<>();
            Structure structure = (Structure) combo.getSelectedItem();
            if (structure != null) {
                for (JPanel panel : panels) {
                    for (Component component : panel.getComponents()) {
                        if (component instanceof JCheckBox
                                && ((JCheckBox) component).isSelected()) {
                            String chainId = ((JCheckBox) component).getText();
                            try {
                                list.add(structure.getChainByPDB(chainId));
                            } catch (StructureException e) {
                                JOptionPane.showMessageDialog(
                                        DialogChains.this, e.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
            return list;
        }
    }

    public static final int CANCEL = 0;
    public static final int OK = 1;
    private static DialogChains instance;

    private static final long serialVersionUID = 1L;

    public static synchronized DialogChains getInstance(Frame owner) {
        DialogChains inst = DialogChains.instance;
        if (inst == null) {
            inst = new DialogChains(owner);
        }
        DialogChains.instance = inst;
        return inst;
    }

    List<Chain> chainsLeft = new ArrayList<>();
    List<Chain> chainsRight = new ArrayList<>();
    int chosenOption;
    PanelChains panelsChainsLeft = new PanelChains();
    PanelChains panelsChainsRight = new PanelChains();

    Structure structureLeft;
    Structure structureRight;

    private DialogChains(Frame owner) {
        super(owner, true);
        setLayout(new BorderLayout());
        setTitle("MCQ4Structures: structure & chain selection");

        JButton buttonOk = new JButton("OK");
        JButton buttonCancel = new JButton("Cancel");

        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(panelsChainsLeft);
        panel.add(panelsChainsRight);
        add(panel, BorderLayout.CENTER);

        panel = new JPanel();
        panel.add(buttonOk);
        panel.add(buttonCancel);
        add(panel, BorderLayout.SOUTH);

        int width = 640;
        int height = 480;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - width;
        int y = screenSize.height - height;
        setSize(width, height);
        setLocation(x / 2, y / 2);

        buttonOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                structureLeft = (Structure) panelsChainsLeft.combo
                        .getSelectedItem();
                structureRight = (Structure) panelsChainsRight.combo
                        .getSelectedItem();
                if (structureLeft == null || structureRight == null) {
                    chosenOption = DialogChains.CANCEL;
                    dispose();
                    return;
                }

                chainsLeft = panelsChainsLeft.getSelectedChains();
                chainsRight = panelsChainsRight.getSelectedChains();
                if (chainsLeft.size() == 0 || chainsRight.size() == 0) {
                    chosenOption = DialogChains.CANCEL;
                    dispose();
                    return;
                }

                chosenOption = DialogChains.OK;
                dispose();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chosenOption = DialogChains.CANCEL;
                dispose();
            }
        });
    }

    public Pair<List<Chain>, List<Chain>> getChains() {
        return Pair.of(chainsLeft, chainsRight);
    }

    public String getSelectionDescription() {
        Structure left = structureLeft;
        Structure right = structureRight;
        if (left == null || right == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<span style=\"color: blue\">");
        builder.append(StructureManager.getName(left));
        builder.append('.');
        for (Chain chain : chainsLeft) {
            builder.append(chain.getChainID());
        }
        builder.append("</span>, <span style=\"color: green\">");
        builder.append(StructureManager.getName(right));
        builder.append('.');
        for (Chain chain : chainsRight) {
            builder.append(chain.getChainID());
        }
        builder.append("</span>");
        return builder.toString();
    }

    public Pair<Structure, Structure> getStructures() {
        return Pair.of(structureLeft, structureRight);
    }

    public int showDialog() {
        panelsChainsLeft.model.removeAllElements();
        panelsChainsRight.model.removeAllElements();
        for (Structure structure : StructureManager.getAllStructures()) {
            panelsChainsLeft.model.addElement(structure);
            panelsChainsRight.model.addElement(structure);
        }

        chosenOption = DialogChains.CANCEL;
        setVisible(true);
        return chosenOption;
    }
}
