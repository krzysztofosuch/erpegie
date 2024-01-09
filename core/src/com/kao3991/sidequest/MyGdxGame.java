package com.kao3991.sidequest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Octree;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import java.util.ArrayList;
import java.util.List;

public class MyGdxGame extends ApplicationAdapter {
	private PerspectiveCamera cam;
	private Environment environment;

	private CameraInputController camController;
	private MyInputProcessor inputProcessor;
	private Model world;
	private List<ModelInstance> instances;

	private List<GameObject> objects;
	private GameObject character;
	private GameObject cursor;
	private ModelBatch modelBatch;
	public void create () {
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0,0,0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		//camController = new CameraInputController(cam);

		ModelLoader loader = new ObjLoader();
		character = new GameObject("assets/dwarf.obj");
		cursor = new GameObject("assets/cursor.obj");
		world = loader.loadModel(Gdx.files.internal("assets/elven_village.obj"));

		final ModelInstance worldInstance = new ModelInstance(world);
		instances = new ArrayList<>();
		instances.add(worldInstance);
		modelBatch = new ModelBatch();
		objects = new ArrayList<>();
		objects.add(character);
		objects.add(cursor);
		inputProcessor = new MyInputProcessor(new ClickHandler() {
			@Override
			public void onClick(int x, int y, int btn) {
				System.out.println(String.format("click on %d:%d", x, y));
				Ray ray = cam.getPickRay(y, x);
				Vector3 v3 = intersectModel(worldInstance, ray);
				if (v3 != null) {
					System.out.println(String.format("O PRZENOSZE NA %f %f %f", v3.x, v3.y, v3.z));
				} else {
					System.out.println("collision miss");
				}
			}
		});
/*		inputProcessor = new MyInputProcessor(new ClickHandler() {
			@Override
			public void onClick(int x, int y, int btn) {
				Ray ray = cam.getPickRay(y, x);
				ArrayList<Vector3> vertices = new ArrayList<>();
				worldInstance.calculateTransforms();
				Renderable rend = new Renderable();
				Mesh mesh = worldInstance.getRenderable(rend).meshPart.mesh;

				int vertexSize = mesh.getVertexSize() / 4;
				float[] verts = new float[mesh.getNumVertices() * vertexSize];
				short[] inds = new short[mesh.getNumIndices()];
				mesh.getVertices(verts);
				mesh.getIndices(inds);

				for (int i = 0; i < inds.length; i++) {
					int i1 = inds[i] * vertexSize;
					Vector3 v = new Vector3(verts[i1], verts[i1 + 1], verts[i1 + 2]);
					v.set(v.prj(rend.worldTransform));
					vertices.add(v);
				}
				Vector3 out = Vector3.Zero;
				for (int i = 0; i < vertices.size() - 3; i+=3){
					if (Intersector.intersectRayTriangle(ray, vertices.get(i), vertices.get(i + 1), vertices.get(i + 2), out)) {
						System.out.println(String.format("O PRZENOSZE NA %f %f %f", out.x, out.y, out.z));
						cursor.modelInstance.transform.setToTranslation(out);
						cam.lookAt(out);
						break;
					}
				}

			}
		});
		*/
		Gdx.input.setInputProcessor(inputProcessor);
	}

	@Override
	public void render () {
		//camController.update();
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(instances, environment);
		for (GameObject o : objects) {
			o.render(modelBatch, environment);
		}
		modelBatch.end();

	}
	
	@Override
	public void dispose () {

	}
	public static Vector3 intersectModel(ModelInstance modelInstance, Ray ray) {
        Vector3 intersection = new Vector3();

		BoundingBox b = new BoundingBox();
        // Check if the ray intersects with the bounding box of the model
        if (!Intersector.intersectRayBoundsFast(ray, modelInstance.calculateBoundingBox(b))) {
            return null; // No intersection with the bounding box, return null
        }

        // Get the model's mesh
        Model model = modelInstance.model;
        if (model == null || model.meshes == null || model.meshes.size == 0) {
            return null; // No mesh available, return null
        }
		for (int meshIndex = 0; meshIndex < model.meshes.size; meshIndex++) {
			System.out.println(String.format("mesh %d", meshIndex));
			// Iterate through the mesh triangles and find the intersection point
			float[] vs = new float[model.meshes.get(meshIndex).getNumVertices()];
			short[] indices = new short[model.meshes.get(meshIndex).getNumIndices()];
			float[] vertices = model.meshes.get(meshIndex).getVertices(vs);
			model.meshes.get(meshIndex).getIndices(indices);
			for (int i = 0; i < indices.length; i += 3) {
				System.out.println(String.format("indices %d to %d", i, i+2));
				int index1 = indices[i] * model.meshes.get(meshIndex).getVertexSize() / 4;
				int index2 = indices[i + 1] * model.meshes.get(meshIndex).getVertexSize() / 4;
				int index3 = indices[i + 2] * model.meshes.get(meshIndex).getVertexSize() / 4;

				Vector3 v1 = new Vector3(vertices[index1], vertices[index1], vertices[index1]);
				Vector3 v2 = new Vector3(vertices[index2], vertices[index2], vertices[index2]);
				Vector3 v3 = new Vector3(vertices[index3], vertices[index3], vertices[index3]);

				if (Intersector.intersectRayTriangle(ray, v1, v2, v3, intersection)) {
					return intersection; // Return the intersection point
				}
			}
		}

        return null; // No intersection found
    }

}
