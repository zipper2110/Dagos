package com.dagos.graphics.display.maskviewer.scene;

import com.dagos.graphics.Mask;
import com.dagos.graphics.Point;

import javax.media.j3d.Appearance;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;
import java.util.*;

/**
 * Created by Dmitry on 29.03.2014
 */
public class SceneBuilderPointTriangles extends SceneBuilder {

    TriangleArray voxels;
    Mask drawedPointsMask;
    Point lastUndrawedPoint = new Point(0, 0, 0);


    protected Shape3D getShape() {
        Date startTime = Calendar.getInstance().getTime();
        lastUndrawedPoint = new Point(0, 0, 0);
        Collection<Triangle> triangles = new HashSet<Triangle>();
        List<Line> lines = new ArrayList<Line>();
        int i = 0;
        while (i < 1000) {
            i++;
            Point startPoint = getUndrawedPoint();
            if (startPoint == null) break;
            System.out.println(i);
            System.out.println(startPoint.x + ":" + startPoint.y + ":" + startPoint.sliceId);
            drawedPointsMask.setPointValue(startPoint, true);

            Point neighborPoint = getNeighborPoint(startPoint);
            if (neighborPoint == null) continue;
            drawedPointsMask.setPointValue(neighborPoint, true);

            Line startLine = new Line(startPoint, neighborPoint);
            lines.add(startLine);
            while (lines.size() > 0) {
                Line currentLine = lines.get(0);
                lines.remove(0);

                Collection<Point> closestPoints = getNeighborPoints(currentLine);
                for (Point lineNeighborPoint : closestPoints) {
                    Triangle newTriangle = new Triangle(currentLine.start, currentLine.end, lineNeighborPoint);
                    if (currentLine.point3 == null ||
                            !(triangleIntersectLine(newTriangle, new Line(currentLine.start, currentLine.point3)) ||
                                    triangleIntersectLine(newTriangle, new Line(currentLine.end, currentLine.point3)))) {
                        if (triangles.add(newTriangle)) {
                            lines.add(new Line(currentLine.start, lineNeighborPoint, currentLine.end));
                            lines.add(new Line(currentLine.end, lineNeighborPoint, currentLine.start));
                            drawedPointsMask.setPointValue(lineNeighborPoint, true);
                            break;
                        }
                    }
                }
            }
        }
        Date endTime = Calendar.getInstance().getTime();
        long time = endTime.getTime() - startTime.getTime();
        System.out.println("Operation performed in " + time / 1000 + " sec");

        int numVerts = 3; // 3 vertices * 1 triangle
        int numVoxel = triangles.size();

        int vertexCount = numVerts * numVoxel;
        int vertexFormat = TriangleArray.COORDINATES | TriangleArray.NORMALS /*| TriangleArray.COLOR_3*/;

        // One geometry for all visible voxels
        voxels = new TriangleArray(vertexCount, vertexFormat);

        int voxelOffset = 0;

        float[] voxelCoords;
        //float[] voxelColors;
        float[] voxelNormals;

        Point boundsPoint = new Point(mask.getWitdh(), mask.getHeight(), mask.getSliceCount());
        for (Triangle triangle : triangles) {
            voxelCoords = getTriangleCoords(triangle, boundsPoint);
            voxelNormals = getTriangleNormals(triangle);
            //voxelColors = getTriangleColors((float) Math.random(), (float) Math.random(), (float) Math.random());

            voxels.setCoordinates(voxelOffset, voxelCoords);
            voxels.setNormals(voxelOffset, voxelNormals);
            //voxels.setColors(voxelOffset, voxelColors);

            voxelOffset += numVerts;
        }

        return new Shape3D(voxels);
    }

