package com.simagis.maskViewer;

import com.dagos.graphics.*;
import com.dagos.graphics.Image;
import com.dagos.graphics.Point;
import com.dagos.graphics.display.MaskViewer3D;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Dmitry on 05.03.14.
 */
public class MainForm extends JFrame {

    private Mask dagosMask;
    private Image dagosImage;
    private Mask invertedMask;
    private MaskViewer3D canvas;

    private Point pointFrom = new Point(0, 0, 0);
    private Point pointTo = new Point(0, 0, 0);
    private JTextField fieldXFrom;
    private JTextField fieldXTo;
    private JTextField fieldYFrom;
    private JTextField fieldYTo;
    private JTextField fieldZFrom;
    private JTextField fieldZTo;

    public MainForm() {
        super("Dagos MaskViewer");
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
                        dagosMask = Mask.Load(importMaskFile);
                        invertedMask = new Mask(invertMask(dagosMask));
                        fieldXFrom.setText("1");
                        fieldXTo.setText(Integer.toString((invertedMask.getWitdh())));
                        fieldYFrom.setText("1");
                        fieldYTo.setText(Integer.toString((invertedMask.getHeight())));
                        fieldZFrom.setText("1");
                        fieldZTo.setText(Integer.toString((invertedMask.getSliceCount())));
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
                        JOptionPane.showMessageDialog(null, "Image file was imported from " + importImageFile);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Mask file wasn't imported from " + importImageFile + ": " + e1.getMessage());
                        e1.printStackTrace();
                    }
                }
            }
        });
        menu.add(openImageItem);

        JMenuItem displayLungItem = new JMenuItem("Display lung");
        displayLungItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pointFrom = new Point(0, 0, 0);
                pointTo = new Point(invertedMask.getWitdh() - 1, invertedMask.getHeight() - 1, invertedMask.getSliceCount() - 1);
                canvas.displayMask(invertedMask, dagosImage, pointFrom, pointTo);
            }
        });
        menu.add(displayLungItem);

        add("North", getSettingsPanel());

        canvas = new MaskViewer3D();
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

        for(int k = 0; k < mask.getSliceCount(); k++) {
            for(int i = 0; i < mask.getWitdh(); i++) {
                for(int j = 0; j < mask.getHeight(); j++) {
                    if(mask.getPointValue(i, j, k)) {
                        java.util.List<Point> neighborPoints = getNeighborPoints(
                                new Point(i, j, k), mask.getWitdh(), mask.getHeight(), mask.getSliceCount());
                        for(Point neighborPoint : neighborPoints) {
                            if(!mask.getPointValue(neighborPoint)) {
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

    private JPanel getSettingsPanel() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        settingsPanel.add(panel);

        JButton applySettingsButton = new JButton("Apply");
        applySettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.displayMask(invertedMask, dagosImage, pointFrom, pointTo);
            }
        });
        settingsPanel.add(applySettingsButton);

        // ------------------------- X axis settings
        JPanel panelX = new JPanel();
        panelX.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelX.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(panelX);

        JLabel labelX = new JLabel("X: from ");
        panelX.add(labelX);
        fieldXFrom = new JTextField("0");
        fieldXFrom.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setValue(e);
            }

            private void setValue(DocumentEvent e) {
                try {
                    if(e.getDocument().getLength() == 0) return;
                    Integer value = new Integer(e.getDocument().getText(0, e.getDocument().getLength()));
                    if (checkValue(value, 0, Math.min(invertedMask.getWitdh() - 1, pointTo.x))) {
                        pointFrom.x = value;
                    } else {
                        pointFrom.x = 0;
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });
        fieldXFrom.setPreferredSize(new Dimension(100, 30));
        panelX.add(fieldXFrom);
        JLabel labelXTo = new JLabel(" to ");
        panelX.add(labelXTo);
        fieldXTo = new JTextField("0");
        fieldXTo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setValue(e);
            }

            private void setValue(DocumentEvent e) {
                try {
                    if (e.getDocument().getLength() == 0) return;
                    Integer value = new Integer(e.getDocument().getText(0, e.getDocument().getLength()));
                    if (checkValue(value, Math.max(pointFrom.x, 0), invertedMask.getWitdh() - 1)) {
                        pointTo.x = value;
                    } else {
                        pointTo.x = invertedMask.getWitdh() - 1;
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });
        fieldXTo.setPreferredSize(new Dimension(100, 30));
        panelX.add(fieldXTo);

        // ------------------------- Y axis settings
        JPanel panelY = new JPanel();
        panelY.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelY.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(panelY);

        JLabel labelY = new JLabel("Y: from ");
        panelY.add(labelY);
        fieldYFrom = new JTextField("0");
        fieldYFrom.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setValue(e);
            }

            private void setValue(DocumentEvent e) {
                try {
                    if (e.getDocument().getLength() == 0) return;
                    Integer value = new Integer(e.getDocument().getText(0, e.getDocument().getLength()));
                    if (checkValue(value, 0, Math.min(invertedMask.getHeight() - 1, pointTo.y))) {
                        pointFrom.y = value;
                    } else {
                        pointFrom.y = 0;
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });
        fieldYFrom.setPreferredSize(new Dimension(100, 30));
        panelY.add(fieldYFrom);
        JLabel labelYTo = new JLabel(" to ");
        panelY.add(labelYTo);
        fieldYTo = new JTextField("0");
        fieldYTo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setValue(e);
            }

            private void setValue(DocumentEvent e) {
                try {
                    if (e.getDocument().getLength() == 0) return;
                    Integer value = new Integer(e.getDocument().getText(0, e.getDocument().getLength()));
                    if (checkValue(value, Math.max(pointFrom.y, 0), invertedMask.getHeight() - 1)) {
                        pointTo.y = value;
                    } else {
                        pointTo.y = invertedMask.getHeight() - 1;
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });
        fieldYTo.setPreferredSize(new Dimension(100, 30));
        panelY.add(fieldYTo);

        // ------------------------- Z axis settings
        JPanel panelZ = new JPanel();
        panelZ.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelZ.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(panelZ);

        JLabel labelZ = new JLabel("Z: from ");
        panelZ.add(labelZ);
        fieldZFrom = new JTextField("0");
        fieldZFrom.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setValue(e);
            }

            private void setValue(DocumentEvent e) {
                try {
                    if (e.getDocument().getLength() == 0) return;
                    Integer value = new Integer(e.getDocument().getText(0, e.getDocument().getLength()));
                    if (checkValue(value, 0, Math.min(invertedMask.getSliceCount() - 1, pointTo.sliceId))) {
                        pointFrom.sliceId = value;
                    } else {
                        pointFrom.sliceId = 0;
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });
        fieldZFrom.setPreferredSize(new Dimension(100, 30));
        panelZ.add(fieldZFrom);
        JLabel labelZTo = new JLabel(" to ");
        panelZ.add(labelZTo);
        fieldZTo = new JTextField("0");
        fieldZTo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setValue(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setValue(e);
            }

            private void setValue(DocumentEvent e) {
                try {
                    if (e.getDocument().getLength() == 0) return;
                    Integer value = new Integer(e.getDocument().getText(0, e.getDocument().getLength()));
                    if (checkValue(value, Math.max(pointFrom.sliceId, 0), invertedMask.getSliceCount() - 1)) {
                        pointTo.sliceId = value;
                    } else {
                        pointTo.sliceId = invertedMask.getSliceCount() - 1;
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });
        fieldZTo.setPreferredSize(new Dimension(100, 30));
        panelZ.add(fieldZTo);

        return settingsPanel;
    }

    private boolean checkValue(int value, int min, int max) {
        return (value >= min && value <= max);
    }
}
