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

import pl.poznan.put.comparison.ModelsComparisonResult;
import pl.poznan.put.visualisation.ColorbarComponent;

public class ColorbarFrame extends JFrame {
    private final ColorbarComponent colorbar;

    public ColorbarFrame(ModelsComparisonResult.SelectedAngle result) {
        super("Colorbar");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        Container contentPane = getContentPane();
        JButton buttonSave = new JButton("Save");
        contentPane.add(buttonSave, BorderLayout.NORTH);

        colorbar = new ColorbarComponent(result);
        contentPane.add(colorbar, BorderLayout.CENTER);
        pack();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = screenSize.width / 2 - colorbar.getSvgWidth() / 2;
        int y = screenSize.height / 2 - colorbar.getSvgHeight() / 2;
        setLocation(x, y);

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                colorbar.selectFileAndExport();
            }
        });
    }
}
