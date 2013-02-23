package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
import javax.swing.border.Border;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.StructureManager;

final class DialogChains extends JDialog {
    private static final long serialVersionUID = 1L;
    public static final int CANCEL = 0;
    public static final int OK = 1;

    private static DialogChains instance;

    public static DialogChains getInstance(Frame owner) {
        if (DialogChains.instance == null) {
            DialogChains.instance = new DialogChains(owner);
        }
        return DialogChains.instance;
    }

    private int chosenOption;
    private File[] selectedStructures;
    private Chain[][] selectedChains;

    private DefaultComboBoxModel<File> modelLeft = new DefaultComboBoxModel<>();
    private JComboBox<File> comboLeft = new JComboBox<>(modelLeft);
    private JPanel panelChainsLeft = new JPanel();
    private JButton buttonRNAsLeft = new JButton("Select RNA chains");
    private JButton buttonProteinsLeft = new JButton("Select protein chains");

    private DefaultComboBoxModel<File> modelRight = new DefaultComboBoxModel<>();
    private JComboBox<File> comboRight = new JComboBox<>(modelRight);
    private JPanel panelChainsRight = new JPanel();
    private JButton buttonRNAsRight = new JButton("Select RNA chains");
    private JButton buttonProteinsRight = new JButton("Select protein chains");

    private DialogChains(Frame owner) {
        super(owner, true);
        setLayout(new BorderLayout());
        setTitle("MCQ4Structures: structure & chain selection");

        Border border = BorderFactory.createTitledBorder("Select chain(s)");

        JPanel panelLeft = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Select structure"), BorderLayout.NORTH);
        panel.add(comboLeft, BorderLayout.CENTER);
        panelLeft.add(panel, BorderLayout.NORTH);
        BoxLayout layout = new BoxLayout(panelChainsLeft, BoxLayout.Y_AXIS);
        panelChainsLeft.setLayout(layout);
        panelChainsLeft.setBorder(border);
        panelLeft.add(new JScrollPane(panelChainsLeft), BorderLayout.CENTER);
        panel = new JPanel();
        panel.add(buttonRNAsLeft);
        panel.add(buttonProteinsLeft);
        panelLeft.add(panel, BorderLayout.SOUTH);

        JPanel panelRight = new JPanel(new BorderLayout());
        panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Select structure"), BorderLayout.NORTH);
        panel.add(comboRight, BorderLayout.CENTER);
        panelRight.add(panel, BorderLayout.NORTH);
        layout = new BoxLayout(panelChainsRight, BoxLayout.Y_AXIS);
        panelChainsRight.setLayout(layout);
        panelChainsRight.setBorder(border);
        panelRight.add(new JScrollPane(panelChainsRight), BorderLayout.CENTER);
        panel = new JPanel();
        panel.add(buttonRNAsRight);
        panel.add(buttonProteinsRight);
        panelRight.add(panel, BorderLayout.SOUTH);

        panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2));
        panel.add(panelLeft);
        panel.add(panelRight);
        add(panel, BorderLayout.CENTER);
        JButton buttonOk = new JButton("OK");
        JButton buttonCancel = new JButton("Cancel");
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

        ActionListener selectChainsListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (StructureManager.getAllStructures().size() == 0) {
                    return;
                }

                JPanel panelReference;
                Structure structure;
                boolean isRna;

                Object source = arg0.getSource();
                if (source.equals(buttonRNAsLeft)) {
                    panelReference = panelChainsLeft;
                    structure = StructureManager.getStructure((File) comboLeft
                            .getSelectedItem());
                    isRna = true;
                } else if (source.equals(buttonProteinsLeft)) {
                    panelReference = panelChainsLeft;
                    structure = StructureManager.getStructure((File) comboLeft
                            .getSelectedItem());
                    isRna = false;
                } else if (source.equals(buttonRNAsRight)) {
                    panelReference = panelChainsRight;
                    structure = StructureManager.getStructure((File) comboRight
                            .getSelectedItem());
                    isRna = true;
                } else { // source.equals(buttonSelectProteinsRight)
                    panelReference = panelChainsRight;
                    structure = StructureManager.getStructure((File) comboRight
                            .getSelectedItem());
                    isRna = false;
                }

                for (Component component : panelReference.getComponents()) {
                    if (component instanceof JCheckBox) {
                        String chainId = ((JCheckBox) component).getText();
                        try {
                            Chain chain = structure.getChainByPDB(chainId);
                            if (Helper.isNucleicAcid(chain) == isRna) {
                                ((JCheckBox) component).setSelected(true);
                            }
                        } catch (StructureException e) {
                            JOptionPane.showMessageDialog(DialogChains.this,
                                    e.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        };
        buttonRNAsLeft.addActionListener(selectChainsListener);
        buttonProteinsLeft.addActionListener(selectChainsListener);
        buttonRNAsRight.addActionListener(selectChainsListener);
        buttonProteinsRight.addActionListener(selectChainsListener);

        final ListCellRenderer<? super File> renderer = comboLeft.getRenderer();
        ListCellRenderer<File> pdbCellRenderer = new ListCellRenderer<File>() {
            @Override
            public Component getListCellRendererComponent(
                    JList<? extends File> list, File value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) renderer.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    label.setText(StructureManager.getName(value));
                }
                return label;
            }
        };
        comboLeft.setRenderer(pdbCellRenderer);
        comboRight.setRenderer(pdbCellRenderer);

        ActionListener actionListenerCombo = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<File> source = (JComboBox<File>) e.getSource();
                File file = (File) source.getSelectedItem();
                if (file == null) {
                    return;
                }
                Structure structure = StructureManager.getStructure(file);
                if (structure == null) {
                    return;
                }

                JPanel panelReference;
                if (source.equals(comboLeft)) {
                    panelReference = panelChainsLeft;
                } else {
                    panelReference = panelChainsRight;
                }

                panelReference.removeAll();
                for (Chain chain : structure.getChains()) {
                    panelReference.add(new JCheckBox(chain.getChainID()));
                }
                panelReference.updateUI();
            }
        };
        comboLeft.addActionListener(actionListenerCombo);
        comboRight.addActionListener(actionListenerCombo);

        buttonOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                @SuppressWarnings("rawtypes")
                JComboBox[] combos = new JComboBox[] { comboLeft, comboRight };
                JPanel[] panels = new JPanel[] { panelChainsLeft,
                        panelChainsRight };

                selectedStructures = new File[2];
                selectedChains = new Chain[2][];
                for (int i = 0; i < 2; i++) {
                    List<Chain> list = new ArrayList<>();
                    File pdb = (File) combos[i].getSelectedItem();
                    Structure structure = StructureManager.getStructure(pdb);
                    for (Component component : panels[i].getComponents()) {
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
                    selectedStructures[i] = pdb;
                    selectedChains[i] = list.toArray(new Chain[list.size()]);
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

    public Chain[][] getChains() {
        return selectedChains;
    }

    public File[] getFiles() {
        return selectedStructures;
    }

    public String getSelectionDescription() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            builder.append(StructureManager.getName(selectedStructures[i]));
            builder.append('.');
            for (Chain chain : selectedChains[i]) {
                builder.append(chain.getChainID());
            }
            if (i == 0) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    public int showDialog() {
        modelLeft.removeAllElements();
        modelRight.removeAllElements();
        for (File file : StructureManager.getAllStructures()) {
            modelLeft.addElement(file);
            modelRight.addElement(file);
        }

        chosenOption = DialogChains.CANCEL;
        setVisible(true);
        return chosenOption;
    }
}
