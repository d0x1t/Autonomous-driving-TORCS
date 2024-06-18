package provaKDTree;

/**
 * Questa classe va cambiata sulla base del vostro vettore di feature.
 * Per ora, considero: 
 * un vettore di feature di 3 double (x,y,z) e una classe che è un intero.
 */

public class Sample {
    double x;
    double y;
    double z;
    int cls;

    /*
    Chiamo questo costruttore quando ho la classe di appartenenza e sto costruendo il dataset. 
    In alternativa, quando voglio classificare un nuovo campione, uso l'altro costruttore.
    */
    public Sample(double x, double y, double z, int cls) {
        this.x = x;
        this.y = y;
        this.cls = cls;
    }

    public Sample(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.cls = -1;
    }

    /*
    Questo costruttore prende la stringa dal file csv e costruisce il Sample
    */
    public Sample (String line){
        String[] parts = line.split(";");
        this.x = Double.parseDouble(parts[0].trim());
        this.y = Double.parseDouble(parts[1].trim());
        this.z = Double.parseDouble(parts[2].trim());
        this.cls = Integer.parseInt(parts[3].trim());
    }
 
    
    public double distance(Sample other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2) + Math.pow(this.z - other.z, 2));
    }
    
    
}