package com.dagos.graphics.display.maskviewer.scene;

import com.dagos.graphics.Image;
import com.dagos.graphics.Mask;
import com.dagos.graphics.Point;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;

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
    protected Color3f backgroundColor = new Color3f(1f, 1f, 1f);


    public SceneBuilder() {
        this.scene = new BranchGroup();
        this.transformGroupMain = new TransformGroup();
        this.transformGroupMain.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        this.transformGroupMain.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

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
        this.transformGroupMain.removeAllChildren();

        Shape3D shape = getShape();
        shape.setPickable(true);
        shape.setAppearance(getAppearance());
        this.transformGroupMain.addChild(shape);

        MouseRotate f1 = new MouseRotate();
        f1.setSchedulingBounds(new BoundingSphere());
        f1.setTransformGroup(this.transformGroupMain);
        this.scene.addChild(f1);

        List<Light> lights = getLights();
        for (Light light : lights) {
            this.scene.addChild(light);
        }

        this.scene.addChild(getBackground());

        setScale(scale);

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

    protected abstract Shape3D getShape();

    protected Background getBackground() {
        Background background = new Background(backgroundColor);
        background.setApplicationBounds(new BoundingSphere(new Point3d(0, 0, 0), 1000));

        return background;
    }

    protected List<Light> getLights() {
        List<Light> lights = new ArrayList<Light>();

        BoundingSphere bounds = new BoundingSphere(new Point3d(0, 0, 0), 500);
        Color3f lightColor = new Color3f(1.0f, 1.0f, 1.0f);
        PointLight light3 = new PointLight();
        light3.setEnable(true);
        light3.setColor(lightColor);
        light3.setPosition(0, 0, 0);
        light3.setAttenuation(1.0f, 0.0f, 0.0f);
        /*
        light3.setDirection(light1Direction);
        light3.setSpreadAngle((float)(Math.PI / 2));
        light3.setConcentration(0.0f);*/
        light3.setInfluencingBounds(bounds);
        lights.add(light3);
        /*
        Vector3f light1Direction = new Vector3f(0.0f, 1.0f, 0.0f);
        DirectionalLight light1  = new DirectionalLight (lightColor, light1Direction);
        light1.setInfluencingBounds (bounds);
        this.transformGroupMain.addChild (light1);*/
/*
        PointLight light2 = new PointLight();
        light2.setEnable( true );
        light2.setAttenuation(1.0f, 0.0f, 0.0f);
        light2.setPosition(0, 0, -200);
        light2.setColor(lightColor);
        light2.setInfluencingBounds( bounds );
        this.transformGroupMain.addChild(light2);
        */
/*
        AmbientLight ambientLightNode = new AmbientLight (lightColor);
        ambientLightNode.setInfluencingBounds (bounds);
        this.scene.addChild (ambientLightNode);*/
        return lights;
    }

    protected Appearance getAppearance() {
        Appearance app = new Appearance();
        Material mat = new Material();
        mat.setAmbientColor(new Color3f(0.0f, 0.0f, 1.0f));
        mat.setDiffuseColor(new Color3f(0.7f, 0.7f, 0.7f));
        mat.setSpecularColor(new Color3f(0.7f, 0.7f, 0.7f));
        app.setMaterial(mat);

        return app;
    }
}
