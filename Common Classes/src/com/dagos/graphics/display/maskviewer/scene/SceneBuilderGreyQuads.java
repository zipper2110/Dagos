package com.dagos.graphics.display.maskviewer.scene;

import com.dagos.graphics.Point;

import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;

/**
 * Created by Dmitry on 26.03.14
 */
public class SceneBuilderGreyQuads extends SceneBuilder {

    @Override
    protected Shape3D getShape() {
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
