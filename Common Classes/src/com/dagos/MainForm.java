package com.dagos;

import com.dagos.graphics.Mask;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Dmitry on 15.04.2014
 */
public abstract class MainForm extends JFrame {

    public MainForm() {
        super();
        init();
    }

    public MainForm(String formTitle) {
        super(formTitle);
        init();
    }

    protected void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(null);

        createForm();

        pack();
        setVisible(true);
    }

    protected abstract void createForm();

    protected void saveDagosImage(com.dagos.graphics.Image dagosImage) {
        if (dagosImage != null) {
            JFileChooser fileChooser = new JFileChooser();
            int saveResult = fileChooser.showSaveDialog(null);
            if (saveResult == JFileChooser.APPROVE_OPTION) {
                try {
                    File fileToSave = fileChooser.getSelectedFile();
                    if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(com.dagos.graphics.Image.FILE_EXTENTION)) {
                        fileToSave = new File(fileToSave.getAbsolutePath() + com.dagos.graphics.Image.FILE_EXTENTION);
                    }
                    dagosImage.save(fileToSave);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    protected void saveDagosMask(Mask dagosMask) {
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

    protected void saveDagosMasks(List<Mask> masks) {

    }

    protected File importFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int openFileResult = fileChooser.showOpenDialog(null);
        if (openFileResult == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            return selectedFile;
        }
        return null;
    }
}
