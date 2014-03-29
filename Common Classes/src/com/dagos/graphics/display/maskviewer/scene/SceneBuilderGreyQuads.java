package com.dagos.graphics.display.maskviewer.scene;

import com.dagos.graphics.Point;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

/**
 * Created by Dmitry on 26.03.14
 */
public class SceneBuilderGreyQuads extends SceneBuilder {

    @Override
    protected void buildScene() {
        this.transformGroupMain.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        this.transformGroupMain.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        Shape3D lungShape = getVisibleVoxelsShape();
        lungShape.setAppearance(getAppearance());
        lungShape.setPickable(true);
        this.transformGroupMain.addChild(lungShape);

        MouseRotate f1 = new MouseRotate();
        f1.setSchedulingBounds(new BoundingSphere());
        f1.setTransformGroup(this.transformGroupMain);
        this.scene.addChild(f1);


        Transform3D temp = new Transform3D();
        this.transformGroupMain.getTransform(temp);
        temp.setScale(scale);
        this.transformGroupMain.setTransform(temp);

        Background background = new Background(new Color3f(1f, 1f, 1f));
        BoundingSphere sphere = new BoundingSphere(new Point3d(0, 0, 0), 100000);
        background.setApplicationBounds(sphere);
        this.scene.addChild(background);
        setLight();
    }

    private void setLight() {
        BoundingSphere bounds = new BoundingSphere(new Point3d(0, 0, 0), 500);
        Color3f lightColor = new Color3f(1.0f, 1.0f, 1.0f);
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
        this.transformGroupMain.addChild(light3);
        /*
        Vector3f light1Direction = new Vector3f(0.0f, 1.0f, 0.0f);
        DirectionalLight light1  = new DirectionalLight (lightColor, light1Direction);
        light1.setInfluencingBounds (bounds);
        this.transformGroupMain.addChild (light1);*/
/*
        PointLight light2 = new PointLight();
        light2.setEnable( true );
        light2.setAttenuation(1.0f, 0.0f, 0.0f);
        light2.setPosition(0, 0, -200);
        light2.setColor(lightColor);
        light2.setInfluencingBounds( bounds );
        this.transformGroupMain.addChild(light2);
        */
/*
        AmbientLight ambientLightNode = new AmbientLight (lightColor);
        ambientLightNode.setInfluencingBounds (bounds);
        this.scene.addChild (ambientLightNode);*/
    }


    protected Shape3D getVisibleVoxelsShape() {
        if (this.mask == null) return new Shape3D();
        if (this.mask.getWitdh() == 0 || this.mask.getHeight() == 0 || this.mask.getSliceCount() == 0)
            return new Shape3D();

        int numVerts = 24; // 6 faces * 1 quad * 4 vertices
        int numVoxel = this.mask.getPointsCount();

        int indexCount = numVerts * numVoxel;
        int vertexCount = numVoxel * 8;    // 8 vertices/corners of the box/voxel
        int vertexFormat = TriangleArray.COORDINATES | TriangleArray.NORMALS | TriangleArray.COLOR_3 |
                TriangleArray.USE_COORD_INDEX_ONLY;

        // One geometry for all visible voxels
        IndexedQuadArray voxels = new IndexedQuadArray(vertexCount, vertexFormat, indexCount);

        int indexOffset = 0;
        int voxelOffset = 0;

        int[] voxelIndices;
        float[] voxelCoords;
        float[] voxelColors;
        float[] voxelNormals;

        for (int i = 0; i < this.mask.getWitdh(); i++) {
            for (int j = 0; j < this.mask.getHeight(); j++) {
                for (int k = 0; k < this.mask.getSliceCount(); k++) {
                    Point point = new Point(i, j, k);
                    if (this.mask.getPointValue(point)) {
                        if (point.x < pointFrom.x || point.y < pointFrom.y || point.sliceId < pointFrom.sliceId ||
                                point.x > pointTo.x || point.y > pointTo.y || point.sliceId > pointTo.sliceId)
                            continue;

                        voxelIndices = getBoxQuadIndices(voxelOffset);

                        voxelCoords = getBoxIndexedCoords((point.x * 2 - mask.getWitdh()), (mask.getSliceCount() - point.sliceId * 2), (point.y * 2 - mask.getHeight()));
                        voxelColors = getBoxIndexedColors(0.5f, 0.5f, 0.5f/*image.getPointValueFloat(point), image.getPointValueFloat(point), image.getPointValueFloat(point)*/);
                        voxelNormals = getBoxIndexedNormals();

                        voxels.setCoordinateIndices(indexOffset, voxelIndices);

                        voxels.setCoordinates(voxelOffset, voxelCoords);
                        voxels.setNormals(voxelOffset, voxelNormals);
                        voxels.setColors(voxelOffset, voxelColors);

                        indexOffset += numVerts;
                        voxelOffset += 8;
                    }
                }
            }
        }
        return new Shape3D(voxels);
    }

    private Appearance getAppearance() {
        Appearance app = new Appearance();
        Material mat = new Material();
        mat.setAmbientColor(new Color3f(0.0f, 0.0f, 1.0f));
        mat.setDiffuseColor(new Color3f(0.7f, 0.7f, 0.7f));
        mat.setSpecularColor(new Color3f(0.7f, 0.7f, 0.7f));
        app.setMaterial(mat);

        return app;
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
    protected float[] getBoxIndexedCoords(float x, float y, float z) {
        return new float[]{
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
    }

    // (r, g, b) = color
    protected float[] getBoxIndexedColors(float r, float g, float b) {
        return new float[]{
                r, g, b,    // 0
                r, g, b,    // 1
                r, g, b,    // 2
                r, g, b,    // 3

                r, g, b,    // 4
                r, g, b,    // 5
                r, g, b,    // 6
                r, g, b    // 7
        };
    }

    // (r, g, b) = color
    protected float[] getBoxIndexedNormals() {
        return new float[]{
                -1, -1, -1,    // 0
                1, 1, 1,     // 1
                -1, -1, -1,    // 2
                1, 1, 1,     // 3

                -1, -1, -1,    // 4
                1, 1, 1,     // 5
                -1, -1, -1,    // 6
                1, 1, 1,     // 7
        };
    }
}
