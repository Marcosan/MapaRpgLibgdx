package kokoa.induccion.mapa.entities;

public class Crono extends Thread {
    private int nuSeg = 0;
    public Crono(){
        super();
    }
    public void run() {
        try {//si ocurre un error al dormir el proceso(sleep(999))
            for (; ;){ //for infinito
                nuSeg++;
                System.out.println(nuSeg);
                sleep(999);//Duermo el hilo durante 999 milisegundos(casi un segundo, quintandole el tiempo de proceso)
                if (nuSeg > 60){
                    nuSeg = 0;
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public int getNuSeg() {
        return nuSeg;
    }
}
