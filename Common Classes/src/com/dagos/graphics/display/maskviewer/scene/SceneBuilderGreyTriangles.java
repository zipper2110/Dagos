package com.dagos.graphics.display.maskviewer.scene;

import com.dagos.graphics.Point;

import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;

/**
 * Created by Dmitry on 28.03.2014
 */
public class SceneBuilderGreyTriangles extends SceneBuilderGreyQuads {
    TriangleArray voxels;

    protected Shape3D getShape() {

        int numVerts = 36; // 6 faces * 2 triangles * 3 vertices
        int numVoxel = this.mask.getPointsCount();

        int vertexCount = numVerts * numVoxel;
        int vertexFormat = TriangleArray.COORDINATES | TriangleArray.NORMALS | TriangleArray.COLOR_3;

        // One geometry for all visible voxels
        voxels = new TriangleArray(vertexCount, vertexFormat);

        int voxelOffset = 0;

        float[] voxelCoords;
        float[] voxelColors;
        float[] voxelNormals;

        for (int i = 0; i < this.mask.getWitdh(); i++) {
            for (int j = 0; j < this.mask.getHeight(); j++) {
                for (int k = 0; k < this.mask.getSliceCount(); k++) {
                    Point point = new Point(i, j, k);
                    if (this.mask.getPointValue(point)) {

                        voxelCoords = getBoxTriaCoords(point.x * 2 - mask.getWitdh(), mask.getSliceCount() - point.sliceId * 2, point.y * 2 - mask.getHeight());
                        voxelColors = getBoxTriaColors(0.5f, 0.5f, 0.5f/*image.getPointValueFloat(point), image.getPointValueFloat(point), image.getPointValueFloat(point)*/);
                        voxelNormals = getBoxTriaNormals();

                        voxels.setCoordinates(voxelOffset, voxelCoords);
                        voxels.setNormals(voxelOffset, voxelNormals);
                        voxels.setColors(voxelOffset, voxelColors);

                        voxelOffset += numVerts;
                    }
                }
            }
        }

        // One Shape3D for all visible voxels, null Apearance
        return new Shape3D(voxels);
    }

    // (x, y, z) = translation
    private float[] getBoxTriaCoords(float x, float y, float z) {
        float[] coords = {
                // +X box right
                1.0f + x, 1.0f + y, 1.0f + z, 1.0f + x, -1.0f + y, 1.0f + z, 1.0f + x, -1.0f + y, -1.0f + z,
                1.0f + x, 1.0f + y, 1.0f + z, 1.0f + x, -1.0f + y, -1.0f + z, 1.0f + x, 1.0f + y, -1.0f + z,
                // -X box left
                -1.0f + x, 1.0f + y, -1.0f + z, -1.0f + x, -1.0f + y, -1.0f + z, -1.0f + x, -1.0f + y, 1.0f + z,
                -1.0f + x, 1.0f + y, -1.0f + z, -1.0f + x, -1.0f + y, 1.0f + z, -1.0f + x, 1.0f + y, 1.0f + z,
                // +Y box top
                -1.0f + x, 1.0f + y, -1.0f + z, -1.0f + x, 1.0f + y, 1.0f + z, 1.0f + x, 1.0f + y, 1.0f + z,
                -1.0f + x, 1.0f + y, -1.0f + z, 1.0f + x, 1.0f + y, 1.0f + z, 1.0f + x, 1.0f + y, -1.0f + z,
                // -Y box bottom
                -1.0f + x, -1.0f + y, 1.0f + z, -1.0f + x, -1.0f + y, -1.0f + z, 1.0f + x, -1.0f + y, 1.0f + z,
                1.0f + x, -1.0f + y, 1.0f + z, -1.0f + x, -1.0f + y, -1.0f + z, 1.0f + x, -1.0f + y, -1.0f + z,
                // +Z box front
                -1.0f + x, 1.0f + y, 1.0f + z, -1.0f + x, -1.0f + y, 1.0f + z, 1.0f + x, -1.0f + y, 1.0f + z,
                -1.0f + x, 1.0f + y, 1.0f + z, 1.0f + x, -1.0f + y, 1.0f + z, 1.0f + x, 1.0f + y, 1.0f + z,
                // -Z box back
                1.0f + x, 1.0f + y, -1.0f + z, 1.0f + x, -1.0f + y, -1.0f + z, -1.0f + x, -1.0f + y, -1.0f + z,
                1.0f + x, 1.0f + y, -1.0f + z, -1.0f + x, -1.0f + y, -1.0f + z, -1.0f + x, 1.0f + y, -1.0f + z
        };
        return coords;
    }

    // (r, g, b) = color
    private float[] getBoxTriaColors(float r, float g, float b) {
        float[] colors = {
                r, g, b, r, g, b, r, g, b,
                r, g, b, r, g, b, r, g, b,

                r, g, b, r, g, b, r, g, b,
                r, g, b, r, g, b, r, g, b,

                r, g, b, r, g, b, r, g, b,
                r, g, b, r, g, b, r, g, b,

                r, g, b, r, g, b, r, g, b,
                r, g, b, r, g, b, r, g, b,

                r, g, b, r, g, b, r, g, b,
                r, g, b, r, g, b, r, g, b,

                r, g, b, r, g, b, r, g, b,
                r, g, b, r, g, b, r, g, b
        };
        return colors;
    }

    // (r, g, b) = color
    protected float[] getBoxTriaNormals() {
        return new float[]{
                1, 1, 1,     // 0
                1, 1, 1,     // 1
                1, 1, 1,     // 2
                1, 1, 1,     // 3
                1, 1, 1,    // 4
                1, 1, 1,   // 5
                1, 1, 1,     // 6
                1, 1, 1,     // 7

                1, 1, 1,   // 8
                1, 1, 1,    // 9
                1, 1, 1,     // 10
                1, 1, 1,     // 11
        };
    }
}
