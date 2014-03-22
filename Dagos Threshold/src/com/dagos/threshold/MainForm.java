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
public class MainForm extends JFrame {

    private Image dagosImage;
    private Mask dagosMask;

    public MainForm() {
        super("Dagos Threshold");
        createForm();
    }

    public void createForm() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        JButton openImageButton = new JButton("Open Dagos image");
        openImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File importImageFile = importImage();
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

        pack();
        setVisible(true);
    }

    private File importImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int openFileResult = fileChooser.showOpenDialog(null);
        if (openFileResult == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            return selectedFile;
        }
        return null;
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
}
