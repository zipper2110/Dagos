package com.dagos.graphics;

import java.io.*;
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

    public void save(File fileToSaveTo) throws IOException {
        if (!fileToSaveTo.createNewFile())
            throw new NoSuchFileException(fileToSaveTo.getAbsolutePath(), "", "can't create specified file");

        FileOutputStream fos = new FileOutputStream(fileToSaveTo);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(this);

        oos.close();
    }

    public static Mask Load(File fileToLoadFrom) throws IOException {
        if (!fileToLoadFrom.exists()) throw new FileNotFoundException("File doesn't exist");

        Mask mask = null;
        FileInputStream fis = new FileInputStream(fileToLoadFrom);
        ObjectInputStream ois = new ObjectInputStream(fis);
        try {
            mask = (Mask) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            ois.close();
        }
        return mask;
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
