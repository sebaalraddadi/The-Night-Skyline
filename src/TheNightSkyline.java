import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.PointLight;
import javafx.scene.AmbientLight;
import javafx.scene.shape.Box;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.image.Image;
import javafx.scene.transform.Translate;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Rotate;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.animation.AnimationTimer;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Interpolator;
import javafx.geometry.Point3D;
import javafx.util.Duration;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.CubicCurveTo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TheNightSkyline extends Application {

    private static final double SCENE_W = 1200;
    private static final double SCENE_H = 800;
    private PerspectiveCamera camera;
    private Group root3D = new Group();
    
    private double cameraX = 0.0 ;
    private double cameraY = -100.0;
    private double cameraZ = -800.0;
    private double rotationX = 0;
    private double rotationY = 0;
    private final double CAMERA_SPEED = 20.0 ;
    
    private List<Particle> rainDrops = new ArrayList<>();
    private Group weatherEffect = new Group();
    private boolean rainfall = true;
    
    private boolean cinematicMode = false;
    private Timeline cameraTimeline;

    @Override
    public void start(Stage primaryStage) {
        SubScene subScene = new SubScene(root3D, SCENE_W, SCENE_H, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(10, 10, 30));

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(cameraZ);
        camera.setTranslateY(cameraY);
        subScene.setCamera(camera);
        
        Group root = new Group(subScene);
        primaryStage.setTitle("The Night Skyline");
        primaryStage.setScene(new Scene(root, SCENE_W, SCENE_H));
        primaryStage.show();

        Lights();
        MoonAndStars();
        City();
        
      MeshBuilding();
        
        root3D.getChildren().add(weatherEffect);
        startParticleSystem();
        
        setupCamera();
        
        handleUserControls(primaryStage.getScene());
    }
    
    private void LouvreDome() {
        TriangleMesh domeMesh = new TriangleMesh();
        
        float radius = 80f;
        int segments = 24;
        int rings = 12;
        
        for (int ring = 0; ring <= rings; ring++) {
            double phi = Math.PI * 0.5 * ring / rings; 
            float y = (float)(-radius * Math.cos(phi));
            float ringRadius = (float)(radius * Math.sin(phi));
            
            for (int seg = 0; seg <= segments; seg++) {
                double theta = 2 * Math.PI * seg / segments;
                float x = (float)(ringRadius * Math.cos(theta));
                float z = (float)(ringRadius * Math.sin(theta));
                
                domeMesh.getPoints().addAll(x, y, z);
            }
        }
        
        for (int ring = 0; ring <= rings; ring++) {
            for (int seg = 0; seg <= segments; seg++) {
                float u = (float)seg / segments;
                float v = (float)ring / rings;
                domeMesh.getTexCoords().addAll(u, v);
            }
        }
        
        for (int ring = 0; ring < rings; ring++) {
            for (int seg = 0; seg < segments; seg++) {
                int curr = ring * (segments + 1) + seg;
                int next = curr + segments + 1;
                
                domeMesh.getFaces().addAll(
                    curr, curr,
                    next, next,
                    curr + 1, curr + 1
                );
                
                domeMesh.getFaces().addAll(
                    curr + 1, curr + 1,
                    next, next,
                    next + 1, next + 1
                );
            }
        }
        
        MeshView dome = new MeshView(domeMesh);
        dome.setCullFace(CullFace.NONE);
        
     PhongMaterial domeMat = new PhongMaterial();
        domeMat.setDiffuseMap(LouvrePattern(512, 512));
        domeMat.setSpecularColor(Color.rgb(150, 150, 150));
        dome.setMaterial(domeMat);
        
        Box base1 = new Box(100, 30, 80);
        base1.setMaterial(new PhongMaterial(Color.rgb(200, 190, 180)));
        base1.setTranslateY(-15);
        
        Box base2 = new Box(80, 25, 100);
        base2.setMaterial(new PhongMaterial(Color.rgb(195, 185, 175)));
        base2.setTranslateY(-12.5);
        base2.setTranslateX(60);
        
        Group louvre = new Group(dome, base1, base2);
        louvre.setTranslateX(-500);
        louvre.setTranslateY(0);  
        louvre.setTranslateZ(400);
        
        root3D.getChildren().add(louvre);
    }
    
    private Image LouvrePattern(int width, int height) {
        WritableImage image = new WritableImage(width, height);
        PixelWriter pw = image.getPixelWriter();
        
        int gridSize = 20 ; 
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean inPattern = false;
              
                int modX = x % gridSize;
                int modY = y % gridSize;
                
                if (modX < 2 || modY < 2) {
                    inPattern = true;
                }
                
                if ((modX + modY) % gridSize < 2) {
                    inPattern = true;
                }
                
                Color color;
                if (inPattern) {
                    color = Color.rgb(140, 120, 90);
                    
                }   else{
                   
                    double noise = perlinNoise(x * 0.05, y * 0.05);
                    int brightness = (int)(180 + noise * 50);
                    color = Color.rgb(
                        Math.min(255,  brightness),
                        Math.min(255, brightness - 20),
                        Math.min(255, brightness - 40)
                    );
                }
               
                pw.setColor(x, y, color);
             }
        }
        return image;
    }

    private void Lights() {
        AmbientLight ambientLight = new AmbientLight(Color.rgb(40, 40, 60));
        root3D.getChildren().add(ambientLight);

        PointLight streetLight1 = new PointLight(Color.ORANGE);
        streetLight1.setTranslateX(100);
        streetLight1.setTranslateY(-50);
        streetLight1.setTranslateZ(0);
        
        PointLight streetLight2 = new PointLight(Color.LIGHTYELLOW);
        streetLight2.setTranslateX(-200);
        streetLight2.setTranslateY(-50);
        streetLight2.setTranslateZ(100);
        
        root3D.getChildren().addAll(streetLight1, streetLight2);
    }

    private void MoonAndStars() {
        Sphere moon = new Sphere(25);
        PhongMaterial moonMaterial = new PhongMaterial(Color.LIGHTYELLOW);
        moonMaterial.setSpecularColor(Color.WHITE);
        moon.setMaterial(moonMaterial);
        moon.setTranslateX(500);
        moon.setTranslateY(-400);
        moon.setTranslateZ(1500);
        
        PointLight moonLight = new PointLight(Color.rgb(180, 180, 220));
        moonLight.setTranslateX(500);
        moonLight.setTranslateY(-400);
        moonLight.setTranslateZ(1500);
        
        root3D.getChildren().addAll(moon, moonLight);
        createStars(300);
    }

    private void createStars(int count) {
        PhongMaterial starMaterial = new PhongMaterial(Color.WHITE);
        starMaterial.setSelfIlluminationMap(createSolidColorImage(Color.WHITE, 4, 4));
        
        final double MAX_DISTANCE = 5000;
        Random rand = new Random();

        for (int i = 0; i < count; i++) {
            Sphere star = new Sphere(rand.nextDouble() * 1.5 + 0.5);
            star.setMaterial(starMaterial);
            star.setTranslateX(rand.nextDouble() * MAX_DISTANCE - MAX_DISTANCE / 2);
            star.setTranslateY(-(rand.nextDouble() * 600 + 200));
            star.setTranslateZ(rand.nextDouble() * MAX_DISTANCE - MAX_DISTANCE / 2);
            root3D.getChildren().add(star);
        }
    }

    private Box createBuilding(double width, double height, double depth, String textureUrl, double translateX, double translateZ) {
        Box building = new Box(width, height, depth);
        PhongMaterial material = new PhongMaterial();
        try {
            Image texture = new Image(textureUrl);
            material.setDiffuseMap(texture);
        } catch (Exception e) {
            material.setDiffuseColor(Color.DARKGRAY);
        }
        building.setMaterial(material);
        building.getTransforms().add(new Translate(translateX, -height / 2, translateZ));
        root3D.getChildren().add(building);
        return building;
    }

    private void City() {
        Box ground = new Box(3000, 10 , 3000);
        PhongMaterial groundMat = new PhongMaterial(Color.rgb(30, 30, 40) );
        ground.setMaterial(groundMat);
        root3D.getChildren().add(ground);
        
        createBuilding(50, 200, 50, "/resources/img/w3.jpeg", -150, 50 );
        createBuilding(80, 150, 60, "/resources/img/night_windows_1.jpeg", 100, -80 );
        createBuilding(80, 350, 60, "/resources/img/w7.jpeg", -250, 200);
        createBuilding (70, 250 , 70, "/resources/img/w3.jpeg", 200, 150);
        createBuilding(120, 100, 40 , "/resources/img/night_windows_3.jpg", -380, -50 );
        createBuilding(40, 300, 40, "/resources/img/w2.jpeg", 370, 400);
        createBuilding(60, 280, 60, "/resources/img/w3.jpeg", -50, 300);
        createBuilding(90, 180, 70, "/resources/img/night_windows_1.jpeg", 300, -150); }
        
    
    private void setupCamera() {
        double[][] bezierPoints = {
            {-600, -150, -800},
            {-300, -200, -400}, 
            {300, -100, -200},
            {600, -150, -600}    
        };
        
        cameraTimeline = new Timeline();
        int steps = 200; 
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            Point3D point = BezierPoint(t, bezierPoints);
            
            double time = i *50;  
            KeyFrame kf = new KeyFrame(Duration.millis(time),
                new KeyValue(camera.translateXProperty(), point.getX(), Interpolator.EASE_BOTH ),
                new KeyValue(camera.translateYProperty(), point.getY(), Interpolator.EASE_BOTH ),
                new KeyValue(camera.translateZProperty(), point.getZ(), Interpolator.EASE_BOTH )
            ) ;
            cameraTimeline.getKeyFrames().add(kf);
        }
        
        cameraTimeline.setCycleCount(Timeline.INDEFINITE);
        cameraTimeline.setAutoReverse(true);
    }
    
    private Point3D BezierPoint(double t, double[][] points) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;
        
        double x = uuu * points[0][0] + 
                   3 * uu * t * points[1][0] + 
                   3 * u * tt * points[2][0] + 
                   ttt * points[3][0];
        
        double y = uuu * points[0][1] + 
                   3 * uu * t * points[1][1] + 
                   3 * u * tt * points[2][1] + 
                   ttt * points[3][1];
        
        double z = uuu * points[0][2] + 
                   3 * uu * t * points[1][2] + 
                   3 * u * tt * points[2][2] + 
                   ttt * points[3][2];
        
        return new Point3D(x, y, z);
    }

   //particle System (rain)
 private class Particle {
    Sphere sphere;
    double velocityY;
    double velocityX;
    double velocityZ;
    double life;
    double maxLife;
    
    Particle(double x, double y, double z) {
        sphere = new Sphere(0.5);
        PhongMaterial mat = new PhongMaterial(Color.rgb(150, 180, 255, 0.7));
        sphere.setMaterial(mat);
        sphere.setTranslateX(x);
        sphere.setTranslateY(y);
        sphere.setTranslateZ(z);
        
        Random rand = new Random();
        velocityY = 8 + rand.nextDouble() * 4;
        velocityX = (rand.nextDouble() - 0.5) * 0.5;
        velocityZ = (rand.nextDouble() - 0.5) * 0.5;
            
        maxLife = 150 + rand.nextDouble() * 100;
        life = maxLife;
    }
    
    void update() {
        sphere.setTranslateY(sphere.getTranslateY() + velocityY);
        sphere.setTranslateX(sphere.getTranslateX() + velocityX);
        sphere.setTranslateZ(sphere.getTranslateZ() + velocityZ);
        life--;
    }
    
    boolean isDead() {
        if (sphere.getTranslateY() > 10 || life <= 0) {
            return true;
        }
        return false;
    }
    
    void reset() {
        Random rand = new Random();
        sphere.setTranslateX(camera.getTranslateX() + rand.nextDouble() * 800 - 400);
        sphere.setTranslateY(-500 - rand.nextDouble() * 200);
        sphere.setTranslateZ(camera.getTranslateZ() + rand.nextDouble() * 800 - 400);
        life = maxLife;
    }
}

