package knn2;
/**
 *
 * @author asaggese
 */
public class Main {

    public static void main(String[] args) {
        //valore di k per il K-NN. Se voglio usare NN, allora k=1 altrimenti k= (es) 5
        int k = 1;
        String prototypes_filename = "datasetTORCS.csv";

        // Costruisco il mio classificatore a partire dal nome del file dei prototipi
        NearestNeighbor knn = new NearestNeighbor(prototypes_filename);

        // Per ciascun nuovo campione, creo il Sample.
        // Se hai un vettore di feature di più di tre elementi e una classe, cambia l'implementazione della classe Sample di conseguenza.
        Sample testPoint = new Sample(0.0,0.0,0.0,0.0,0.0);

        // Per classificare il campione testPoint, richiama la classe Classify
        int predictedClass = knn.classify(testPoint, k);

        System.out.println("Predicted class for point ("  + ", " + testPoint.sensoreFrontale
                + ", " + testPoint.differenzaSxDxSensor + ", " + testPoint.differenzaSxMinDxMinSensor+ ", " + testPoint.posizioneRispettoAlCentro + ", "
                + testPoint.angoloLongTang + ") is: " + predictedClass);
    }

}
