package provaKDTree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author asaggese
 */
public class Main {
    
    
        public static void main(String[] args) {
             String filePath = "data.csv";
        File file = new File(filePath);
        boolean isDeleted = file.delete();

       
        try (BufferedWriter data = new BufferedWriter(new FileWriter(filePath, true))) {
            data.append("x;y;z;Cls");
            data.newLine();
         //   for(double i = 0; i < 5 ; i++){
            data.append(Double.toString(50)).append(";");
            data.append(Double.toString(33)).append(";");
            data.append(Double.toString(300)).append(";");
            data.append(Integer.toString(0)).append("\n");
             data.append(Double.toString(50)).append(";");
            data.append(Double.toString(100)).append(";");
            data.append(Double.toString(290)).append(";");
            data.append(Integer.toString(1)).append("\n");
           
           // }
            System.out.println("Contenuto aggiunto al file con successo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
            //valore di k per il K-NN. Se voglio usare NN, allora k=1 altrimenti k= (es) 5
            int k = 1;
            String prototypes_filename = "data.csv";
            
            // Costruisco il mio classificatore a partire dal nome del file dei prototipi
            NearestNeighbor knn = new NearestNeighbor(prototypes_filename);

            // Per ciascun nuovo campione, creo il Sample.
            // Se hai un vettore di feature di più di tre elementi e una classe, cambia l'implementazione della classe Sample di conseguenza.
            Sample testPoint = new Sample(0, 100, 299);

            // Per classificare il campione testPoint, richiama la classe Classify
            int predictedClass = knn.classify(testPoint, k);

            System.out.println("Predicted class for point (" + testPoint.x + ", " + testPoint.y +", " + testPoint.z+ ") is: " + predictedClass);
    }
    
}
