package com.dagos.graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by Dmitry on 02.03.14.
 */
public class Image {

    private byte[][][] imageData;

    public static String FILE_EXTENTION = ".dgsimg";

    public Image(byte[][][] imageData) {
        this.imageData = imageData;
    }

    public Image(File[] imageFiles) throws IOException {
        byte[][][] imagesData = new byte[0][0][0];
        if (imageFiles.length > 0) {
            BufferedImage _image = ImageIO.read(imageFiles[0]);
            imagesData = new byte[_image.getWidth()][_image.getHeight()][imageFiles.length];

            for (int k = 0; k < imageFiles.length; k++) {
                File imageFile = imageFiles[k];
                BufferedImage image = ImageIO.read(imageFile);
                for (int i = 0; i < image.getWidth(); i++) {
                    for (int j = 0; j < image.getHeight(); j++) {
                        int rgb = image.getRGB(i, j);
                        Color color = new Color(rgb);
                        imagesData[i][j][k] = (byte) (color.getBlue() - 128);
                    }
                }
            }
        }
        this.imageData = imagesData;
    }

    public Image(File dagosImageFile) throws IOException {
        this.imageData = load(dagosImageFile);
    }

    public void save(File fileToSaveTo) throws IOException {
        fileToSaveTo.createNewFile();
        if (imageData.length == 0) return;

        int imageX = imageData.length;
        int imageY = imageData[0].length;
        int imagesCount = imageData[0][0].length;

        FileOutputStream fos = new FileOutputStream(fileToSaveTo);
        BufferedOutputStream bos = new BufferedOutputStream(fos, imageX * imageY);

        byte[] imageXBytes = ByteBuffer.allocate(4).putInt(imageX).array();
        byte[] imageYBytes = ByteBuffer.allocate(4).putInt(imageY).array();
        byte[] imagesCountBytes = ByteBuffer.allocate(4).putInt(imagesCount).array();

        fos.write(imageXBytes);
        fos.write(imageYBytes);
        fos.write(imagesCountBytes);

        for (int k = 0; k < imagesCount; k++) {
            for (int i = 0; i < imageX; i++) {
                for (int j = 0; j < imageY; j++) {
                    bos.write(imageData[i][j][k]);
                }
            }
        }
        bos.flush();
        bos.close();
    }

    private byte[][][] load(File fileToLoadFrom) throws IOException {
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

        BufferedInputStream bis = new BufferedInputStream(fis, imageX * imageY);

        byte[][][] imageData = new byte[imageX][imageY][imagesCount];

        for (int k = 0; k < imagesCount; k++) {
            for (int i = 0; i < imageX; i++) {
                for (int j = 0; j < imageY; j++) {
                    imageData[i][j][k] = (byte) bis.read();
                }
            }
        }
        bis.close();

        return imageData;
    }

    public byte[][][] getImageData() {
        return imageData;
    }

    public byte getPointValue(Point point) {
        return imageData[point.x][point.y][point.sliceId];
    }

    public int getPointValueInt(Point point) {
        return imageData[point.x][point.y][point.sliceId] + 128;
    }

    public Color getPointColor(Point point) {
        int greyValue = imageData[point.x][point.y][point.sliceId] + 128;
        return new Color(greyValue, greyValue, greyValue);
    }

    public float getPointValueFloat(Point point) {
        return ((float) ((int) imageData[point.x][point.y][point.sliceId] + 128)) / 255;
    }

    public int getSliceCount() {
        return imageData[0][0].length;
    }

    public int getWitdh() {
        return imageData.length;
    }

    public int getHeight() {
        return imageData[0].length;
    }
}