    private boolean trianglesIntersect(Triangle triangle1, Triangle triangle2) {
        return linesIntersect(triangle1.point1, triangle1.point2, triangle2.point1, triangle2.point2) ||
                linesIntersect(triangle1.point1, triangle1.point2, triangle2.point2, triangle2.point3) ||
                linesIntersect(triangle1.point1, triangle1.point2, triangle2.point3, triangle2.point1) ||

                linesIntersect(triangle1.point2, triangle1.point3, triangle2.point1, triangle2.point2) ||
                linesIntersect(triangle1.point2, triangle1.point3, triangle2.point2, triangle2.point3) ||
                linesIntersect(triangle1.point2, triangle1.point3, triangle2.point3, triangle2.point1) ||

                linesIntersect(triangle1.point3, triangle1.point1, triangle2.point1, triangle2.point2) ||
                linesIntersect(triangle1.point3, triangle1.point1, triangle2.point2, triangle2.point3) ||
                linesIntersect(triangle1.point3, triangle1.point1, triangle2.point3, triangle2.point1);
    }

    private boolean triangleIntersectLine(Triangle triangle, Line line) {
        return linesIntersect(triangle.point1, triangle.point2, line.start, line.end) ||
                linesIntersect(triangle.point2, triangle.point3, line.start, line.end) ||
                linesIntersect(triangle.point3, triangle.point1, line.start, line.end);
    }

