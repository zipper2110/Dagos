package com.dagos.threshold;

import com.dagos.graphics.Image;
import com.dagos.graphics.Mask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by Dmitry on 02.03.14.
 */
public class MainForm extends com.dagos.MainForm {

    private Image dagosImage;
    private Mask dagosMask;

    public void createForm() {
        setLayout(new FlowLayout());

        JButton openImageButton = new JButton("Open Dagos image");
        openImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File importImageFile = importFile();
                if (importImageFile != null) {
                    try {
                        dagosImage = new Image(importImageFile);
                        JOptionPane.showMessageDialog(null, "Image file was imported from " + importImageFile);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Image file wasn't imported from " + importImageFile + ": " + e1.getMessage());
                        e1.printStackTrace();
                    }
                }
            }
        });
        getContentPane().add(openImageButton);

        JButton applyThresholdButton = new JButton("Apply lung segmentation");
        applyThresholdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SegmentationI segmentation = new SegmentationRegionGrowing();
                dagosMask = segmentation.getMask(dagosImage);
                JOptionPane.showMessageDialog(null, "Image segmentation performed successfully!");
            }
        });
        getContentPane().add(applyThresholdButton);

        JButton saveMaskButton = new JButton("Save segmentation mask");
        saveMaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDagosMask(dagosMask);
                JOptionPane.showMessageDialog(null, "Segmentation mask saved successfully!");
            }
        });
        getContentPane().add(saveMaskButton);
    }
}
