package com.kao3991.sidequest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class GameObject {
    public ModelInstance modelInstance;
    public BoundingBox boundingBox;
    public GameObject(String modelPath) {
        ModelLoader loader = new ObjLoader();
        Model m = loader.loadModel(Gdx.files.internal(modelPath));
        modelInstance = new ModelInstance(m);
        boundingBox = new BoundingBox();
        m.calculateBoundingBox(boundingBox);
    }
    public void render(ModelBatch target, Environment env) {
        target.render(modelInstance, env);
    }

    public void move(Vector3 movement) {

    }

    public Vector3 getDownPoint() {
        Vector3 center = Vector3.Zero;
        return boundingBox.getCenter(center);
    }
}
