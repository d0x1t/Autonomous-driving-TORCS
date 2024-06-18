package knn2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NearestNeighbor {


    private List<Sample> trainingData;
    private KDTree kdtree;
    private int[] classCounts; // VERIFICA NEI COSTRUTTORI CHE QUESTO SIA CONFORME CON QUELLO CHE STAI IPOTIZZANDO!
  
    


    public NearestNeighbor(String filename) {
        this.trainingData = new ArrayList<>();
        this.kdtree = null;
        this.classCounts = new int[5]; // Assuming classes are labeled 0-9
        
        this.readPointsFromCSV(filename);
    }
    

    private void readPointsFromCSV(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                trainingData.add(new Sample(line));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.kdtree = new KDTree(trainingData); // Inizializza il KDTree utilizzando i punti letti
    }

    public List<Sample> findKNearestNeighbors(Sample testPoint, int k) {
        return kdtree.kNearestNeighbors(testPoint, k);
    }

   public int classify(Sample testPoint, int k) {
    List<Sample> kNearestNeighbors = findKNearestNeighbors(testPoint, k);

    // Inizializza un array per contare le occorrenze di ciascuna classe
    int[] classCounts = new int[5]; // Assumi numberOfClasses come il numero totale di classi possibili

    // Conta le occorrenze di ciascuna classe tra i k vicini
    for (Sample neighbor : kNearestNeighbors) {
        System.out.println("Classe scelta " + neighbor.cls);
        classCounts[neighbor.cls]++;
    }

    // Trova la classe con il conteggio più alto
    int maxCount = -1;
    int predictedClass = -1;
    for (int i = 0; i < classCounts.length; i++) {
        if (classCounts[i] > maxCount) {
            maxCount = classCounts[i];
            predictedClass = i;
        }
    }

    return predictedClass;
}

    public List<Sample> getTrainingData() {
        return trainingData;
    }

}


