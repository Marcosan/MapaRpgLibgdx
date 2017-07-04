package kokoa.induccion.mapa.entities;

public class NpcWalk {

    private int a = 0, b = 0;
    private Integer ruta[][] = new Integer[a][b];

    public NpcWalk(int a, int b){
        this.a = a;
        this.b = b;
    }

    public Integer[][] getPathRandom(){
        for (int i=0; i<ruta.length; i++){
            for (int j=0; j<ruta.length; j++){
                ruta[i][j] = (int) (Math.random() * 1);
            }
        }
        return ruta;
    }
}
