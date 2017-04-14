package kokoa.induccion.mapa.screens;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

import kokoa.induccion.mapa.entities.Player;

/**
 * Created by dell on 10/04/17.
 */

public class Play implements Screen {
    private static final int CAM_SIZE_X = 960;
    private static final int CAM_SIZE_Y = 640;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    private TextureAtlas playerAtlas;
    private Player player;

    private int[] background = new int[] {0}, foreground = new int[] {1};

    private ShapeRenderer sr;

    private int wMap, hMap;

    @Override
    public void show() {
        map = new TmxMapLoader().load("maps/mapaprueba.tmx");
        wMap = Integer.parseInt(map.getProperties().get("width").toString()) * Integer.parseInt(map.getProperties().get("tilewidth").toString());
        hMap = Integer.parseInt(map.getProperties().get("height").toString()) * Integer.parseInt(map.getProperties().get("tileheight").toString());

        renderer = new OrthogonalTiledMapRenderer(map);
        sr = new ShapeRenderer();
        sr.setColor(Color.CYAN);
        Gdx.gl.glLineWidth(3);

        camera = new OrthographicCamera();
        camera.setToOrtho(false,wMap, hMap);

        playerAtlas = new TextureAtlas("img/player/player.pack");
        Animation still, left, right;
        still = new Animation(1 / 2f, playerAtlas.findRegions("still"));
        left = new Animation(1 / 6f, playerAtlas.findRegions("left"));
        right = new Animation(1 / 6f, playerAtlas.findRegions("right"));
        still.setPlayMode(Animation.PlayMode.LOOP);
        left.setPlayMode(Animation.PlayMode.LOOP);
        right.setPlayMode(Animation.PlayMode.LOOP);

        player = new Player(still, left, right, (TiledMapTileLayer) map.getLayers().get("Paredes_cercas"), wMap, hMap, CAM_SIZE_X, CAM_SIZE_Y);
        //player.setPosition(11 * player.getCollisionLayer().getTileWidth(), (player.getCollisionLayer().getHeight() - 14) * player.getCollisionLayer().getTileHeight());
        player.setPosition(150,300);

        Gdx.input.setInputProcessor(player);

        // ANIMATED TILES

        // frames
        Array<StaticTiledMapTile> frameTiles = new Array<StaticTiledMapTile>(2);

        // get the frame tiles
        Iterator<TiledMapTile> tiles = map.getTileSets().getTileSet("tileset").iterator();
        while(tiles.hasNext()) {
            TiledMapTile tile = tiles.next();
            if(tile.getProperties().containsKey("animation") && tile.getProperties().get("animation", String.class).equals("flower"))
                frameTiles.add((StaticTiledMapTile) tile);
        }

        // create the animated tile
        AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(1 / 3f, frameTiles);

        // background layer
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Paredes_cercas");

        // replace static with animated tile
        for(int x = 0; x < layer.getWidth(); x++)
            for(int y = 0; y < layer.getHeight(); y++) {
                Cell cell = layer.getCell(x, y);
                //if(cell.getTile().getProperties().containsKey("animation") && cell.getTile().getProperties().get("animation", String.class).equals("flower"))
                //    cell.setTile(animatedTile);
            }

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //posicion de la camara segun player
        //camera.position.set(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2, 0);
        camera.update();

        renderer.setView(camera);

        renderer.render(background);

        renderer.getBatch().begin();
        player.draw(renderer.getBatch());
        renderer.getBatch().end();

        renderer.render(foreground);

        // render objects
        sr.setProjectionMatrix(camera.combined);
        if (map.getLayers().get("objects") != null)
        for(MapObject object : map.getLayers().get("objects").getObjects())
            if(object instanceof RectangleMapObject) {
                RectangleMapObject rectObject = (RectangleMapObject) object;
                Rectangle rect = rectObject.getRectangle();
                if(rectObject.getProperties().containsKey("gid")) { // if it contains the gid key, it's an image object from Tiled
                    int gid = rectObject.getProperties().get("gid", Integer.class);
                    TiledMapTile tile = map.getTileSets().getTile(gid);
                    renderer.getBatch().begin();
                    renderer.getBatch().draw(tile.getTextureRegion(), rect.x, rect.y);
                    renderer.getBatch().end();
                } else { // otherwise, it's a normal RectangleMapObject
                    sr.begin(ShapeType.Filled);
                    sr.rect(rect.x, rect.y, rect.width, rect.height);
                    sr.end();
                }
            } else if(object instanceof CircleMapObject) {
                Circle circle = ((CircleMapObject) object).getCircle();
                sr.begin(ShapeType.Filled);
                sr.circle(circle.x, circle.y, circle.radius);
                sr.end();
            } else if(object instanceof EllipseMapObject) {
                Ellipse ellipse = ((EllipseMapObject) object).getEllipse();
                sr.begin(ShapeType.Filled);
                sr.ellipse(ellipse.x, ellipse.y, ellipse.width, ellipse.height);
                sr.end();
            } else if(object instanceof PolylineMapObject) {
                Polyline line = ((PolylineMapObject) object).getPolyline();
                sr.begin(ShapeType.Line);
                sr.polyline(line.getTransformedVertices());
                sr.end();
            } else if(object instanceof PolygonMapObject) {
                Polygon poly = ((PolygonMapObject) object).getPolygon();
                sr.begin(ShapeType.Line);
                sr.polygon(poly.getTransformedVertices());
                sr.end();
            }

    }

    @Override
    public void resize(int width, int height) {
        //camera.viewportWidth = width / 2.5f;
        //camera.viewportHeight = height / 2.5f;
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

}
