package kokoa.induccion.mapa.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import kokoa.induccion.mapa.entities.Crono;
import kokoa.induccion.mapa.entities.NPC;
import kokoa.induccion.mapa.entities.NpcWalk;
import kokoa.induccion.mapa.entities.Player;

/**
 * Created by dell on 10/04/17.
 */

public class Play implements InputProcessor, Screen {
    private static final int CAM_SIZE_X = 700;
    private static final int CAM_SIZE_Y = 600;
    private static final int NUM_NPC = 2;

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    private TextureAtlas playerAtlas;
    private Player player;
    private TextureAtlas npcAtlas;

    private int[] background = new int[] {0}, foreground = new int[] {1}, objLayer = new int[] {2};

    private ShapeRenderer sr;

    private int wMap, hMap, wScreen, hScreen;
    private float wPer = 0.3f, hPer = 0.3f;

    private int touchX, touchY;
    private int velocidad = 6;
    //private int posIniX = 100, posIniY = 500;
    private int posIniX = 5800, posIniY = 5900;

    private String capa = "paredes"; //  Paredes_cercas     paredes

    //NPCs
    private ArrayList<NPC> npcList;


    @Override
    public void show() {
        map = new TmxMapLoader().load("maps/FiecFEPOL.tmx"); // FiecFEPOL   mapaprueba
        wMap = Integer.parseInt(map.getProperties().get("width").toString()) * Integer.parseInt(map.getProperties().get("tilewidth").toString());
        hMap = Integer.parseInt(map.getProperties().get("height").toString()) * Integer.parseInt(map.getProperties().get("tileheight").toString());

        wScreen = Gdx.graphics.getWidth();
        hScreen = Gdx.graphics.getHeight();

        renderer = new OrthogonalTiledMapRenderer(map);
        sr = new ShapeRenderer();
        sr.setColor(Color.CYAN);
        //Gdx.gl.glLineWidth(3);

        camera = new OrthographicCamera();
        camera.setToOrtho(false,CAM_SIZE_X, CAM_SIZE_Y);

        npcList = new ArrayList<NPC>(NUM_NPC);

        Crono crono=new Crono();
        crono.start();

        playerAtlas = new TextureAtlas("img/player/player.pack");
        Hashtable<String, Animation> animation = new Hashtable<String, Animation>();
        Animation still, left, right;
        still = new Animation(1 / 2f, playerAtlas.findRegions("still"));
        left = new Animation(1 / 6f, playerAtlas.findRegions("left"));
        right = new Animation(1 / 6f, playerAtlas.findRegions("right"));

        still.setPlayMode(Animation.PlayMode.LOOP);
        left.setPlayMode(Animation.PlayMode.LOOP);
        right.setPlayMode(Animation.PlayMode.LOOP);

        animation.put("still", still);
        animation.put("left", left);
        animation.put("right", right);

        //INICIO
        npcAtlas = new TextureAtlas("img/player/player.pack");
        Hashtable<String, Animation> animation2 = new Hashtable<String, Animation>();
        Animation still2, left2, right2;
        still2 = new Animation(1 / 2f, npcAtlas.findRegions("still"));
        left2 = new Animation(1 / 6f, npcAtlas.findRegions("left"));
        right2 = new Animation(1 / 6f, npcAtlas.findRegions("right"));

        still2.setPlayMode(Animation.PlayMode.LOOP);
        left2.setPlayMode(Animation.PlayMode.LOOP);
        right2.setPlayMode(Animation.PlayMode.LOOP);

        animation2.put("still", still2);
        animation2.put("left", left2);
        animation2.put("right", right2);
        //FIN

        player = new Player(animation, (TiledMapTileLayer) map.getLayers().get(capa), wMap, hMap, CAM_SIZE_X, CAM_SIZE_Y);

        for (int i = 0; i < NUM_NPC; i++){
            npcList.add(i, new NPC(animation, (TiledMapTileLayer) map.getLayers().get(capa), wMap, hMap, CAM_SIZE_X, CAM_SIZE_Y, new NpcWalk(5,5), crono, 2));
            npcList.get(i).setPosition(posIniX,posIniY);
            Gdx.input.setInputProcessor(npcList.get(i));
        }
        //player = new Player(still, left, right, (TiledMapTileLayer) map.getLayers().get("Paredes_cercas"), wMap, hMap, CAM_SIZE_X, CAM_SIZE_Y);
        //player.setPosition(11 * player.getCollisionLayer().getTileWidth(), (player.getCollisionLayer().getHeight() - 14) * player.getCollisionLayer().getTileHeight());
        player.setPosition(posIniX,posIniY);

        Gdx.input.setInputProcessor(player);

        // ANIMATED TILES

        // frames
        Array<StaticTiledMapTile> frameTiles = new Array<StaticTiledMapTile>(2);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        renderer.setView(camera);

        renderer.render(background);

        renderer.getBatch().begin();
        player.draw(renderer.getBatch());
        for (int i = 0; i < npcList.size(); i++){
            npcList.get(i).draw(renderer.getBatch());
        }
        renderer.getBatch().end();

        renderer.render(foreground);
        renderer.render(objLayer);

        if(Gdx.input.isTouched()){
            camera.position.set(player.getX(), player.getY(), 0);
        }

    }

    public int[][] getPosNpc(){
        int pos_npc[][] = new int[NUM_NPC][2];
        pos_npc[0] = new int[]{1, 2};
        return pos_npc;
    }

    @Override
    public void resize(int width, int height) {
        //camera.viewportWidth = width;
        //camera.viewportHeight = height;
        camera.position.set(posIniX, posIniY, 0); //by default camera position on (0,0,0)
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        sr.dispose();
        playerAtlas.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