    private boolean linesIntersect(Point line1Point1, Point line1Point2, Point line2Point1, Point line2Point2) {
        if (line1Point1.equals(line2Point1) ||
                line1Point2.equals(line2Point2) ||
                line1Point1.equals(line2Point2) ||
                line1Point2.equals(line2Point1)
                ) {
            return false;
        }

        Point vector1 = new Point(line1Point2.x - line1Point1.x, line1Point2.y - line1Point1.y, line1Point2.sliceId - line1Point1.sliceId);
        Point vector2 = new Point(line2Point2.x - line2Point1.x, line2Point2.y - line2Point1.y, line2Point2.sliceId - line2Point1.sliceId);
        Point vector3 = new Point(line2Point1.x - line1Point2.x, line2Point1.y - line1Point2.y, line2Point1.sliceId - line1Point2.sliceId);
//        Vector3f v1 = new Vector3f((line1Point2.x - line1Point1.x) * 0.9f, (line1Point2.y - line1Point1.y) * 0.9f, (line1Point2.sliceId - line1Point1.sliceId) * 0.9f);
//        Vector3f v2 = new Vector3f(line2Point1.x - line1Point2.x, line2Point1.y - line1Point2.y, line2Point1.sliceId - line1Point2.sliceId);
//        Vector3f v3 = new Vector3f(line2Point1.x - line1Point2.x, line2Point1.y - line1Point2.y, line2Point1.sliceId - line1Point2.sliceId);

        // parallel lines
        if (vector1.x == vector2.x && vector1.y == vector2.y && vector1.sliceId == vector2.sliceId) {
            return false;
        }
        if (vector1.x == -vector2.x && vector1.y == -vector2.y && vector1.sliceId == -vector2.sliceId) {
            return false;
        }

        int determinant =
                vector1.x * vector2.y * vector3.sliceId -
                        vector1.x * vector3.y * vector2.sliceId +
                        vector3.x * vector1.y * vector2.sliceId -
                        vector2.x * vector1.y * vector3.sliceId +
                        vector2.x * vector3.y * vector1.sliceId -
                        vector3.x * vector2.y * vector1.sliceId;
        if (determinant == 0) {
            if (Math.max(line1Point1.x, line1Point2.x) < Math.min(line2Point1.x, line2Point2.x) ||
                    Math.max(line2Point1.x, line2Point2.x) < Math.min(line1Point1.x, line1Point2.x)) {
                return false;
            }
            if (Math.max(line1Point1.y, line1Point2.y) < Math.min(line2Point1.y, line2Point2.y) ||
                    Math.max(line2Point1.y, line2Point2.y) < Math.min(line1Point1.y, line1Point2.y)) {
                return false;
            }
            if (Math.max(line1Point1.sliceId, line1Point2.sliceId) < Math.min(line2Point1.sliceId, line2Point2.sliceId) ||
                    Math.max(line2Point1.sliceId, line2Point2.sliceId) < Math.min(line1Point1.sliceId, line1Point2.sliceId)) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private Point getUndrawedPoint() {
        for (int k = lastUndrawedPoint.sliceId; k < drawedPointsMask.getSliceCount(); k++) {
            for (int i = lastUndrawedPoint.x; i < drawedPointsMask.getWitdh(); i++) {
                for (int j = lastUndrawedPoint.y; j < drawedPointsMask.getHeight(); j++) {
                    Point point = new Point(i, j, k);
                    if (mask.getPointValue(point) && !drawedPointsMask.getPointValue(point)) {
                        lastUndrawedPoint = point;
                        return point;
                    }
                }
            }
        }
        return null;
    }

    private Point getNeighborUndrawedPoint(Point closestToPoint) {
        List<Point> neighborPoints = getNeighborPoints(closestToPoint);
        for (Point point : neighborPoints) {
            if (!drawedPointsMask.getPointValue(point)) return point;
        }
        return null;
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

        tempNeighborPoints.add(new Point(point.x + 1, point.y + 1, point.sliceId));
        tempNeighborPoints.add(new Point(point.x + 1, point.y - 1, point.sliceId));
        tempNeighborPoints.add(new Point(point.x - 1, point.y + 1, point.sliceId));
        tempNeighborPoints.add(new Point(point.x - 1, point.y - 1, point.sliceId));

        tempNeighborPoints.add(new Point(point.x + 1, point.y, point.sliceId + 1));
        tempNeighborPoints.add(new Point(point.x + 1, point.y, point.sliceId - 1));
        tempNeighborPoints.add(new Point(point.x - 1, point.y, point.sliceId + 1));
        tempNeighborPoints.add(new Point(point.x - 1, point.y, point.sliceId - 1));

        tempNeighborPoints.add(new Point(point.x, point.y + 1, point.sliceId + 1));
        tempNeighborPoints.add(new Point(point.x, point.y + 1, point.sliceId - 1));
        tempNeighborPoints.add(new Point(point.x, point.y - 1, point.sliceId + 1));
        tempNeighborPoints.add(new Point(point.x, point.y - 1, point.sliceId - 1));

        tempNeighborPoints.add(new Point(point.x + 1, point.y + 1, point.sliceId + 1));
        tempNeighborPoints.add(new Point(point.x + 1, point.y + 1, point.sliceId - 1));
        tempNeighborPoints.add(new Point(point.x + 1, point.y - 1, point.sliceId + 1));
        tempNeighborPoints.add(new Point(point.x + 1, point.y - 1, point.sliceId - 1));
        tempNeighborPoints.add(new Point(point.x - 1, point.y + 1, point.sliceId + 1));
        tempNeighborPoints.add(new Point(point.x - 1, point.y + 1, point.sliceId - 1));
        tempNeighborPoints.add(new Point(point.x - 1, point.y - 1, point.sliceId + 1));
        tempNeighborPoints.add(new Point(point.x - 1, point.y - 1, point.sliceId - 1));

        for (Point neighborPoint : tempNeighborPoints) {
            if (mask.hasPoint(neighborPoint)) {
                neighborPoints.add(neighborPoint);
            }
        }
        return neighborPoints;
    }

    private Point getNeighborPoint(Point point) {
        List<Point> neighborPoints = getNeighborPoints(point);

        if (neighborPoints.size() > 0) return neighborPoints.get(0);
        else return null;
    }

    private Point getNeighborPoint(Line line) {
        List<Point> startNeighborPoints = getNeighborPoints(line.start);
        List<Point> endNeighborPoints = getNeighborPoints(line.end);

        for (Point startNeighborPoint : startNeighborPoints) {
            for (Point endNeighborPoint : endNeighborPoints) {
                if (startNeighborPoint.equals(endNeighborPoint)) {
                    if (line.point3 == null || !line.point3.equals(endNeighborPoint)) {
                        return startNeighborPoint;
                    }
                }
            }
        }
        return null;
    }

    private Collection<Point> getNeighborPoints(Line line) {
        List<Point> startNeighborPoints = getNeighborPoints(line.start);
        List<Point> endNeighborPoints = getNeighborPoints(line.end);

        List<Point> lineNeighborPoints = new ArrayList<Point>();

        for (Point startNeighborPoint : startNeighborPoints) {
            for (Point endNeighborPoint : endNeighborPoints) {
                if (startNeighborPoint.equals(endNeighborPoint)) {
                    if (line.point3 == null || !line.point3.equals(endNeighborPoint)) {
                        lineNeighborPoints.add(endNeighborPoint);
                    }
                }
            }
        }

//        return sortLineNeighborPoints(line, lineNeighborPoints);
        return lineNeighborPoints;
    }

    private Collection<Point> sortLineNeighborPoints(final Line line, List<Point> lineNeighborPoints) {
        if (line.point3 == null) return lineNeighborPoints;
        final Point point3LineDistance = getDistance(line, line.point3);
        Set<Point> sortedSet = new TreeSet<Point>(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                Point lineDistance1 = getDistance(line, o1);
                Point lineDistance2 = getDistance(line, o2);
                int pointDistance1 =
                        Math.abs(point3LineDistance.x - lineDistance1.x) +
                                Math.abs(point3LineDistance.y - lineDistance1.y) +
                                Math.abs(point3LineDistance.sliceId - lineDistance1.sliceId);
                int pointDistance2 =
                        Math.abs(point3LineDistance.x - lineDistance2.x) +
                                Math.abs(point3LineDistance.y - lineDistance2.y) +
                                Math.abs(point3LineDistance.sliceId - lineDistance2.sliceId);

                if (pointDistance1 < pointDistance2) return 1;
                else return -1;
            }
        });
        sortedSet.addAll(lineNeighborPoints);
        return sortedSet;
    }

