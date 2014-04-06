package com.dagos.maskViewer2D;

import com.dagos.graphics.Image;
import com.dagos.graphics.Mask;
import com.dagos.graphics.Point;
import com.dagos.graphics.display.MaskViewer2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Dmitry on 05.04.2014
 */
public class MainForm extends JFrame {

    private Mask dagosMask;
    private Image dagosImage;
    private Mask invertedMask;
    private MaskViewer2D canvas;

    public MainForm() {
        super("Dagos MaskViewer2D");
        createForm();
    }

    private void createForm() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(null);

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
                        invertedMask = new Mask(invertMask(dagosMask));
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

        setVisible(true);
    }

    private File importFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int openFileResult = fileChooser.showOpenDialog(null);
        if (openFileResult == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            return selectedFile;
        }
        return null;
    }

    private boolean[][][] invertMask(Mask mask) {
        boolean[][][] invertedMask = new boolean[mask.getWitdh()][mask.getHeight()][mask.getSliceCount()];

        for (int k = 0; k < mask.getSliceCount(); k++) {
            for (int i = 0; i < mask.getWitdh(); i++) {
                for (int j = 0; j < mask.getHeight(); j++) {
                    if (mask.getPointValue(i, j, k)) {
                        java.util.List<Point> neighborPoints = getNeighborPoints(
                                new Point(i, j, k), mask.getWitdh(), mask.getHeight(), mask.getSliceCount());
                        for (Point neighborPoint : neighborPoints) {
                            if (!mask.getPointValue(neighborPoint)) {
                                invertedMask[neighborPoint.x][neighborPoint.y][neighborPoint.sliceId] = true;
                            }
                        }
                    }
                }
            }
        }
        return invertedMask;
    }

    private java.util.List<Point> getNeighborPoints(Point point, int imageWidth, int imageHeight, int imageSliceCount) {
        // get 6 points
        java.util.List<Point> neighborPoints = new ArrayList<Point>();
        if (point.x > 0) neighborPoints.add(new Point(point.x - 1, point.y, point.sliceId));
        if (point.x < imageWidth - 1) neighborPoints.add(new Point(point.x + 1, point.y, point.sliceId));
        if (point.y > 0) neighborPoints.add(new Point(point.x, point.y - 1, point.sliceId));
        if (point.y < imageHeight - 1) neighborPoints.add(new Point(point.x, point.y + 1, point.sliceId));
        if (point.sliceId > 0) neighborPoints.add(new Point(point.x, point.y, point.sliceId - 1));
        if (point.sliceId < imageSliceCount - 1)
            neighborPoints.add(new Point(point.x, point.y, point.sliceId + 1));

        return neighborPoints;
    }
}