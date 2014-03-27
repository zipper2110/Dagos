package com.dagos.graphics.display;

import com.dagos.graphics.Mask;
import com.dagos.graphics.display.maskviewer.scene.SceneBuilder;
import com.dagos.graphics.display.maskviewer.scene.SceneBuilderGreyQuads;
import com.sun.j3d.utils.universe.SimpleUniverse;

import javax.media.j3d.Canvas3D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Created by Dmitry on 02.03.14.
 */
public class MaskViewer extends Canvas3D {

    private SimpleUniverse universe;
    private SceneBuilder sceneBuilder;

    private double lastZoom = 0.002;

    public MaskViewer() {
        super(SimpleUniverse.getPreferredConfiguration());
        setStereoEnable(true);

        sceneBuilder = new SceneBuilderGreyQuads();

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
    }

    public void displayMask(Mask mask, com.dagos.graphics.Image image, com.dagos.graphics.Point pointFrom, com.dagos.graphics.Point pointTo) {
        if (universe != null) {
            universe.cleanup();
        }
        sceneBuilder.setMask(mask);
        sceneBuilder.setImage(image);
        sceneBuilder.setPointFrom(pointFrom);
        sceneBuilder.setPointTo(pointTo);
        sceneBuilder.setScale(lastZoom);

        universe = new SimpleUniverse(this);
        universe.getViewingPlatform().setNominalViewingTransform();
        universe.addBranchGraph(sceneBuilder.getScene(lastZoom));
    }
}
