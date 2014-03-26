package com.dagos.graphics.display;

import com.dagos.graphics.Mask;
import com.dagos.graphics.display.maskviewer.scene.SceneBuilder;
import com.dagos.graphics.display.maskviewer.scene.SceneBuilderGreyQuads;
import com.sun.j3d.utils.universe.SimpleUniverse;

import javax.media.j3d.Canvas3D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

/**
 * Created by Dmitry on 02.03.14.
 */
public class MaskViewer extends Canvas3D {

    private SimpleUniverse universe;
    private SceneBuilder sceneBuilder;

    private double lastZoom = 0.5f;

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
                sceneBuilder.setScale(sceneBuilder.getScale() + rotation);
            }
        });
    }

    public void displayMask(Mask mask, com.dagos.graphics.Image image, com.dagos.graphics.Point pointFrom, com.dagos.graphics.Point pointTo) {
        if (universe != null) {
            universe.cleanup();
        }
        universe = new SimpleUniverse(this);
        universe.getViewingPlatform().setNominalViewingTransform();
        universe.addBranchGraph(sceneBuilder.getScene(lastZoom));
    }

    private java.util.List<com.dagos.graphics.Point> getVisiblePoints(Mask mask) {
        java.util.List<com.dagos.graphics.Point> visiblePoints = new ArrayList<com.dagos.graphics.Point>();

        for (int k = 0; k < mask.getSliceCount(); k++) {
            for (int i = 0; i < mask.getWitdh(); i++) {
                for (int j = 0; j < mask.getHeight(); j++) {
                    if (mask.getPointValue(i, j, k)) {
                        visiblePoints.add(new com.dagos.graphics.Point(i, j, k));
                    }
                }
            }
        }

        return visiblePoints;
    }


}
