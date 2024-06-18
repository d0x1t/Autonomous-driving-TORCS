package knn2;

/**
 * Questa classe va cambiata sulla base del vostro vettore di feature. Per ora,
 * considero: un vettore di feature di 3 double (x,y,z) e una classe che è un
 * intero.
 */
public class Sample {

   
    double sensoreFrontale;
    double differenzaSxDxSensor;
    double differenzaSxMinDxMinSensor;
    double posizioneRispettoAlCentro;
    double angoloLongTang;
    int cls;

    public Sample(double sensoreFrontale, double differenzaSxDxSensor, double differenzaSxMinDxMinSensor, double posizioneRispettoAlCentro, double angoloLongTang, int cls) {
        
        
        this.sensoreFrontale = sensoreFrontale;
        this.differenzaSxDxSensor = differenzaSxDxSensor;
        this.differenzaSxMinDxMinSensor = differenzaSxMinDxMinSensor;
        this.posizioneRispettoAlCentro = posizioneRispettoAlCentro;
        this.angoloLongTang = angoloLongTang;
        
        this.cls = cls;

    }

    public Sample(double sensoreFrontale, double sensoreSinistra, double sensoreDestra, double posizioneRispettoAlCentro, double angoloLongTang) {
        
        this.sensoreFrontale = sensoreFrontale;
        this.differenzaSxDxSensor = sensoreSinistra;
        this.differenzaSxMinDxMinSensor = sensoreDestra;
        this.posizioneRispettoAlCentro = posizioneRispettoAlCentro;
        this.angoloLongTang = angoloLongTang;
        
    }


    /*
    Chiamo questo costruttore quando ho la classe di appartenenza e sto costruendo il dataset. 
    In alternativa, quando voglio classificare un nuovo campione, uso l'altro costruttore.
     */
 /*
    Questo costruttore prende la stringa dal file csv e costruisce il Sample
     */
    public Sample(String line) {
        String[] parts = line.split(";");
        this.sensoreFrontale = Double.parseDouble(parts[0].trim());
        this.differenzaSxDxSensor = Double.parseDouble(parts[1].trim());
        this.differenzaSxMinDxMinSensor = Double.parseDouble(parts[2].trim());
        this.posizioneRispettoAlCentro = Double.parseDouble(parts[3].trim());
        this.angoloLongTang = Double.parseDouble(parts[4].trim());
        this.cls = Integer.parseInt(parts[5].trim());
    }

 public double distance(Sample other) {
    return Math.sqrt(
           
             Math.pow(this.sensoreFrontale - other.sensoreFrontale, 2)
            + Math.pow(this.differenzaSxDxSensor - other.differenzaSxDxSensor, 2)
            + Math.pow(this.differenzaSxMinDxMinSensor - other.differenzaSxMinDxMinSensor, 2)
            + Math.pow(this.posizioneRispettoAlCentro - other.posizioneRispettoAlCentro, 2)
            + Math.pow(this.angoloLongTang - other.angoloLongTang, 2)
           
    );
}


}
