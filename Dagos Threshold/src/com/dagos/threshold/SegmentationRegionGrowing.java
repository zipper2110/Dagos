package com.dagos.threshold;

import com.dagos.graphics.Image;
import com.dagos.graphics.Mask;
import com.dagos.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitry on 02.03.14.
 */
public class SegmentationRegionGrowing implements SegmentationI {

    private double seedPointMinSlice = 0.11;
    private double seedPointMaxSlice = 0.3;

    private int growingThreshold = 25;

    @Override
    public Mask getMask(Image image) {
        byte[][][] imageData = image.getImageData();

        List<Point> seedPoints = getSeedPoints(imageData);
        return new Mask(getSegment(imageData, growingThreshold, seedPoints));
    }

    private List<Point> getSeedPoints(byte[][][] imageData) {
        int sliceIndex = getSeedSliceIndex(imageData, seedPointMinSlice, seedPointMaxSlice);

        // on selected slice find seed points by mid line
        int imageY = imageData[0].length;
        int seedRowIndex = (int) Math.floor(imageY / 2);

        byte[] seedRow = new byte[imageData.length];
        int[] seedRowInt = new int[imageData.length];
        // get middle row of slice
        for (int i = 0; i < imageData.length; i++) {
            seedRow[i] = imageData[i][seedRowIndex][sliceIndex];
        }

        return _getLungPointsFromRow(seedRow, seedRowIndex, sliceIndex);
    }

    private List<Point> _getLungPointsFromRow(byte[] row, int y, int sliceId) {
        // amongst points of row find lung points
        // search for 2 biggest areas with the darkest color surrounded by lighter color areas

        // 1. find the darkest points in row, that are dark enough

        byte minRowColor = Byte.MAX_VALUE;
        for (int i = 0; i < row.length; i++) {
            if (row[i] < minRowColor) {
                minRowColor = row[i];
            }
        }

        List<Integer> darkPointsIndexes = new ArrayList<Integer>();
        for (int i = 0; i < row.length; i++) {
            if (row[i] <= minRowColor + growingThreshold) {
                darkPointsIndexes.add(i);
            }
        }

        // 2. check what of them are connected in areas, group them in areas
        List<List<Integer>> darkPointsAreas = new ArrayList<List<Integer>>();

        int index = 0;
        boolean isRowEnded = false;
        while (!isRowEnded) {
            List<Integer> darkPointsArea = new ArrayList<Integer>();
            for (; index < darkPointsIndexes.size(); index++) {
                boolean isPointInArea = false;
                darkPointsArea.add(darkPointsIndexes.get(index));

                // if dark point is next to next dark point then they are in same area
                if (index == darkPointsIndexes.size() - 1 || darkPointsIndexes.get(index + 1) == darkPointsIndexes.get(index) + 1) {
                    isPointInArea = true;
                }

                if (!isPointInArea) {
                    index++;
                    break;
                }
            }
            darkPointsAreas.add(darkPointsArea);
            if (index > darkPointsIndexes.size() - 1) isRowEnded = true;
        }

        // 3. leave only dark areas that has light pixels by sides of them (min width of light area)
        int minLightAreaWidth = 5;
        List<List<Integer>> darkPointsAreasFiltered = new ArrayList<List<Integer>>();

        for (int i = 0; i < darkPointsAreas.size(); i++) {
            List<Integer> area = darkPointsAreas.get(i);
            int areaFirstIndex = area.get(0);
            int areaLastIndex = area.get(area.size() - 1);
            int previousAreaLastIndex = -1;
            int nextAreaFirstIndex = y;
            if (i > 0) {
                previousAreaLastIndex = darkPointsAreas.get(i - 1).get(darkPointsAreas.get(i - 1).size() - 1);
            }
            if (i < darkPointsAreas.size() - 1) {
                nextAreaFirstIndex = darkPointsAreas.get(i + 1).get(0);
            }

            if (
                    (areaFirstIndex - previousAreaLastIndex > minLightAreaWidth) &&
                            (nextAreaFirstIndex - areaLastIndex > minLightAreaWidth)
                    ) {
                darkPointsAreasFiltered.add(area);
            }
        }

        // 4. leave only 1 biggest dark area (like one of two lungs)
        int biggestAreaSize = 0;
        int biggestAreaIndex = 0;
        for (int i = 0; i < darkPointsAreasFiltered.size(); i++) {
            if (darkPointsAreasFiltered.get(i).size() > biggestAreaSize) {
                biggestAreaSize = darkPointsAreasFiltered.get(i).size();
                biggestAreaIndex = i;
            }
        }
        List<Integer> seedArea = darkPointsAreasFiltered.get(biggestAreaIndex);

        // 5. Take the darkest point from that one area and use it as a seed point
        byte darkerstPointValue = Byte.MAX_VALUE;
        int darkestPointIndex = 0;
        for (int seedPointIndex : seedArea) {
            if (row[seedPointIndex] < darkerstPointValue) {
                darkestPointIndex = seedPointIndex;
                darkerstPointValue = row[seedPointIndex];
            }
        }

        Point seedPoint = new Point();
        seedPoint.x = darkestPointIndex;
        seedPoint.y = y;
        seedPoint.sliceId = sliceId;

        List<Point> seedPoints = new ArrayList<Point>();
        seedPoints.add(seedPoint);
        return seedPoints;
    }

