package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import pl.poznan.put.comparison.ModelsComparisonResult;
import pl.poznan.put.visualisation.ColorbarComponent;

public class ColorbarFrame extends JFrame {
    public ColorbarFrame(ModelsComparisonResult result) {
        super("Colorbar");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        ColorbarComponent colorbar = new ColorbarComponent(result);
        getContentPane().add(colorbar, BorderLayout.CENTER);
        pack();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = screenSize.width / 2 - colorbar.getSvgWidth() / 2;
        int y = screenSize.height / 2 - colorbar.getSvgHeight() / 2;
        setLocation(x, y);
    }
}
