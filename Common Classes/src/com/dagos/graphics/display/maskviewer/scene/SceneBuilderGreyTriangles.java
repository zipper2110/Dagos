package com.dagos.graphics.display.maskviewer.scene;

import com.dagos.graphics.Point;

import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;

/**
 * Created by Dmitry on 28.03.2014
 */
public class SceneBuilderGreyTriangles extends SceneBuilderGreyQuads {

    protected Shape3D getVisibleVoxelsShape() {

        int numVerts = 36; // 6 faces * 2 triangles * 3 vertices
        int numVoxel = this.mask.getPointsCount();

        int vertexCount = numVerts * numVoxel;
        int vertexFormat = TriangleArray.COORDINATES | TriangleArray.NORMALS | TriangleArray.COLOR_3;

        // One geometry for all visible voxels
        TriangleArray voxels = new TriangleArray(vertexCount, vertexFormat);

        int voxelOffset = 0;

        float[] voxelCoords = null;
        float[] voxelColors = null;
        float[] voxelNormals;

        for (int i = 0; i < this.mask.getWitdh(); i++) {
            for (int j = 0; j < this.mask.getHeight(); j++) {
                for (int k = 0; k < this.mask.getSliceCount(); k++) {
                    Point point = new Point(i, j, k);
                    if (this.mask.getPointValue(point)) {

                        voxelCoords = getBoxIndexedCoords(point.x - mask.getWitdh() / 2, mask.getSliceCount() / 2 - point.sliceId, point.y - mask.getHeight() / 2);
                        voxelColors = getBoxIndexedColors(0.5f, 0.5f, 0.5f/*image.getPointValueFloat(point), image.getPointValueFloat(point), image.getPointValueFloat(point)*/);
                        voxelNormals = getBoxIndexedNormals();

                        voxels.setCoordinates(voxelOffset, voxelCoords);
                        voxels.setNormals(voxelOffset, voxelNormals);
                        voxels.setColors(voxelOffset, voxelColors);

                        voxelOffset += numVerts;
                    }
                }
            }
        }

        // One Shape3D for all visible voxels, null Apearance
        return new Shape3D(voxels, null);
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
}
