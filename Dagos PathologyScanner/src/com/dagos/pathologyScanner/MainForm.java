package com.dagos.pathologyScanner;

import com.dagos.graphics.Mask;
import com.dagos.graphics.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Dmitry on 15.04.2014
 */
public class MainForm extends com.dagos.MainForm {

    private Mask dagosMask;
    private Mask pathologyMask;
    private List<Mask> pathologyMasks;
    private Mask usedPointsMask;
    Point lastUnusedPoint = new Point(0, 0, 0);

    public void createForm() {
        setLayout(new FlowLayout());

        JButton openImageButton = new JButton("Open Dagos mask");
        openImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File importMaskFile = importFile();
                if (importMaskFile != null) {
                    try {
                        dagosMask = new Mask(importMaskFile);
                        usedPointsMask = new Mask(new boolean[dagosMask.getWitdh()][dagosMask.getHeight()][dagosMask.getSliceCount()]);
                        pathologyMask = new Mask(new boolean[dagosMask.getWitdh()][dagosMask.getHeight()][dagosMask.getSliceCount()]);
                        JOptionPane.showMessageDialog(null, "Mask file was imported from " + importMaskFile);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Image file wasn't imported from " + importMaskFile + ": " + e1.getMessage());
                        e1.printStackTrace();
                    }
                }
            }
        });
        getContentPane().add(openImageButton);

        JButton applyThresholdButton = new JButton("Scan for pathologies");
        applyThresholdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scanForPathologies();
                JOptionPane.showMessageDialog(null, "Scan performed successfully!");
            }
        });
        getContentPane().add(applyThresholdButton);

        JButton saveMaskButton = new JButton("Save pathology mask");
        saveMaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDagosMask(pathologyMask);
                JOptionPane.showMessageDialog(null, "Pathology mask saved successfully!");
            }
        });
        getContentPane().add(saveMaskButton);

        JButton saveMasksButton = new JButton("Save pathology masks");
        saveMaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDagosMasks(pathologyMasks);
                JOptionPane.showMessageDialog(null, "Pathology masks saved successfully!");
            }
        });
        getContentPane().add(saveMaskButton);
    }

