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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.helper.StructureManager;

class DialogChains extends JDialog {
    public static final int CANCEL = 0;
    public static final int OK = 1;
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DialogChains.class);
    private static DialogChains INSTANCE;

    private static int chosenOption;

    private static File[] selectedStructures;
    private static Chain[][] selectedChains;
    private static DefaultComboBoxModel<File> modelLeft;
    private static DefaultComboBoxModel<File> modelRight;

    public static Chain[][] getChains() {
        return DialogChains.selectedChains;
    }

    public static File[] getFiles() {
        return DialogChains.selectedStructures;
    }

    public static DialogChains getInstance(Frame owner) {
        if (DialogChains.INSTANCE == null) {
            DialogChains.INSTANCE = new DialogChains(owner);
        }
        return DialogChains.INSTANCE;
    }

    public static String[] getNames() {
        return new String[] {
                StructureManager.getName(DialogChains.selectedStructures[0]),
                StructureManager.getName(DialogChains.selectedStructures[1]) };
    }

    public static String getSelectionDescription() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            builder.append(StructureManager
                    .getName(DialogChains.selectedStructures[i]));
            builder.append('.');
            for (Chain chain : DialogChains.selectedChains[i]) {
                builder.append(chain.getChainID());
            }
            if (i == 0) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    public static int showDialog() {
        DialogChains.modelLeft.removeAllElements();
        DialogChains.modelRight.removeAllElements();
        for (File file : StructureManager.getAllStructures()) {
            DialogChains.modelLeft.addElement(file);
            DialogChains.modelRight.addElement(file);
        }

        DialogChains.chosenOption = DialogChains.CANCEL;
        DialogChains.INSTANCE.setVisible(true);
        return DialogChains.chosenOption;
    }

    private DialogChains(Frame owner) {
        super(owner, true);

        DialogChains.modelLeft = new DefaultComboBoxModel<>();
        final JComboBox<File> comboLeft = new JComboBox<>(
                DialogChains.modelLeft);
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

        DialogChains.modelRight = new DefaultComboBoxModel<>();
        final JComboBox<File> comboRight = new JComboBox<>(
                DialogChains.modelRight);
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

                DialogChains.selectedStructures = new File[2];
                DialogChains.selectedChains = new Chain[2][];
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
                                DialogChains.LOGGER.error(
                                        "Failed to read chain " + chainId
                                                + " from structure: " + pdb, e);
                            }
                        }
                    }
                    DialogChains.selectedStructures[i] = pdb;
                    DialogChains.selectedChains[i] = list
                            .toArray(new Chain[list.size()]);
                }

                DialogChains.chosenOption = DialogChains.OK;
                dispose();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogChains.chosenOption = DialogChains.CANCEL;
                dispose();
            }
        });
    }
}
