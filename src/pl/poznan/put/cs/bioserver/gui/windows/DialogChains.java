package pl.poznan.put.cs.bioserver.gui.windows;

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

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.cs.bioserver.helper.StructureManager;

final class DialogChains extends JDialog {
    private static final long serialVersionUID = 1L;
    public static final int CANCEL = 0;
    public static final int OK = 1;

    private static DialogChains instance;

    private int chosenOption;
    private File[] selectedStructures;
    private Chain[][] selectedChains;
    private DefaultComboBoxModel<File> modelLeft;
    private DefaultComboBoxModel<File> modelRight;

    public Chain[][] getChains() {
        return selectedChains;
    }

    public File[] getFiles() {
        return selectedStructures;
    }

    public static DialogChains getInstance(Frame owner) {
        if (DialogChains.instance == null) {
            DialogChains.instance = new DialogChains(owner);
        }
        return DialogChains.instance;
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

    private DialogChains(Frame owner) {
        super(owner, true);

        modelLeft = new DefaultComboBoxModel<>();
        final JComboBox<File> comboLeft = new JComboBox<>(modelLeft);
        final JPanel panelChainsLeft = new JPanel();
        panelChainsLeft.setLayout(new BoxLayout(panelChainsLeft,
                BoxLayout.Y_AXIS));
        panelChainsLeft.setBorder(BorderFactory
                .createTitledBorder("Select chain(s)"));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Select structure"), BorderLayout.NORTH);
        panel.add(comboLeft, BorderLayout.CENTER);
        final JPanel panelLeft = new JPanel(new BorderLayout());
        panelLeft.add(panel, BorderLayout.NORTH);
        panelLeft.add(new JScrollPane(panelChainsLeft), BorderLayout.CENTER);

        modelRight = new DefaultComboBoxModel<>();
        final JComboBox<File> comboRight = new JComboBox<>(modelRight);
        final JPanel panelChainsRight = new JPanel();
        panelChainsRight.setLayout(new BoxLayout(panelChainsRight,
                BoxLayout.Y_AXIS));
        panelChainsRight.setBorder(BorderFactory
                .createTitledBorder("Select chain(s)"));
        panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Select structure"), BorderLayout.NORTH);
        panel.add(comboRight, BorderLayout.CENTER);
        JPanel panelRight = new JPanel();
        panelRight.setLayout(new BorderLayout());
        panelRight.add(panel, BorderLayout.NORTH);
        panelRight.add(new JScrollPane(panelChainsRight), BorderLayout.CENTER);

        JPanel panelBoth = new JPanel();
        panelBoth.setLayout(new GridLayout(1, 2));
        panelBoth.add(panelLeft);
        panelBoth.add(panelRight);

        JButton buttonOk = new JButton("OK");
        JButton buttonCancel = new JButton("Cancel");
        JPanel panelButtons = new JPanel();
        panelButtons.add(buttonOk);
        panelButtons.add(buttonCancel);

        setLayout(new BorderLayout());
        add(panelBoth, BorderLayout.CENTER);
        add(panelButtons, BorderLayout.SOUTH);

        int width = 640;
        int height = 480;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - width;
        int y = screenSize.height - height;
        setSize(width, height);
        setLocation(x / 2, y / 2);

        setTitle("MCQ4Structures: structure & chain selection");

        final ListCellRenderer<? super File> renderer = comboLeft.getRenderer();
        ListCellRenderer<File> pdbCellRenderer = new ListCellRenderer<File>() {
            @Override
            public Component getListCellRendererComponent(
                    JList<? extends File> list, File value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) renderer.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setText(StructureManager.getName(value));
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
}
