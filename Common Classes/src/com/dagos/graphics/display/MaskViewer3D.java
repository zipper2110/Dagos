package com.dagos.graphics.display;

import com.dagos.graphics.Image;
import com.dagos.graphics.Mask;
import com.dagos.graphics.Point;
import com.dagos.graphics.display.maskviewer.scene.SceneBuilder;
import com.dagos.graphics.display.maskviewer.scene.SceneBuilderGreyQuads;
import com.sun.j3d.utils.universe.SimpleUniverse;

import javax.media.j3d.Canvas3D;
import javax.vecmath.Vector3f;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Created by Dmitry on 02.03.14.
 */
public class MaskViewer3D extends Canvas3D {

    private SimpleUniverse universe;
    private SceneBuilder sceneBuilder;

    private double lastZoom = 0.004;

    public MaskViewer3D() {
        super(SimpleUniverse.getPreferredConfiguration());
        setStereoEnable(true);

        sceneBuilder = new SceneBuilderGreyQuads();
//        sceneBuilder = new SceneBuilderGreyTriangles();
//        sceneBuilder = new SceneBuilderPointTriangles();

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double rotation = 0;
                if (e.getWheelRotation() > 0) {
                    rotation = -0.3f;
                } else {
                    rotation = 0.3f;
                }
                sceneBuilder.setScale(lastZoom + lastZoom * rotation);
                lastZoom = sceneBuilder.getScale();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char keyChar = e.getKeyChar();
                Vector3f translation = sceneBuilder.getTranslation();
                if (keyChar == 'a') {
                    translation.setY(translation.getY() - 0.1f);
                } else if (keyChar == 's') {
                    translation.setY(translation.getY() + 0.1f);
                } else if (keyChar == 'q') {
                    translation.setZ(translation.getZ() - 0.1f);
                } else if (keyChar == 'w') {
                    translation.setZ(translation.getZ() + 0.1f);
                } else if (keyChar == 'z') {
                    translation.setX(translation.getX() - 0.1f);
                } else if (keyChar == 'x') {
                    translation.setX(translation.getX() + 0.1f);
                }
                sceneBuilder.setTranslation(translation);
            }
        });
    }

    public void displayMask(Mask mask, Mask pathologyMask, Image image, Point pointFrom, Point pointTo) {
        if (universe != null) {
            universe.cleanup();
        }
        sceneBuilder.setMask(mask);
        sceneBuilder.setPathologyMask(pathologyMask);
        sceneBuilder.setImage(image);
        sceneBuilder.setPointFrom(pointFrom);
        sceneBuilder.setPointTo(pointTo);
        sceneBuilder.setScale(lastZoom);

        universe = new SimpleUniverse(this);
        universe.getViewingPlatform().setNominalViewingTransform();
        universe.addBranchGraph(sceneBuilder.getScene(lastZoom));
    }

//    public void displayMask(Mask mask, Image image, Point pointFrom, Point pointTo) {
//        if (universe != null) {
//            universe.cleanup();
//        }
//        sceneBuilder.setMask(mask);
//        sceneBuilder.setImage(image);
//        sceneBuilder.setPointFrom(pointFrom);
//        sceneBuilder.setPointTo(pointTo);
//        sceneBuilder.setScale(lastZoom);
//
//        universe = new SimpleUniverse(this);
//        universe.getViewingPlatform().setNominalViewingTransform();
//
//        universe.addBranchGraph(sceneBuilder.getScene(lastZoom));
//    }
}
