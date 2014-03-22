package com.dagos.graphics;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.universe.SimpleUniverse;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

/**
 * Created by Dmitry on 02.03.14.
 */
public class MaskViewer extends Canvas3D {

    private TransformGroup tg2;
    private SimpleUniverse universe;

    private double lastZoom = 0.5f;

    public MaskViewer(GraphicsConfiguration config) {
        super(config);
        setStereoEnable(true);


        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double rotation = 0;
                if (e.getWheelRotation() > 0) {
                    rotation = -0.1f;
                } else {
                    rotation = 0.1f;
                }
                Transform3D temp = new Transform3D();
                tg2.getTransform(temp);
                Transform3D tempDelta = new Transform3D();
                tempDelta.setScale(tempDelta.getScale() + rotation);
                temp.mul(tempDelta);
                lastZoom = temp.getScale();
                tg2.setTransform(temp);
            }
        });
    }

    public void displayMask(Mask mask, Image image, Point pointFrom, Point pointTo) {
        if (universe != null) {
            universe.cleanup();
        }
        universe = new SimpleUniverse(this);
        universe.getViewingPlatform().setNominalViewingTransform();
        universe.addBranchGraph(getCanvasContent(getVisiblePoints(mask), mask, image, pointFrom, pointTo));
    }

    private java.util.List<Point> getVisiblePoints(Mask mask) {
        java.util.List<Point> visiblePoints = new ArrayList<Point>();

        for (int k = 0; k < mask.getSliceCount(); k++) {
            for (int i = 0; i < mask.getWitdh(); i++) {
                for (int j = 0; j < mask.getHeight(); j++) {
                    if (mask.getPointValue(i, j, k)) {
                        visiblePoints.add(new Point(i, j, k));
                    }
                }
            }
        }

        return visiblePoints;
    }

    private BranchGroup getCanvasContent(java.util.List<Point> visiblePoints, Mask mask, Image image, Point pointFrom, Point pointTo) {
        BranchGroup content = new BranchGroup();


        tg2 = new TransformGroup();
        tg2.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tg2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        Shape3D lungShape = getVisibleVoxelsQuad(visiblePoints, mask, image, pointFrom, pointTo);
        lungShape.setPickable(true);
        tg2.addChild(lungShape);

        MouseRotate f1 = new MouseRotate();
        f1.setSchedulingBounds(new BoundingSphere());
        f1.setTransformGroup(tg2);
        content.addChild(f1);

        content.addChild(tg2);

        Transform3D temp = new Transform3D();
        tg2.getTransform(temp);
        temp.setScale(lastZoom);
        tg2.setTransform(temp);

        Background background = new Background(new Color3f(1f, 1f, 1f));
        BoundingSphere sphere = new BoundingSphere(new Point3d(0, 0, 0), 100000);
        background.setApplicationBounds(sphere);
        content.addChild(background);

        BoundingSphere bounds = new BoundingSphere(new Point3d(0, 0, 0), 500);
        Color3f lightColor = new Color3f(1.0f, 1.0f, 1.0f);
        Vector3f light1Direction = new Vector3f(0.0f, 1.0f, 0.0f);
        /*
        DirectionalLight light1  = new DirectionalLight (lightColor, light1Direction);
        light1.setInfluencingBounds (bounds);
        tg2.addChild (light1);*/
/*
        PointLight light2 = new PointLight();
        light2.setEnable( true );
        light2.setAttenuation(1.0f, 0.0f, 0.0f);
        light2.setPosition(0, 0, -200);
        light2.setColor(lightColor);
        light2.setInfluencingBounds( bounds );
        tg2.addChild(light2);
        */
        PointLight light3 = new PointLight();
        light3.setEnable(true);
        light3.setColor(lightColor);
        light3.setPosition(0, 0, 0);
        light3.setAttenuation(1.0f, 0.0f, 0.0f);
        /*
        light3.setDirection(light1Direction);
        light3.setSpreadAngle((float)(Math.PI / 2));
        light3.setConcentration(0.0f);*/
        light3.setInfluencingBounds(bounds);
        tg2.addChild(light3);

/*
        AmbientLight ambientLightNode = new AmbientLight (lightColor);
        ambientLightNode.setInfluencingBounds (bounds);
        content.addChild (ambientLightNode);*/

        return content;
    }

    private Shape3D getVisibleVoxelsQuad(java.util.List<Point> visibleList, Mask mask, Image image, Point pointFrom, Point pointTo) {

        int numVerts = 24; // 6 faces * 1 quad * 4 vertices
        int numVoxel = visibleList.size();

        int indexCount = numVerts * numVoxel;
        int vertexCount = numVoxel * 8;    // 8 vertices/corners of the box/voxel
        int vertexFormat = TriangleArray.COORDINATES | TriangleArray.NORMALS | TriangleArray.COLOR_3 |
                TriangleArray.USE_COORD_INDEX_ONLY;

        // One geometry for all visible voxels
        IndexedQuadArray voxels = new IndexedQuadArray(vertexCount, vertexFormat, indexCount);

        int indexOffset = 0;
        int voxelOffset = 0;

        int[] voxelIndices = null;
        float[] voxelCoords = null;
        float[] voxelColors = null;
        float[] voxelNormals = null;

        for (Point iv : visibleList) {

            if (iv.x < pointFrom.x || iv.y < pointFrom.y || iv.sliceId < pointFrom.sliceId ||
                    iv.x > pointTo.x || iv.y > pointTo.y || iv.sliceId > pointTo.sliceId)
                continue;

            voxelIndices = getBoxQuadIndices(voxelOffset);

            voxelCoords = getBoxIndexedCoords(iv.x - mask.getWitdh() / 2, iv.y - mask.getHeight() / 2, iv.sliceId - mask.getSliceCount() / 2);
            voxelColors = getBoxIndexedColors(0.5f, 0.5f, 0.5f/*image.getPointValueFloat(iv), image.getPointValueFloat(iv), image.getPointValueFloat(iv)*/);
            voxelNormals = getBoxIndexedNormals();

            voxels.setCoordinateIndices(indexOffset, voxelIndices);

            voxels.setCoordinates(voxelOffset, voxelCoords);
            voxels.setNormals(voxelOffset, voxelNormals);
            voxels.setColors(voxelOffset, voxelColors);

            indexOffset += numVerts;
            voxelOffset += 8;
        }

        Appearance app = new Appearance();
        Material mat = new Material();
        mat.setAmbientColor(new Color3f(0.0f, 0.0f, 1.0f));
        mat.setDiffuseColor(new Color3f(0.7f, 0.7f, 0.7f));
        mat.setSpecularColor(new Color3f(0.7f, 0.7f, 0.7f));
        app.setMaterial(mat);

        return new Shape3D(voxels, app);
    }

    // Quad indices
    private int[] getBoxQuadIndices(int offset) {
        int[] indices = {
                // +X box right
                0, 6, 5, 1,
                // -X box left
                3, 2, 4, 7,
                // +Y box top
                0, 1, 2, 3,
                // -Y box bottom
                4, 5, 6, 7,
                // +Z box front
                0, 3, 7, 6,
                // -Z box back
                4, 2, 1, 5
        };

        for (int i = 0; i < 24; i++)
            indices[i] += offset;

        return indices;
    }

    // (x, y, z) = translation
    private float[] getBoxIndexedCoords(float x, float y, float z) {
        float[] coords = {
                // box top
                1.0f + x, 1.0f + y, 1.0f + z,    // 0
                1.0f + x, 1.0f + y, -1.0f + z,    // 1
                -1.0f + x, 1.0f + y, -1.0f + z,    // 2
                -1.0f + x, 1.0f + y, 1.0f + z,    // 3

                // box bottom
                -1.0f + x, -1.0f + y, -1.0f + z,    // 4
                1.0f + x, -1.0f + y, -1.0f + z,    // 5
                1.0f + x, -1.0f + y, 1.0f + z,    // 6
                -1.0f + x, -1.0f + y, 1.0f + z,    // 7
        };
        return coords;
    }

    // (r, g, b) = color
    private float[] getBoxIndexedColors(float r, float g, float b) {
        float[] colors = {
                r, g, b,    // 0
                r, g, b,    // 1
                r, g, b,    // 2
                r, g, b,    // 3

                r, g, b,    // 4
                r, g, b,    // 5
                r, g, b,    // 6
                r, g, b    // 7
        };
        return colors;
    }

    // (r, g, b) = color
    private float[] getBoxIndexedNormals() {
        float[] normals = {
                -1, -1, -1,    // 0
                1, 1, 1,     // 1
                -1, -1, -1,    // 2
                1, 1, 1,     // 3

                -1, -1, -1,    // 4
                1, 1, 1,     // 5
                -1, -1, -1,    // 6
                1, 1, 1,     // 7
        };
        return normals;
    }
}
