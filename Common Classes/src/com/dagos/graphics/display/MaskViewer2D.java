package com.dagos.graphics.display;

import com.dagos.graphics.Image;
import com.dagos.graphics.Mask;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

/**
 * Created by Dmitry on 05.04.2014
 */
public class MaskViewer2D extends Canvas implements MouseWheelListener {

    private Image image;
    private Mask mask;

    private int sliceId;

    public MaskViewer2D() {
        super();
        addMouseWheelListener(this);
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setMask(Mask mask) {
        this.mask = mask;
    }

    public void display() {
        displaySlice(0);
    }

    public void displaySlice(int sliceId) {

        if (mask == null || image == null) return;
        if (image.getSliceCount() < 1 || mask.getSliceCount() < 1 || image.getSliceCount() != mask.getSliceCount())
            return;
        if (sliceId < 0) sliceId = 0;
        if (sliceId > image.getSliceCount() - 1) sliceId = image.getSliceCount() - 1;
        this.sliceId = sliceId;

        int imageHeight = image.getHeight();
        int imageWidth = image.getWitdh();

        float heightRatio = (float) imageHeight / getHeight();
        float widthRatio = (float) imageWidth / getWidth();

        float ratio = Math.max(heightRatio, widthRatio);

        BufferedImage sliceImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        int imagePixelColor, maskColor, red;
        Color pixelColor;

        for (int i = 0; i < imageWidth; i++) {
            for (int j = 0; j < imageHeight; j++) {
                imagePixelColor = image.getPointValueInt(i, j, sliceId);
                maskColor = 0;
                if (mask.getPointValue(i, j, sliceId)) {
                    maskColor = 150;
                }
                red = imagePixelColor + maskColor;
                if (red > 255) red = 255;
                pixelColor = new Color(red, imagePixelColor, imagePixelColor);
                sliceImage.setRGB(i, j, pixelColor.getRGB());
            }
        }

        this.getGraphics().drawImage(
                sliceImage.getScaledInstance(
                        (int) (imageWidth / ratio),
                        (int) (imageHeight / ratio),
                        BufferedImage.SCALE_FAST),
                0, 0, null
        );
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0) {
            sliceId += 1;
        } else {
            sliceId -= 1;
        }
        displaySlice(sliceId);
    }
}