private void startParticleSystem() {
    Random rand = new Random();
    for (int i = 0; i < 500; i++) {
        double x = rand.nextDouble() * 1600 - 800 ;
        double y = -rand.nextDouble() * 600 ;
        double z = rand.nextDouble() * 1600 - 800;
        
        Particle p = new Particle(x, y, z) ;
        rainDrops.add(p);
        weatherEffect.getChildren().add(p.sphere);
    }
    
    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (!rainfall) {
                return;
            }
            for (Particle p : rainDrops) {
                p.update();
                
                if (p.isDead()) {
                    p.reset();
                }
            }
        }
    };
    timer.start();
 }
    
    // 3D tringle Mesh & texture
    private void MeshBuilding() {
        LouvreDome();
        TriangleMesh pyramidMesh = new TriangleMesh();
        
        float h = 400;
        float s = 120 ;
        
        pyramidMesh.getPoints().addAll(
            0,    - h,   0,      
            -s,    0,  -s ,   
             s,    0,  - s,    
             s ,    0,   s ,    
            -s,    0 ,   s      
        );
        
        pyramidMesh.getTexCoords().addAll(
            0.5f, 0,    
            0, 1,    
                 1, 1 ,    
            0.5f, 1     
        );
        
        pyramidMesh.getFaces().addAll(
            0,0,  2,2,  1,1,   
            0,0,  3 ,2,  2 ,1,
           0,0,    4,2,    3,1,  
            0,0 ,  1,2,  4,1,   
            1, 1,  2,2,  3 ,3,
            1,1,  3,3,  4,2
        );
        
        MeshView pyramid = new MeshView(pyramidMesh);
        pyramid.setCullFace(CullFace.NONE);
        pyramid.setDrawMode(DrawMode.FILL ) ;
        PhongMaterial proceduralMat = new PhongMaterial();
        proceduralMat.setDiffuseMap(PerlinNoiseTexture (256, 256));
        proceduralMat.setSpecularColor(Color.rgb(100, 100, 120)  );
        proceduralMat.setSpecularPower( 20);
        pyramid.setMaterial(proceduralMat );
        
        pyramid.setTranslateX(20 );
        pyramid.setTranslateY(h / 2 );   // edit the height to make it look better
        pyramid.setTranslateZ(100 );
        
        root3D.getChildren().add(pyramid);
        
        MarbleMeshBuilding();
    }
    
    private Image PerlinNoiseTexture(int width, int height) {
        WritableImage image = new WritableImage(width, height);
        PixelWriter pw = image.getPixelWriter();
        
        double scale = 0.02 ;
        for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
                double noise = perlinNoise(x * scale, y * scale);
                
                noise = (noise + 1) / 2;
                                int r = (int)(30 + noise * 100);
                int g = (int)(40 + noise * 110);
                int b = (int)(60 + noise * 130);
                
                pw.setColor(x, y, Color.rgb(
                    Math.min(255, Math.max(0, r)),
                    Math.min(255, Math.max(0, g)),
                    Math.min(255, Math.max(0, b))
                ) )
                  ;
            }
        }
        return image;}
     
    
    private double perlinNoise(double x, double y) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);
        double u = fade(xf );
        double  v = fade(yf ) ;
        
        double aa = grad(hash(xi, yi), xf, yf);
        double ab = grad(hash(xi, yi + 1), xf, yf - 1);
        double ba = grad(hash(xi + 1, yi), xf - 1, yf);
        double bb = grad(hash(xi + 1, yi + 1), xf - 1, yf - 1);
        double x1 = lerp(aa, ba, u);
        double x2 = lerp(ab, bb, u);
        
        return lerp(x1, x2, v);
    }
    
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
    
    private int hash(int x, int y) {
        int h = x * 374761393 + y * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        return h ^ (h >> 16);
    }
    
    private double grad(int hash, double x, double y) {
        int h = hash & 3;
        double u = h < 2 ? x : y;
        double v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
    
    private void MarbleMeshBuilding() {
        TriangleMesh mesh = new TriangleMesh();
        
        float w = 40, h = 180, d = 40;
        
        mesh.getPoints().addAll(
            -w, -h, -d,  w, -h, -d,  w, 0, -d,  -w, 0, -d,  
            -w, -h,  d,  w, -h,  d,  w, 0,  d,  -w, 0,  d   
        );
        
        mesh.getTexCoords().addAll(
            0, 0,  1, 0,  1, 1,  0, 1
        );
        
        mesh.getFaces().addAll(
            0,0, 1,1, 2,2,  0,0, 2,2, 3,3,  
            5,0, 4,1, 7,2,  5, 0, 7, 2, 6,3,  
            4,0, 0 ,1, 3 ,2,  4,0 , 3,2, 7,3,  
            1,0, 5,1, 6,2,  1, 0, 6, 2, 2,3,  
            4,0, 5,1, 1,2,  4,0, 1,2, 0, 3 ,
            3,0, 2,1, 6,2,  3,0, 6,2 , 7,3  
        );
        
        MeshView building = new MeshView(mesh);
        building.setCullFace(CullFace.NONE);
        
        PhongMaterial marble = new PhongMaterial();
        marble.setDiffuseMap(marbleTexture(256, 256));
        building.setMaterial(marble);
        
        building.setTranslateX(300);
        building.setTranslateY(0);
        building.setTranslateZ(250);
        
        root3D.getChildren().add(building);
    }
    
    private Image marbleTexture(int w, int h) {
        WritableImage image = new WritableImage(w, h);
        PixelWriter pw = image.getPixelWriter();
        
        for (int y = 0; y < h; y++) {
          for (int x = 0; x < w; x++ ) {
                double noise = perlinNoise(x * 0.03, y * 0.03);
                double marble = Math.sin((x + noise * 100) * 0.05);
                marble = (marble + 1) / 2;
                
                int gray = (int)(180 + marble * 60);
                int variation = (int)(noise * 30);
                
                pw.setColor(x, y,Color.rgb(
                    Math.min(255, gray+ variation ),
                    Math.min(255, gray+ variation-10),
                    Math.min(255, gray+variation-5)
                ))
 
                    ;
            }
        }
        return image;
    }
    
    private Image createSolidColorImage(Color color, int w, int h) {
        WritableImage img = new WritableImage(w, h);
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pw.setColor(x, y, color);
            }
        }
        return img;
    }

    private void handleUserControls(Scene scene) {
        camera.setTranslateX(cameraX);
        camera.setTranslateY(cameraY);
        camera.setTranslateZ(cameraZ);
        camera.setRotationAxis(Rotate.Y_AXIS);
        
        scene.setOnKeyPressed(event ->{
            if (cinematicMode && event.getCode() != KeyCode.C) return;
            
            switch (event.getCode()) {
                case W: case UP:
                    cameraZ += CAMERA_SPEED ;
                    break;
                case S: case DOWN:
                    cameraZ -= CAMERA_SPEED;
                    break;
                case A: case LEFT:
                    cameraX -= CAMERA_SPEED;
                    break;
                case D: case RIGHT:
                    cameraX += CAMERA_SPEED;
                    break;
                case Q :
                    cameraY -= CAMERA_SPEED;
                    break;
                case E: 
                    cameraY += CAMERA_SPEED;
                    break;
                case R :
                    rainfall = !rainfall;
                    weatherEffect.setVisible(rainfall);
                    break;
                case C:
                    cinematicMode = !cinematicMode;
                    if (cinematicMode) {
                        cameraTimeline.play();
                    } else {
                        cameraTimeline.stop();
                        camera.setTranslateX(cameraX);
                        camera.setTranslateY(cameraY);
                        camera.setTranslateZ(cameraZ);
                    }
                    break;
            }
            
            if (!cinematicMode) {
                camera.setTranslateX(cameraX);
                camera.setTranslateY(cameraY);
                camera.setTranslateZ(cameraZ);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
