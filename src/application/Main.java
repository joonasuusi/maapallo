package application;
	
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.stage.Stage;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;


public class Main extends Application {
	
	private static final float leveys = 1200;
	private static final float korkeus = 800;
	
	private double ankX, ankY;
	private double ankKulmaX = 0;
	private double ankKulmaY = 0;
	private final DoubleProperty kulmaX = new SimpleDoubleProperty(0);
	private final DoubleProperty kulmaY = new SimpleDoubleProperty(0);
	
	private final Sphere sphere = new Sphere(120);
	private final PointLight pl = new PointLight();
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Camera cam = new PerspectiveCamera(true);
			cam.setNearClip(1);
			cam.setFarClip(10000);
			cam.translateZProperty().set(-1000);

			SmartGroup world = new SmartGroup();
			world.getChildren().add(prepareEarth());
			world.getChildren().addAll(prepareLightSource());
			world.getChildren().addAll(new AmbientLight());
			
			Slider slider = prepareSlider();
			world.translateZProperty().bind(slider.valueProperty());
			
			Group root = new Group();
			root.getChildren().add(world);
			root.getChildren().add(prepareImageView());
			root.getChildren().add(slider);
			
			Scene scene = new Scene(root, leveys, korkeus, true);
			scene.setFill(Color.SILVER);
			scene.setCamera(cam);
			
			Transform transform = new Rotate(10
					, new Point3D(0,0,1));
			sphere.getTransforms().add(transform);
			
			
			initMouseControl(world, scene, primaryStage);
			
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setTitle("Maapallo");
			primaryStage.setScene(scene);
			primaryStage.show();
			
			prepareAnimation();
			
			AnimationTimer timer = new AnimationTimer() {
				
				@Override
				public void handle(long now) {
					pl.setRotate(pl.getRotate() + -0.5);
				}
			};
			timer.start();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private Node[] prepareLightSource() {
		pl.setColor(Color.AQUA);
		pl.getTransforms().add(new Translate(0, -100, 200));
		pl.setRotationAxis(Rotate.Y_AXIS);
		
		PhongMaterial pm = new PhongMaterial();
		pm.setDiffuseColor(Color.GRAY);
		
		Sphere s = new Sphere(10);
		s.setMaterial(pm);
		s.getTransforms().setAll(pl.getTransforms());
		s.rotateProperty().bind(pl.rotateProperty());
		s.rotationAxisProperty().bind(pl.rotationAxisProperty());
		
		return new Node[]{pl, s};
	}

	private void prepareAnimation() {
		AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(final long now) {
				sphere.rotateProperty().set(sphere.getRotate() + 0.2);
			}
		};
		timer.start();
	}
	
	private ImageView prepareImageView() {
		Image image = new Image(Main.class.getResourceAsStream("avaruus.jpg"));
		ImageView imageView = new ImageView(image);
		imageView.setPreserveRatio(true);
		imageView.getTransforms().add(new Translate(-image.getWidth()/2, -image.getHeight()/2,
				800));
		
		return imageView;
	}
	
	private Slider prepareSlider() {
		Slider slider = new Slider();
		slider.setMax(800);
		slider.setMin(-400);
		slider.setPrefWidth(300d);
		slider.setLayoutX(-150);
		slider.setLayoutY(200);
		slider.setShowTickLabels(true);
		slider.setTranslateZ(5);
		slider.setStyle("-fx-base: black");
		return slider;
	}

	private Node prepareEarth() {
		PhongMaterial material = new PhongMaterial();
		
		material.setDiffuseMap(new Image(getClass().getResourceAsStream("maa4.jpg")));
		material.setSelfIlluminationMap(new Image(getClass().getResourceAsStream("illuminationmap.jpg")));
		material.setSpecularMap(new Image(getClass().getResourceAsStream("specmaa.jpg")));
		//material.setBumpMap(new Image(getClass().getResourceAsStream("bump2.jpg")));
		
		sphere.setMaterial(material);
		sphere.setRotationAxis(Rotate.Y_AXIS);
		
		return sphere;
	}
	
	private void initMouseControl(SmartGroup group, Scene scene, Stage stage) {
		Rotate xRotate;
		Rotate yRotate;
		group.getTransforms().addAll(
				xRotate = new Rotate(0, Rotate.X_AXIS),
				yRotate = new Rotate(0, Rotate.Y_AXIS)
		);
		xRotate.angleProperty().bind(kulmaX);
		yRotate.angleProperty().bind(kulmaY);
		
		scene.setOnMousePressed(event -> {
			ankX = event.getSceneX();
			ankY = event.getSceneY();
			ankKulmaX = kulmaX.get();
			ankKulmaY = kulmaY.get();
		});
		
		scene.setOnMouseDragged(event -> {
			kulmaX.set(ankKulmaX - (ankY - event.getSceneY()));
			kulmaY.set(ankKulmaY + ankX - event.getSceneX());
		});
		stage.addEventHandler(ScrollEvent.SCROLL, e -> {
			double liike = e.getDeltaY();
			group.translateZProperty().set(group.getTranslateZ() + liike);
		});
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	class SmartGroup extends Group {
		Rotate r;
		Transform t = new Rotate();
		
		
		void rotateByX(int kulma) {
			r = new Rotate(kulma, Rotate.X_AXIS);
			t = t.createConcatenation(r);
			this.getTransforms().clear();
			this.getTransforms().addAll(t);
		}
		
		void rotateByY(int kulma) {
			r = new Rotate(kulma, Rotate.Y_AXIS);
			t = t.createConcatenation(r);
			this.getTransforms().clear();
			this.getTransforms().addAll(t);
		}
		
		void rotateByZ(int kulma) {
			r = new Rotate(kulma, Rotate.Z_AXIS);
			t = t.createConcatenation(r);
			this.getTransforms().clear();
			this.getTransforms().addAll(t);
		}
	}
}