//    private void scanForPathologies() {
//        List<Mask> masks = new ArrayList<Mask>();
//
//        while(true) {
//            List<Point> currentPointsGroup;
//            // get unused cell
//            Point unusedPoint = getUnusedPoint();
//
//            if(unusedPoint != null) {
//                currentPointsGroup = new ArrayList<Point>();
//                currentPointsGroup.add(unusedPoint);
//                masks.add(new Mask(getSegment(dagosMask.getMaskData(), currentPointsGroup)));
//            } else {
//                break;
//            }
//        }
//
//        // throw out the biggest group (outer lung space)
//        int maxGroupSize = 0;
//        Mask maxMask = null;
//        for(Mask mask : masks) {
//            int size = mask.getPointsCount();
//            if(size > maxGroupSize) {
//                maxGroupSize = size;
//                maxMask = mask;
//            }
//        }
//
//        if(maxMask != null) {
//            masks.remove(maxMask);
//        }
//
//        // put points from group to mask
//        for(Mask mask : masks) {
//            for (int k = 0; k < mask.getSliceCount(); k++) {
//                for (int i = 0; i < mask.getWitdh(); i++) {
//                    for (int j = 0; j < mask.getHeight(); j++) {
//                        if(mask.getPointValue(i, j, k)) {
//                            pathologyMask.setPointValue(i, j, k, true);
//                        }
//                    }
//                }
//            }
//        }
//    }

    private void scanForPathologies() {
        List<Mask> masks = new ArrayList<Mask>();

        while (true) {
            Mask currentMask;
            Set<Point> newPoints;
            // get unused cell
            Point unusedPoint = getUnusedPoint();

            if (unusedPoint != null) {
                currentMask = new Mask(new boolean[usedPointsMask.getWitdh()][usedPointsMask.getHeight()][usedPointsMask.getSliceCount()]);
                newPoints = new HashSet<Point>();
                newPoints.add(unusedPoint);

                // find neighbor cells for it until there is no more
                while (newPoints.size() > 0) {
                    Set<Point> _points = new HashSet<Point>();
                    for (Point point : newPoints) {
                        usedPointsMask.setPointValue(point, true);
                        currentMask.setPointValue(point, true);
                    }
                    for (Point point : newPoints) {
//                        currentPointsGroup.add(point);
                        _points.addAll(getNeighborPoints(point));
                    }
                    newPoints = _points;
                }
                // put cells in list
                masks.add(currentMask);
            } else {
                break;
            }
        }

        // throw out the biggest group (outer lung space)
        int maxGroupSize = 0;
        Mask maxGroup = null;
        for (Mask pointsGroup : masks) {
            int pointsCount = pointsGroup.getPointsCount();
            if (pointsCount > maxGroupSize) {
                maxGroup = pointsGroup;
                maxGroupSize = pointsCount;
            }
        }

        if (maxGroup != null) {
            masks.remove(maxGroup);
            // put points from group to mask
            this.pathologyMasks = masks;
            for (Mask pointsGroup : masks) {
                for (int k = 0; k < pointsGroup.getSliceCount(); k++) {
                    for (int i = 0; i < pointsGroup.getWitdh(); i++) {
                        for (int j = 0; j < pointsGroup.getHeight(); j++) {
                            if (pointsGroup.getPointValue(i, j, k)) {
                                pathologyMask.setPointValue(i, j, k, true);
                            }
                        }
                    }
                }
            }
        }
    }

    private List<Point> getNeighborPoints(Point point) {
        List<Point> tempNeighborPoints = new ArrayList<Point>();
        List<Point> neighborPoints = new ArrayList<Point>();

        tempNeighborPoints.add(new Point(point.x + 1, point.y, point.sliceId));
        tempNeighborPoints.add(new Point(point.x - 1, point.y, point.sliceId));

        tempNeighborPoints.add(new Point(point.x, point.y + 1, point.sliceId));
        tempNeighborPoints.add(new Point(point.x, point.y - 1, point.sliceId));

        tempNeighborPoints.add(new Point(point.x, point.y, point.sliceId + 1));
        tempNeighborPoints.add(new Point(point.x, point.y, point.sliceId - 1));

//        tempNeighborPoints.add(new Point(point.x + 1, point.y + 1, point.sliceId));
//        tempNeighborPoints.add(new Point(point.x + 1, point.y - 1, point.sliceId));
//        tempNeighborPoints.add(new Point(point.x - 1, point.y + 1, point.sliceId));
//        tempNeighborPoints.add(new Point(point.x - 1, point.y - 1, point.sliceId));
//
//        tempNeighborPoints.add(new Point(point.x + 1, point.y, point.sliceId + 1));
//        tempNeighborPoints.add(new Point(point.x + 1, point.y, point.sliceId - 1));
//        tempNeighborPoints.add(new Point(point.x - 1, point.y, point.sliceId + 1));
//        tempNeighborPoints.add(new Point(point.x - 1, point.y, point.sliceId - 1));
//
//        tempNeighborPoints.add(new Point(point.x, point.y + 1, point.sliceId + 1));
//        tempNeighborPoints.add(new Point(point.x, point.y + 1, point.sliceId - 1));
//        tempNeighborPoints.add(new Point(point.x, point.y - 1, point.sliceId + 1));
//        tempNeighborPoints.add(new Point(point.x, point.y - 1, point.sliceId - 1));
//
//        tempNeighborPoints.add(new Point(point.x + 1, point.y + 1, point.sliceId + 1));
//        tempNeighborPoints.add(new Point(point.x + 1, point.y + 1, point.sliceId - 1));
//        tempNeighborPoints.add(new Point(point.x + 1, point.y - 1, point.sliceId + 1));
//        tempNeighborPoints.add(new Point(point.x + 1, point.y - 1, point.sliceId - 1));
//        tempNeighborPoints.add(new Point(point.x - 1, point.y + 1, point.sliceId + 1));
//        tempNeighborPoints.add(new Point(point.x - 1, point.y + 1, point.sliceId - 1));
//        tempNeighborPoints.add(new Point(point.x - 1, point.y - 1, point.sliceId + 1));
//        tempNeighborPoints.add(new Point(point.x - 1, point.y - 1, point.sliceId - 1));

        for (Point neighborPoint : tempNeighborPoints) {
            if (dagosMask.hasPoint(neighborPoint) && !dagosMask.getPointValue(neighborPoint) && !usedPointsMask.getPointValue(neighborPoint)) {
                neighborPoints.add(neighborPoint);
            }
        }
        return neighborPoints;
    }

    private Point getUnusedPoint() {
        for (int k = lastUnusedPoint.sliceId; k < usedPointsMask.getSliceCount(); k++) {
            for (int i = lastUnusedPoint.x; i < usedPointsMask.getWitdh(); i++) {
                for (int j = lastUnusedPoint.y; j < usedPointsMask.getHeight(); j++) {
                    Point point = new Point(i, j, k);
                    if (!dagosMask.getPointValue(point) && !usedPointsMask.getPointValue(point)) {
                        lastUnusedPoint = point;
                        return point;
                    }
                }
            }
        }
        return null;
    }

    private boolean[][][] getSegment(boolean[][][] maskData, List<Point> seedPoints) {
        boolean initialValue = maskData[seedPoints.get(0).x][seedPoints.get(0).y][seedPoints.get(0).sliceId];
        boolean[][][] pathologyMask = new boolean[maskData.length][maskData[0].length][maskData[0][0].length];
        boolean[][][] checkedPoints = new boolean[maskData.length][maskData[0].length][maskData[0][0].length];

        // while there are seed points
        while (seedPoints.size() > 0) {
            List<Point> newSeedPoints = new ArrayList<Point>();
            // for each point from seed points
            for (Point seedPoint : seedPoints) {
                // check that seed point not checked yet
                if (!checkedPoints[seedPoint.x][seedPoint.y][seedPoint.sliceId]) {
                    if (_isPointInSegment(maskData, seedPoint, initialValue)) {
                        pathologyMask[seedPoint.x][seedPoint.y][seedPoint.sliceId] = true;
                    }
                    // get neighbor points that are not checked yet
                    List<Point> neighborPoints = getNeighborPoints(seedPoint, maskData, checkedPoints);
                    // for each point
                    for (Point neighborPoint : neighborPoints) {
                        // if point under threshold then put it in seed points (different variable)
                        // if not then ignore it
                        if (_isPointInSegment(maskData, neighborPoint, initialValue)) {
                            newSeedPoints.add(neighborPoint);
                        }
                    }
                    checkedPoints[seedPoint.x][seedPoint.y][seedPoint.sliceId] = true;
                }
                // remove point from seed points
            }
            seedPoints = newSeedPoints;
        }
        return pathologyMask;
    }

    private boolean _isPointInSegment(boolean[][][] maskData, Point point, boolean initialValue) {
        return maskData[point.x][point.y][point.sliceId] == initialValue;
    }

    private List<Point> getNeighborPoints(Point point, boolean[][][] maskData, boolean[][][] checkedPoints) {
        // get 6 points
        List<Point> _neighborPoints = new ArrayList<Point>();
        if (point.x > 0) _neighborPoints.add(new Point(point.x - 1, point.y, point.sliceId));
        if (point.x < maskData.length - 1) _neighborPoints.add(new Point(point.x + 1, point.y, point.sliceId));
        if (point.y > 0) _neighborPoints.add(new Point(point.x, point.y - 1, point.sliceId));
        if (point.y < maskData[0].length - 1) _neighborPoints.add(new Point(point.x, point.y + 1, point.sliceId));
        if (point.sliceId > 0) _neighborPoints.add(new Point(point.x, point.y, point.sliceId - 1));
        if (point.sliceId < maskData[0][0].length - 1)
            _neighborPoints.add(new Point(point.x, point.y, point.sliceId + 1));

        List<Point> neighborPoints = new ArrayList<Point>();
        for (Point neighborPoint : _neighborPoints) {
            if (!checkedPoints[neighborPoint.x][neighborPoint.y][neighborPoint.sliceId])
                neighborPoints.add(neighborPoint);
        }


        return neighborPoints;
    }

    private void fillBackground(boolean[][][] maskData, Point seedPoint) {
        // find neighbor points that has "false" value
        // set them to "true"

    }
}
