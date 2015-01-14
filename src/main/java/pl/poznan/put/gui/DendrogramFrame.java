package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import pl.poznan.put.clustering.hierarchical.HierarchicalClusteringResult;
import pl.poznan.put.visualisation.DendrogramComponent;

public class DendrogramFrame extends JFrame {
    private final DendrogramComponent dendrogram;

    public DendrogramFrame(HierarchicalClusteringResult clustering,
            String[] names) {
        super("MCQ4Structures: dendrogram of clustered data");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        Container contentPane = getContentPane();
        JButton buttonSave = new JButton("Save");
        contentPane.add(buttonSave, BorderLayout.NORTH);

        dendrogram = new DendrogramComponent(clustering, names);
        contentPane.add(dendrogram, BorderLayout.CENTER);
        pack();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = screenSize.width / 2 - dendrogram.getSvgWidth() / 2;
        int y = screenSize.height / 2 - dendrogram.getSvgHeight() / 2;
        setLocation(x, y);

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dendrogram.selectFileAndExport();
            }
        });
    }
}
