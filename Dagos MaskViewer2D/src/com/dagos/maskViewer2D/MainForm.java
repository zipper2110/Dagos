package com.dagos.maskViewer2D;

import com.dagos.graphics.Image;
import com.dagos.graphics.Mask;
import com.dagos.graphics.display.MaskViewer2D;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by Dmitry on 05.04.2014
 */
public class MainForm extends com.dagos.MainForm {

    private Mask dagosMask;
    private Image dagosImage;
    private Mask invertedMask;
    private MaskViewer2D canvas;

    public MainForm() {
        super("Dagos MaskViewer2D");
    }

    protected void createForm() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menu = new JMenu("Menu");
        menuBar.add(menu);

        JMenuItem openMaskItem = new JMenuItem("Open dagos mask");
        openMaskItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File importMaskFile = importFile();
                if (importMaskFile != null) {
                    try {
                        dagosMask = new Mask(importMaskFile);
                        invertedMask = dagosMask.invert();
                        JOptionPane.showMessageDialog(null, "Mask file was imported from " + importMaskFile);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Mask file wasn't imported from " + importMaskFile + ": " + e1.getMessage());
                        e1.printStackTrace();
                    }
                }
            }
        });
        menu.add(openMaskItem);

        JMenuItem openImageItem = new JMenuItem("Open dagos image");
        openImageItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File importImageFile = importFile();
                if (importImageFile != null) {
                    try {
                        dagosImage = new Image(importImageFile);
                        canvas.setImage(dagosImage);
                        canvas.display();
                        JOptionPane.showMessageDialog(null, "Image file was imported from " + importImageFile);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Mask file wasn't imported from " + importImageFile + ": " + e1.getMessage());
                        e1.printStackTrace();
                    }
                }
            }
        });
        menu.add(openImageItem);

        JMenuItem displayMaskItem = new JMenuItem("Display mask");
        displayMaskItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.setMask(dagosMask);
                canvas.display();
            }
        });
        menu.add(displayMaskItem);

        JMenuItem displayInvertedMaskItem = new JMenuItem("Display inverted mask");
        displayInvertedMaskItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.setMask(invertedMask);
                canvas.display();
            }
        });
        menu.add(displayInvertedMaskItem);

        canvas = new MaskViewer2D();
        add("Center", canvas);
    }
}