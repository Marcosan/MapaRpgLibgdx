package kokoa.induccion.mapa.entities;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;

import java.util.Hashtable;

public class NPC extends Sprite implements InputProcessor {

    /** the movement velocity */
    private Vector2 velocity = new Vector2();

    private float speed = 0.3f , gravity = 0, animationTime = 0, increment;
    private float wPer = 0.3f, hPer = 0.3f;

    private boolean canJump=false, movimiento=false;

    private Animation<TextureRegion> walkAnimation;
    private Hashtable<String, Animation> animation;
    private TiledMapTileLayer collisionLayer;

    private String blockedKey = "blocked";

    private int wMap, hMap, wCam, hCam;
    private float wScreen, hScreen, pointX=0, pointY=0;
    private int seg_wait=0;
    private Crono crono;
    private boolean arriba = false, abajo = false, derecha = false, izquierda = false;
    //Matriz para el recorrido
    private  NpcWalk npcWalk;
    //posicion segun la matriz
    private int global_i = 0, global_j = 0;

    public NPC(Hashtable<String, Animation> animation, TiledMapTileLayer collisionLayer, int wMap, int hMap, int wCam, int hCam, NpcWalk npcWalk, Crono crono, int seg_wait) {
        super((TextureRegion) animation.get("still").getKeyFrame(0));
        //super();
        this.wMap = wMap;
        this.hMap = hMap;
        this.wCam = wCam;
        this.hCam = hCam;
        this.animation = animation;
        this.collisionLayer = collisionLayer;
        this.seg_wait = seg_wait;
        this.crono = crono;
        this.npcWalk = npcWalk;
        //setSize(collisionLayer.getWidth(), collisionLayer.getHeight() );
        setSize(40,40);
        wScreen = Gdx.graphics.getWidth();
        hScreen = Gdx.graphics.getHeight();
    }

    @Override
    public void draw(Batch spriteBatch) {
        update(Gdx.graphics.getDeltaTime());
        super.draw(spriteBatch);
    }

    public void update(float delta) {
        // apply gravity
        velocity.y -= gravity * delta;

        // clamp velocity
        if(velocity.y > speed)
            velocity.y = speed;
        else if(velocity.y < -speed)
            velocity.y = -speed;

        settingMove();

        collisionCharacterX();

        moveCharacterX();

        collisionCharacterY();

        moveCharacterY();

        // update animation
        animationTime += delta;
        setRegion(velocity.x < 0 ? (TextureRegion) animation.get("left").getKeyFrame(animationTime) :
                velocity.x > 0 ? (TextureRegion) animation.get("right").getKeyFrame(animationTime) :
                        (TextureRegion) animation.get("still").getKeyFrame(animationTime));
    }

    private void settingMove() {
        if (crono.getNuSeg() % seg_wait != 0){
            //Durante el movimiento
            movimiento = true;
        } else{
            movimiento = false;
            //despues de n segundos
            int signoX = (int) (Math.random() * 2) + 1;
            signoX = (int) Math.pow((-1), signoX);

            if(signoX < 0){
                izquierda = true;
                derecha = false;
            } else if(signoX > 0){
                derecha = true;
                izquierda = false;
            } else {
                derecha = false;
                izquierda = false;
            }

            int signoY = (int) (Math.random() * 2) + 1;
            signoY = (int) Math.pow((-1), signoY);

            if(signoY < 0){
                abajo = true;
                arriba = false;
            } else if(signoY > 0){
                arriba = true;
                abajo = false;
            } else {
                abajo = false;
                arriba = false;
            }

        }
    }

    private void moveCharacterY() {
        //Para que no se salga del mapa
        if (getY() >= hMap || getY() <= 0){
            //System.out.println("player: "+getY() + ", point: " + relationY);
            velocity.y = 0;
            animationTime = 0;
        }
        //se detiene en el punto marcado velocity.y = 0
        if (!movimiento){
            //System.out.println("player: "+getY() + ", point: " + relationY);
            velocity.y = 0;
            animationTime = 0;
        }else if(arriba){
            //arriba
            velocity.y = -speed;
            animationTime = 0;
        }else if(abajo){
            //abajo
            velocity.y = speed;
            animationTime = 0;
        }

        // move on y
        setPosition(getX(),getY() + velocity.y);

    }

    private void moveCharacterX() {
        //Para que no se salga del mapa
        if (getX() >= wMap || getX() <= 0){
            velocity.x = 0;
            animationTime = 0;
        }
        //se detiene en el punto marcado velocity.x = 0
        if (!movimiento){
            velocity.x = 0;
            animationTime = 0;
        }else if(derecha){
            //derecha
            velocity.x = speed;
            animationTime = 0;
        }else if(izquierda){
            //izquierda
            velocity.x = -speed;
            animationTime = 0;
        }
        setPosition(getX() + velocity.x,getY());
    }

    private void collisionCharacterX(){
        // save old position
        float oldX = getX();
        boolean collisionX = false;

        // calculate the increment for step in #collidesLeft() and #collidesRight()
        increment = collisionLayer.getTileWidth();
        increment = getWidth() < increment ? getWidth() / 2 : increment / 2;

        if(velocity.x < 0) // going left
            collisionX = collidesLeft();
        else if(velocity.x > 0) // going right
            collisionX = collidesRight();

        // react to x collision
        if(collisionX) {
            setX(oldX);
            velocity.x = 0;
        }
    }

    private void collisionCharacterY(){
        float oldY = getY();
        boolean collisionY = false;
        // calculate the increment for step in #collidesBottom() and #collidesTop()
        increment = collisionLayer.getTileHeight();
        increment = getHeight() < increment ? getHeight() / 2 : increment / 2;

        if(velocity.y < 0) // going down
            canJump = collisionY = collidesBottom();
        else if(velocity.y > 0) // going up
            collisionY = collidesTop();

        // react to y collision
        if(collisionY) {
            setY(oldY);
            velocity.y = 0;
        }
    }

    private boolean isCellBlocked(float x, float y) {
        Cell cell = collisionLayer.getCell((int) (x / collisionLayer.getTileWidth()), (int) (y / collisionLayer.getTileHeight()));
        return cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey(blockedKey);
    }

    public boolean collidesRight() {
        for(float step = 0; step <= getHeight(); step += increment)
            if(isCellBlocked(getX() + getWidth(), getY() + step))
                return true;
        return false;
    }

    public boolean collidesLeft() {
        for(float step = 0; step <= getHeight(); step += increment)
            if(isCellBlocked(getX(), getY() + step))
                return true;
        return false;
    }

    public boolean collidesTop() {
        for(float step = 0; step <= getWidth(); step += increment)
            if(isCellBlocked(getX() + step, getY() + getHeight()))
                return true;
        return false;

    }

    public boolean collidesBottom() {
        for(float step = 0; step <= getWidth(); step += increment)
            if(isCellBlocked(getX() + step, getY()))
                return true;
        return false;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public TiledMapTileLayer getCollisionLayer() {
        return collisionLayer;
    }

    public void setCollisionLayer(TiledMapTileLayer collisionLayer) {
        this.collisionLayer = collisionLayer;
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
        this.pointX = screenX;
        this.pointY = screenY;
        movimiento = true;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        velocity.x = 0;
        velocity.y = 0;
        movimiento = false;
        animationTime = 0;
        return true;
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