    private Point getDistance(Line line, Point point) {
        return new Point(
                line.start.x + line.end.x - 2 * point.x,
                line.start.y + line.end.y - 2 * point.y,
                line.start.sliceId + line.end.sliceId - 2 * point.sliceId);
    }

    public void setMask(Mask mask) {
        super.setMask(mask);
        this.drawedPointsMask = new Mask(new boolean[mask.getWitdh()][mask.getHeight()][mask.getSliceCount()]);
    }

    private float[] getTriangleCoords(Triangle triangle, Point maxValue) {
        return new float[]{
                triangle.point1.x - maxValue.x / 2, triangle.point1.y - maxValue.y / 2, triangle.point1.sliceId - maxValue.sliceId / 2,
                triangle.point2.x - maxValue.x / 2, triangle.point2.y - maxValue.y / 2, triangle.point2.sliceId - maxValue.sliceId / 2,
                triangle.point3.x - maxValue.x / 2, triangle.point3.y - maxValue.y / 2, triangle.point3.sliceId - maxValue.sliceId / 2
        };
    }

    // (r, g, b) = color
    private float[] getTriangleColors(float r, float g, float b) {
        float[] colors = {
                r, g, b, r, g, b, r, g, b
        };
        return colors;
    }

    private float[] getTriangleNormals(Triangle triangle) {

        Point p1 = triangle.point1;
        Point p2 = triangle.point2;
        Point p3 = triangle.point3;

        float wrki;
        Point v1 = new Point(), v2 = new Point();

        v1.x = p1.x - p2.x;
        v1.y = p1.y - p2.y;
        v1.sliceId = p1.sliceId - p2.sliceId;

        v2.x = p2.x - p3.x;
        v2.y = p2.y - p3.y;
        v2.sliceId = p2.sliceId - p3.sliceId;

        wrki = new Double(
                Math.sqrt(Math.pow(v1.y * v2.sliceId - v1.sliceId * v2.y, 2) +
                        Math.pow(v1.sliceId * v2.x - v1.x * v2.sliceId, 2) +
                        Math.pow(v1.x * v2.y - v1.y * v2.x, 2))
        ).floatValue();

        float normalX = (v1.y * v2.sliceId - v1.sliceId * v2.y) / wrki;
        float normalY = (v1.sliceId * v2.x - v1.x * v2.sliceId) / wrki;
        float normalZ = (v1.x * v2.y - v1.y * v2.x) / wrki;
        return new float[]{
                normalX, normalY, normalZ,
                normalX, normalY, normalZ,
                normalX, normalY, normalZ
        };
    }

