package com.dagos.maskGenerator;

import com.dagos.graphics.Mask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by Dmitry on 06.04.2014
 */
public class MainForm extends JFrame {

    public MainForm() {
        super("Dagos Test Mask Generator");
        createForm();
    }

    public void createForm() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        JButton saveMaskButton = new JButton("Save test mask");
        saveMaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDagosMask(createTestMask());
                JOptionPane.showMessageDialog(null, "Test mask saved successfully!");
            }
        });
        getContentPane().add(saveMaskButton);

        pack();
        setVisible(true);
    }

    private void saveDagosMask(Mask dagosMask) {
        if (dagosMask != null) {
            JFileChooser fileChooser = new JFileChooser();
            int saveResult = fileChooser.showSaveDialog(null);
            if (saveResult == JFileChooser.APPROVE_OPTION) {
                try {
                    File fileToSave = fileChooser.getSelectedFile();
                    if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(Mask.FILE_EXTENTION)) {
                        fileToSave = new File(fileToSave.getAbsolutePath() + Mask.FILE_EXTENTION);
                    }
                    dagosMask.save(fileToSave);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private Mask createTestMask() {
        Mask mask = new Mask(new boolean[10][10][1]);
        for (int i = 0; i < mask.getWitdh(); i++) {
            for (int j = 0; j < mask.getHeight(); j++) {
                mask.setPointValue(i, j, 0, true);
            }
        }
        return mask;
    }
}
