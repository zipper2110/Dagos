package com.dagos.maskGenerator;

import com.dagos.graphics.Mask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Dmitry on 06.04.2014
 */
public class MainForm extends com.dagos.MainForm {

    public void createForm() {
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
