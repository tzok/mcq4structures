package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartPanel;

import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.visualisation.FragmentMatchChart;

public class LocalComparisonFrame extends JFrame {
    public LocalComparisonFrame(SelectionMatch matches) {
        super("MCQ4Structures: local distance plot");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        for (FragmentMatch match : matches) {
            ChartPanel chart = FragmentMatchChart.create(match);
            chart.setName(match.toString());
            tabbedPane.add(chart);
        }

        getContentPane().add(tabbedPane);
        pack();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Dimension preferredSize = getPreferredSize();
        int x = screenSize.width / 2 - preferredSize.width / 2;
        int y = screenSize.height / 2 - preferredSize.height / 2;
        setLocation(x, y);

    }
}