    public class Line {
        public Point start;
        public Point end;

        public Point point3 = null;

        public boolean used = false;

        public Line(Point start, Point end) {
            this.start = start;
            this.end = end;
        }

        public Line(Point start, Point end, Point point3) {
            this(start, end);
            this.point3 = point3;
        }

        public int hashCode() {
            int code = 0;
            if (start != null) code += start.hashCode();
            if (end != null) code += end.hashCode();
            return code;
        }

//        public boolean equals(Object object) {
//            if (object != null) {
//                if (this.getClass() == object.getClass()) {
//                    Line line = (Line) object;
//
//                    boolean startEquals = pointsEqual(this.start, line.start);
//                    boolean endEquals = pointsEqual(this.end, line.end);
//
//                    return startEquals && endEquals;
//                }
//            }
//            return false;
//        }

        public boolean equals(Object object) {
            if (object != null) {
                if (this.getClass() == object.getClass()) {
                    Line line = (Line) object;

                    if (this.start.equals(line.start) || this.start.equals(line.end)) {
                        if (this.end.equals(line.start) || this.end.equals(line.end)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public boolean pointsEqual(Point point1, Point point2) {
            if (point1 == null && point2 == null) {
                return true;
            } else if (point1 != null && point2 != null) {
                if (point1.equals(point2)) return true;
            }
            return false;
        }
    }

    public class Triangle {
        public Point point1;
        public Point point2;
        public Point point3;

        public Triangle(Point point1, Point point2, Point point3) {
            this.point1 = point1;
            this.point2 = point2;
            this.point3 = point3;
        }

        public int hashCode() {
            int code = 0;
            if (point1 != null) code += point1.hashCode();
            if (point2 != null) code += point2.hashCode();
            if (point3 != null) code += point3.hashCode();
            return code;
        }

        public boolean equals(Object object) {
            if (object != null) {
                if (this.getClass() == object.getClass()) {
                    Triangle triangle = (Triangle) object;

                    if (
                            this.point1.equals(triangle.point1) ||
                                    this.point1.equals(triangle.point2) ||
                                    this.point1.equals(triangle.point3)
                            ) {
                        if (
                                this.point2.equals(triangle.point1) ||
                                        this.point2.equals(triangle.point2) ||
                                        this.point2.equals(triangle.point3)
                                ) {
                            if (
                                    this.point3.equals(triangle.point1) ||
                                            this.point3.equals(triangle.point2) ||
                                            this.point3.equals(triangle.point3)
                                    ) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

//        public boolean equals(Object object) {
//            if (object != null) {
//                if (this.getClass() == object.getClass()) {
//                    Triangle triangle = (Triangle) object;
//
//                    boolean point1Equals = pointsEqual(this.point1, triangle.point1);
//                    boolean point2Equals = pointsEqual(this.point2, triangle.point2);
//                    boolean point3Equals = pointsEqual(this.point3, triangle.point3);
//
//                    return point1Equals && point2Equals && point3Equals;
//                }
//            }
//            return false;
//        }

        public boolean pointsEqual(Point point1, Point point2) {
            if (point1 == null && point2 == null) {
                return true;
            } else if (point2 != null && point1 != null) {
                if (point1.equals(point2)) return true;
            }
            return false;
        }
    }


    @Override
    protected Appearance getAppearance() {
        Appearance app = super.getAppearance();
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace(PolygonAttributes.CULL_NONE);
        app.setPolygonAttributes(pa);
        return app;
    }
}