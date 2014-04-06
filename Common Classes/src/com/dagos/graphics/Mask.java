package com.dagos.graphics;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.NoSuchFileException;

/**
 * Created by Dmitry on 04.03.14
 */
public class Mask implements Serializable {
    public static String FILE_EXTENTION = ".dgsmsk";

    private boolean[][][] maskData;

    public Mask(boolean[][][] maskData) {
        this.maskData = maskData;
    }

    public Mask(File dagosMaskFile) throws IOException {
        this.maskData = load(dagosMaskFile);
    }

    public void save(File fileToSaveTo) throws IOException {
        if (!fileToSaveTo.createNewFile())
            throw new NoSuchFileException(fileToSaveTo.getAbsolutePath(), "", "can't create specified file");
        if (maskData.length == 0) return;

        int imageX = maskData.length;
        int imageY = maskData[0].length;
        int imagesCount = maskData[0][0].length;

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileToSaveTo), imageX * imageY * imagesCount);
        DataOutputStream dos = new DataOutputStream(bos);

        byte[] imageXBytes = ByteBuffer.allocate(4).putInt(imageX).array();
        byte[] imageYBytes = ByteBuffer.allocate(4).putInt(imageY).array();
        byte[] imagesCountBytes = ByteBuffer.allocate(4).putInt(imagesCount).array();

        dos.write(imageXBytes);
        dos.write(imageYBytes);
        dos.write(imagesCountBytes);


        for (int i = 0; i < imageX; i++) {
            for (int j = 0; j < imageY; j++) {
                for (int k = 0; k < imagesCount; k++) {
                    dos.writeBoolean(maskData[i][j][k]);
                }
            }
        }
        dos.close();
    }

    private boolean[][][] load(File fileToLoadFrom) throws IOException {
        FileInputStream fis = new FileInputStream(fileToLoadFrom);

        byte[] byteIntBuffer = new byte[4];
        ByteBuffer wrapper;

        fis.read(byteIntBuffer, 0, 4);
        wrapper = ByteBuffer.wrap(byteIntBuffer);
        int imageX = wrapper.getInt();

        fis.read(byteIntBuffer, 0, 4);
        wrapper = ByteBuffer.wrap(byteIntBuffer);
        int imageY = wrapper.getInt();

        fis.read(byteIntBuffer, 0, 4);
        wrapper = ByteBuffer.wrap(byteIntBuffer);
        int imagesCount = wrapper.getInt();

        DataInputStream dis = new DataInputStream(new BufferedInputStream(fis, imageX * imageY));

        boolean[][][] maskData = new boolean[imageX][imageY][imagesCount];

        for (int k = 0; k < imagesCount; k++) {
            for (int i = 0; i < imageX; i++) {
                for (int j = 0; j < imageY; j++) {
                    maskData[i][j][k] = dis.readBoolean();
                }
            }
        }
        dis.close();

        return maskData;
    }

    public int getSliceCount() {
        return maskData[0][0].length;
    }

    public int getWitdh() {
        return maskData.length;
    }

    public int getHeight() {
        return maskData[0].length;
    }

    public boolean getPointValue(int x, int y, int sliceId) {
        return maskData[x][y][sliceId];
    }

    public boolean getPointValue(Point point) {
        return maskData[point.x][point.y][point.sliceId];
    }

    public void setPointValue(int x, int y, int sliceId, boolean value) {
        maskData[x][y][sliceId] = value;
    }

    public void setPointValue(Point point, boolean value) {
        setPointValue(point.x, point.y, point.sliceId, value);
    }

    public int getPointsCount() {
        int pointsCount = 0;

        for (int k = 0; k < this.getSliceCount(); k++) {
            for (int i = 0; i < this.getWitdh(); i++) {
                for (int j = 0; j < this.getHeight(); j++) {
                    if (maskData[i][j][k]) pointsCount++;
                }
            }
        }
        return pointsCount;
    }

    public boolean hasPoint(Point point) {
        return point.x > -1 && point.x < this.getWitdh() &&
                point.y > -1 && point.y < this.getHeight() &&
                point.sliceId > -1 && point.sliceId < this.getSliceCount() &&
                this.getPointValue(point);
    }
}
