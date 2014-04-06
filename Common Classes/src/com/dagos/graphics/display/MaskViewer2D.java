package com.dagos.graphics.display;

import com.dagos.graphics.Image;
import com.dagos.graphics.Mask;
import com.dagos.graphics.Point;

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

        int canvasHeight = getHeight();
        int canvasWidth = getWidth();
        int imageHeight = image.getHeight();
        int imageWidth = image.getWitdh();

        float heightRatio = (float) imageHeight / canvasHeight;
        float widthRatio = (float) imageWidth / canvasWidth;

        float ratio = Math.max(heightRatio, widthRatio);

        float sliceImageWidth = image.getWitdh() / ratio;
        float sliceImageHeight = image.getHeight() / ratio;

        BufferedImage sliceImage = new BufferedImage(
                new Double(Math.ceil(image.getWitdh() / ratio)).intValue(),
                new Double(Math.ceil(image.getHeight() / ratio)).intValue(),
                BufferedImage.TYPE_INT_RGB);


        for (int i = 0; i < sliceImageWidth; i++) {
            for (int j = 0; j < sliceImageHeight; j++) {
                Point currentPoint = new Point((int) Math.floor(i * ratio), (int) Math.floor(j * ratio), sliceId);
                int imagePixelColor = image.getPointValueInt(currentPoint);
                int maskColor = 0;
                if (mask.getPointValue(currentPoint)) {
                    maskColor = 150;
                }
                int red = imagePixelColor + maskColor;
                if (red > 255) red = 255;
                Color pixelColor = new Color(red, imagePixelColor, imagePixelColor);
                sliceImage.setRGB(i, j, pixelColor.getRGB());
            }
        }

        this.getGraphics().drawImage(sliceImage, 0, 0, null);
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
