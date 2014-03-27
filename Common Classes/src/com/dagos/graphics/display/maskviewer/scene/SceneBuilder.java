package com.dagos.graphics.display.maskviewer.scene;

import com.dagos.graphics.Image;
import com.dagos.graphics.Mask;
import com.dagos.graphics.Point;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

/**
 * Created by Dmitry on 26.03.14
 */
public abstract class SceneBuilder {

    protected TransformGroup transformGroupMain;
    protected BranchGroup scene;
    protected Mask mask;
    protected Image image;
    protected Point pointFrom;
    protected Point pointTo;
    protected Double scale = 1.0;

    public SceneBuilder() {
        scene = new BranchGroup();
        transformGroupMain = new TransformGroup();
        this.scene.addChild(this.transformGroupMain);

        setPointFrom(new Point(0, 0, 0));
        setPointTo(new Point(0, 0, 0));
    }

    public SceneBuilder(Mask mask, Image image, Point pointFrom, Point pointTo) {
        this();
        setMask(mask);
        setImage(image);
        setPointFrom(pointFrom);
        setPointTo(pointTo);
    }

    public BranchGroup getScene(Double scale) {
        setScale(scale);
        this.transformGroupMain.removeAllChildren();
        buildScene();
        return scene;
    }

    public BranchGroup getScene() {
        return getScene(getScale());
    }

    public Mask getMask() {
        return this.mask;
    }

    public void setMask(Mask mask) {
        this.mask = mask;
        if (this.mask != null) {
            if (this.pointFrom == null) {
                this.pointFrom = new Point(0, 0, 0);
            }
            if (this.pointTo == null) {
                this.pointTo = new Point(mask.getWitdh() - 1, mask.getHeight() - 1, mask.getSliceCount() - 1);
            }
        }
    }

    public Image getImage() {
        return this.image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Point getPointFrom() {
        return this.pointFrom;
    }

    public void setPointFrom(Point pointFrom) {
        if (pointFrom != null) {
            this.pointFrom = pointFrom;
        } else {
            this.pointFrom = new Point(0, 0, 0);
        }
    }

    public Point getPointTo() {
        return this.pointTo;
    }

    public void setPointTo(Point pointTo) {
        if (pointFrom != null) {
            this.pointTo = pointTo;
        } else {
            this.pointTo = new Point(0, 0, 0);
        }
    }

    public void setScale(Double scale) {
        Transform3D temp = new Transform3D();
        transformGroupMain.getTransform(temp);
        temp.setScale(scale);
        transformGroupMain.setTransform(temp);

        this.scale = temp.getScale();
    }

    public Double getScale() {
        return this.scale;
    }

    protected abstract void buildScene();
}