    private Integer getSeedSliceIndex(byte[][][] imageData, double minSeedSlicePosition, double maxSeedSlicePosition) {
        // get slice index using min and max slice percentage
        int sliceCount = imageData[0][0].length;

        return (int) Math.floor(seedPointMaxSlice * sliceCount);
    }

    private boolean[][][] getSegment(byte[][][] imageData, int growingThreshold, List<Point> seedPoints) {
        byte initialValue = imageData[seedPoints.get(0).x][seedPoints.get(0).y][seedPoints.get(0).sliceId];
        boolean[][][] maskPoint = new boolean[imageData.length][imageData[0].length][imageData[0][0].length];
        boolean[][][] checkedPoints = new boolean[imageData.length][imageData[0].length][imageData[0][0].length];

        // while there are seed
        while (seedPoints.size() > 0) {
            List<Point> newSeedPoints = new ArrayList<Point>();
            // for each point from seed points
            for (Point seedPoint : seedPoints) {
                // check that seed point not checked yet
                if (!checkedPoints[seedPoint.x][seedPoint.y][seedPoint.sliceId]) {
                    if (_isPointInSegment(imageData, seedPoint, initialValue)) {
                        maskPoint[seedPoint.x][seedPoint.y][seedPoint.sliceId] = true;
                    }
                    // get neighbor points that are not checked yet
                    List<Point> neighborPoints = getNeighborPoints(seedPoint, imageData);
                    // for each point
                    for (Point neighborPoint : neighborPoints) {
                        // if point under threshold then put it in seed points (different variable)
                        // if not then ignore it
                        if (_isPointInSegment(imageData, neighborPoint, initialValue)) {
                            newSeedPoints.add(neighborPoint);
                        }
                    }
                    checkedPoints[seedPoint.x][seedPoint.y][seedPoint.sliceId] = true;
                }
                // remove point from seed points
            }
            seedPoints = newSeedPoints;
        }
        return maskPoint;
    }

    private boolean _isPointInSegment(byte[][][] imageData, Point point, int initialValue) {
        return imageData[point.x][point.y][point.sliceId] <= initialValue + growingThreshold;
    }

    private List<Point> getNeighborPoints(Point point, byte[][][] imageData) {
        // get 6 points
        List<Point> neighborPoints = new ArrayList<Point>();
        if (point.x > 0) neighborPoints.add(new Point(point.x - 1, point.y, point.sliceId));
        if (point.x < imageData.length - 1) neighborPoints.add(new Point(point.x + 1, point.y, point.sliceId));
        if (point.y > 0) neighborPoints.add(new Point(point.x, point.y - 1, point.sliceId));
        if (point.y < imageData[0].length - 1) neighborPoints.add(new Point(point.x, point.y + 1, point.sliceId));
        if (point.sliceId > 0) neighborPoints.add(new Point(point.x, point.y, point.sliceId - 1));
        if (point.sliceId < imageData[0][0].length - 1)
            neighborPoints.add(new Point(point.x, point.y, point.sliceId + 1));

        return neighborPoints;
    }
}
