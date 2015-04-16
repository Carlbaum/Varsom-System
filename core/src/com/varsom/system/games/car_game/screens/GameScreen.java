package com.varsom.system.games.car_game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

import com.varsom.system.games.car_game.gameobjects.Car;
import com.varsom.system.games.car_game.tracks.Track;
import com.varsom.system.games.car_game.tracks.Track1;
import com.varsom.system.games.car_game.tracks.Track2;


public class GameScreen implements Screen{

    public static final int STEER_NONE=0;
    public static final int STEER_RIGHT=1;
    public static final int STEER_LEFT=2;

    public static final int ACC_NONE=0;
    public static final int ACC_ACCELERATE=1;
    public static final int ACC_BRAKE=2;

    public static int level;

    // For countdown
    private static float countDownTimer = 5.0f;
    private static boolean paused = true;

    // Class variables
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;

    private final float TIMESTEP = 1/60f;
    private final int   VELOCITY_ITERATIONS = 8,
                        POSITION_ITERATIONS = 3;

    private SpriteBatch batch;

    // TODO Implement class Track
    //private /*Track*/TestTrack track;
    private Track track;

//    private Pixmap pixmap;
    private Car leaderCar;
//    private MoveSprite moveSprite;
//    private Sprite bgSprite;

    public GameScreen(int level){

        this.level = level;
        world = new World(new Vector2(0f,0f),true);
        debugRenderer = new Box2DDebugRenderer();
        int SCREEN_WIDTH = Gdx.graphics.getWidth();
        int SCREEN_HEIGHT = Gdx.graphics.getHeight();

        // Create objects and select level
        switch(level){
            case 1:
                track = new Track1(world);
////                moveSprite = testTrack.moveSprite;
//                car = track.car;
//                pixmap = track.pixmap;
//                bgSprite = track.backgroundSprite;
                break;
            case 2:
                track = new Track2(world);
//                moveSprite = track2.moveSprite;
                //car = track2.car;
                //pixmap = track2.pixmap;
                //bgSprite = track2.backgroundSprite;
                break;
            default:
                System.out.println("Mega Error");

        }
        leaderCar = track.getCars()[0];

        // Init camera
        //TODO /100 should probably be changed
        camera = new OrthographicCamera(SCREEN_WIDTH/100,SCREEN_HEIGHT/100);
        //camera.rotate(-90);
//        camera.position.set(new Vector2(moveSprite.getX(),moveSprite.getY()), 0);
        //TODO camera.position.set(leaderCar.getPointOnTrack(), 0);
        camera.position.set(track.getCars()[0].getPointOnTrack(), 0);
        camera.rotate((float)Math.toDegrees(leaderCar.getRotationTrack())-180);
        camera.zoom = 5.0f; // can be used to see the entire track
        camera.update();

        batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);

    }

    private void handleCountDownTimer(){

        countDownTimer -= Gdx.graphics.getDeltaTime();
        float secondsLeft = (int)countDownTimer % 60;

        // Render some kick-ass countdown label
        if(secondsLeft > 0){

            Gdx.app.log("COUNTDOWN: ", (int)secondsLeft + "");

        }

        if(secondsLeft == 0){

            paused = false;

        }

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0.7f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleCountDownTimer();

        batch.setProjectionMatrix(camera.combined);

        track.addToRenderBatch(batch,camera);

        //debugRenderer.render(world, camera.combined);

        world.step(TIMESTEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);

        //world.clearForces();
        // Set camera position to same as car

        // Get current angle from body
        //float playerAngle = constrainAngle(car.body.getAngle()*MathUtils.radiansToDegrees);

       // Here goes the all the updating / game logic
       if(!paused){
           for(Car car : track.getCars()) {
               car.update(Gdx.app.getGraphics().getDeltaTime());
           }

           updateCamera();}
    }

    private void updateCamera(){
        float smoothCamConst = 0.1f;
        // TODO LEADER CAR
        //leaderCar = track.getLeaderCar();
        //leaderCar = track.getCars()[0];
        float newCamPosX = (leaderCar.getPointOnTrack().x - camera.position.x);
        float newCamPosY = (leaderCar.getPointOnTrack().y - camera.position.y);
        Vector2 newPos = new Vector2(camera.position.x+newCamPosX*smoothCamConst,camera.position.y+newCamPosY*smoothCamConst);
        //Gdx.app.log("CAMERA","Camera position: " + camera.position);
        if (newPos.x == Float.NaN || newPos.y == Float.NaN) {
            Gdx.app.log("FUUUUDGE","ERROR");
        }
        camera.position.set(newPos,0);

        // Convert camera angle from [-180, 180] to [0, 360]
        float camAngle = -getCurrentCameraAngle(camera) + 180;

        float desiredCamRotation = (camAngle - (float)Math.toDegrees(leaderCar.getRotationTrack())-90);

        if(desiredCamRotation > 180){
            desiredCamRotation -= 360;
        }
        else if(desiredCamRotation < -180) {
            desiredCamRotation += 360;
        }

        camera.rotate(desiredCamRotation*0.02f);

        camera.update();
        //Gdx.app.log("MoveSprite", "MoveSprite: (" + moveSprite.getX() + ", " + moveSprite.getY() +")");

    }

    private float getCurrentCameraAngle(OrthographicCamera cam)
    {
        return (float)Math.atan2(cam.up.x, cam.up.y)*MathUtils.radiansToDegrees;
    }

    @Override
    public void resize(int width, int height) {
        //Gdx.app.log("GameScreen", "resizing in here");
    }

    @Override
    public void show() {
        //Gdx.app.log("GameScreen", "show called");
    }

    @Override
    public void hide() {
        //Gdx.app.log("GameScreen", "hide called");
        dispose();
    }

    @Override
    public void pause() {
        //Gdx.app.log("GameScreen", "pause called");
    }

    @Override
    public void resume() {
        //Gdx.app.log("GameScreen", "resume called");
    }

    @Override
    public void dispose() {
        // Leave blank
    }

}