package com.dagos.input;

import com.dagos.graphics.Image;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created by Dmitry on 02.03.14.
 */
public class MainForm extends com.dagos.MainForm {

    private File[] importImages;
    private Image dagosImage;

    public MainForm() {
        super("Dagos Input");
    }

    protected void createForm() {
        setLayout(new FlowLayout());

        JButton importButton = new JButton("Import folder");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importImages = importImages();
            }
        });
        getContentPane().add(importButton);

        JButton convertButton = new JButton("Convert images to dagos image");
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dagosImage = new Image(importImages);
                    JOptionPane.showMessageDialog(null, "Images were successfully converted to dagos image");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        getContentPane().add(convertButton);

        JButton saveButton = new JButton("Save dagos image");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDagosImage(dagosImage);
                JOptionPane.showMessageDialog(null, "Dagos image was saved");
            }
        });
        getContentPane().add(saveButton);
    }

    private File[] importImages() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int openFileResult = fileChooser.showOpenDialog(null);
        if (openFileResult == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            File[] images = selectedFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".bmp");
                }
            });
            JOptionPane.showMessageDialog(null, images.length + " files were imported from " + selectedFile);
            return images;
        }
        return new File[0];
    }
}
