package com.dagos.graphics.display.maskviewer.scene;

import com.dagos.graphics.Mask;
import com.dagos.graphics.Point;

import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Dmitry on 29.03.2014
 */
public class SceneBuilderPointTrianglesDebug extends SceneBuilder {

    TriangleArray voxels;
    Mask drawedPointsMask;
    Point lastUndrawedPoint = new Point(0, 0, 0);

    Set<Triangle> triangles = new LinkedHashSet<Triangle>();
    LinkedHashSet<Line> lines = new LinkedHashSet<Line>();

    public void findNewTriangle() {
        if (lines.size() == 0) {
            Point startPoint = getUndrawedPoint();
            if (startPoint == null) return;
            System.out.println(startPoint.x + ":" + startPoint.y + ":" + startPoint.sliceId);
            drawedPointsMask.setPointValue(startPoint, true);

            Point neighborPoint = getNeighborUndrawedPoint(startPoint);
            if (neighborPoint == null) return;
            drawedPointsMask.setPointValue(neighborPoint, true);

            Line startLine = new Line(startPoint, neighborPoint);
            lines.add(startLine);
        }

        Line currentLine = lines.iterator().next();
        Point closestPoint = getNeighborPoint(currentLine);
        if (closestPoint == null) {
            lines.remove(currentLine);
            return;
        }

        Triangle newTriangle = new Triangle(currentLine.start, currentLine.end, closestPoint);
        if (!triangles.contains(newTriangle)) {
            triangles.add(newTriangle);
            lines.add(new Line(currentLine.start, closestPoint, currentLine.end));
            lines.add(new Line(currentLine.end, closestPoint, currentLine.start));
        }
        lines.remove(currentLine);
        drawedPointsMask.setPointValue(closestPoint, true);

    }

    protected Shape3D getShape() {
        findNewTriangle();

        int numVerts = 9; // 3 triangles * 3 vertices
        int numVoxel = triangles.size();

        int vertexCount = numVerts * numVoxel;
        int vertexFormat = TriangleArray.COORDINATES | TriangleArray.NORMALS;

        // One geometry for all visible voxels
        voxels = new TriangleArray(vertexCount, vertexFormat);

        int voxelOffset = 0;

        float[] voxelCoords;
        //float[] voxelColors;
        float[] voxelNormals;

        for (Triangle triangle : triangles) {
            voxelCoords = getTriangleCoords(triangle, new Point(mask.getWitdh(), mask.getHeight(), mask.getSliceCount()));
            //voxelColors = getBoxTriaColors(0.5f, 0.5f, 0.5f/*image.getPointValueFloat(point), image.getPointValueFloat(point), image.getPointValueFloat(point)*/);
            voxelNormals = getTriangleNormals(triangle);

            voxels.setCoordinates(voxelOffset, voxelCoords);
            voxels.setNormals(voxelOffset, voxelNormals);
            //voxels.setColors(voxelOffset, voxelColors);

            voxelOffset += numVerts;
        }

        return new Shape3D(voxels);
    }

    private Point getUndrawedPoint() {
        for (int k = lastUndrawedPoint.sliceId; k < drawedPointsMask.getSliceCount(); k++) {
            for (int i = lastUndrawedPoint.x; i < drawedPointsMask.getWitdh(); i++) {
                for (int j = lastUndrawedPoint.y; j < drawedPointsMask.getHeight(); j++) {
                    Point point = new Point(i, j, k);
                    if (mask.getPointValue(point) && !drawedPointsMask.getPointValue(point)) return point;
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

    private Point getNeighborUndrawedPoint(Line line) {
        List<Point> startNeighborPoints = getNeighborPoints(line.start);
        List<Point> endNeighborPoints = getNeighborPoints(line.end);

        for (Point startNeighborPoint : startNeighborPoints) {
            if (endNeighborPoints.contains(startNeighborPoint) && !drawedPointsMask.getPointValue(startNeighborPoint)) {
                return startNeighborPoint;
            }
        }
        return null;
    }

    public void setMask(Mask mask) {
        super.setMask(mask);
        if (this.drawedPointsMask == null)
            this.drawedPointsMask = new Mask(new boolean[mask.getWitdh()][mask.getHeight()][mask.getSliceCount()]);
    }

    private float[] getTriangleCoords(Triangle triangle, Point maxValue) {
        return new float[]{
                triangle.point1.x - maxValue.x / 2, triangle.point1.y - maxValue.y / 2, triangle.point1.sliceId - maxValue.sliceId / 2,
                triangle.point2.x - maxValue.x / 2, triangle.point2.y - maxValue.y / 2, triangle.point2.sliceId - maxValue.sliceId / 2,
                triangle.point3.x - maxValue.x / 2, triangle.point3.y - maxValue.y / 2, triangle.point3.sliceId - maxValue.sliceId / 2
        };
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
            if (start != null) code += start.x + start.y + start.sliceId;
            if (end != null) code += end.x + end.y + end.sliceId;
            return code;
        }

        public boolean equals(Object object) {
            if (object != null) {
                if (this.getClass() == object.getClass()) {
                    Line line = (Line) object;

                    boolean startEquals = pointsEqual(this.start, line.start);
                    boolean endEquals = pointsEqual(this.end, line.end);

                    return startEquals && endEquals;
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
            if (point1 != null) code += point1.x + point1.y + point1.sliceId;
            if (point2 != null) code += point2.x + point2.y + point2.sliceId;
            if (point3 != null) code += point3.x + point3.y + point3.sliceId;
            return code;
        }

        public boolean equals(Object object) {
            if (object != null) {
                if (this.getClass() == object.getClass()) {
                    Triangle triangle = (Triangle) object;

                    boolean point1Equals = pointsEqual(this.point1, triangle.point1);
                    boolean point2Equals = pointsEqual(this.point2, triangle.point2);
                    boolean point3Equals = pointsEqual(this.point3, triangle.point3);

                    return point1Equals && point2Equals && point3Equals;
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
}