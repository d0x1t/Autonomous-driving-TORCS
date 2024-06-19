/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BalanceCSV;
import com.mycompany.esameai.Client;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class NormalizeCSV{
    private static String datasetFile = "datasetTORCS.csv";
    private static String outputNormalizedFile = "output_normalized.csv";

private static String minMaxFile = "minMaxFile.csv";

    public static void normalizeFile(String fileInput, String fileOutput) {
        String inputFile = fileInput;
        String outputFile = fileOutput;

        List<String[]> data = new ArrayList<>();

        // Leggi i dati dal file CSV
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                data.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Trova i valori minimi e massimi per ogni colonna numerica (escludendo l'ultimo valore)
        int numColumns = data.get(0).length;
        double[] minValues = new double[numColumns - 1];
        double[] maxValues = new double[numColumns - 1];
        for (int i = 0; i < numColumns - 1; i++) {
            minValues[i] = Double.MAX_VALUE;
            maxValues[i] = Double.MIN_VALUE;
        }

        for (String[] parts : data) {
            for (int i = 0; i < numColumns - 1; i++) {
                double value = Double.parseDouble(parts[i].trim());
                if (value < minValues[i]) {
                    minValues[i] = value;
                }
                if (value > maxValues[i]) {
                    maxValues[i] = value;
                }
            }
        }

        // Normalizza i valori e scrivi nel file di output con la classe
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (String[] parts : data) {
                double[] normalizedValues = new double[numColumns - 1];
                for (int i = 0; i < numColumns - 1; i++) {
                    double value = Double.parseDouble(parts[i].trim());
                    normalizedValues[i] = (value - minValues[i]) / (maxValues[i] - minValues[i]);
                    writer.write(String.format(Locale.US,"%.6f", normalizedValues[i]));
                    writer.write(";");
                }
                // Aggiungi l'ultimo valore (classe) senza normalizzazione
                writer.write(parts[numColumns - 1].trim());
                writer.newLine();
            }
            System.out.println("Dati normalizzati scritti nel file: " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
         saveMinMaxValues(minValues, maxValues, minMaxFile);
    }
    
 public static void loadMinMaxValues() {
     // Controlla se il file minMaxFile esiste
        if (!new File(minMaxFile).exists()) {
            // Controlla se il file datasetTORCS.csv esiste
            if (new File(datasetFile).exists()) {
                // Normalizza il file datasetTORCS.csv
                normalizeFile(datasetFile, outputNormalizedFile);
            } else {
                // Stampa un messaggio di avviso
                System.out.println("Prima di usare la guida autonoma devi creare il dataset con la guida controllata.");
                return;  // Termina l'esecuzione se il dataset non esiste
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(minMaxFile))) {
            String line;
            List<Double> minValuesList = new ArrayList<>();
            List<Double> maxValuesList = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                minValuesList.add(Double.parseDouble(parts[0].trim()));
                maxValuesList.add(Double.parseDouble(parts[1].trim()));
            }
            // Inizializza gli array del client con la dimensione corretta
            Client.minValues = new double[minValuesList.size()];
            Client.maxValues = new double[maxValuesList.size()];
            for (int i = 0; i < minValuesList.size(); i++) {
                Client.minValues[i] = minValuesList.get(i);
                Client.maxValues[i] = maxValuesList.get(i);
            }
            System.out.println("Valori minimi e massimi caricati dal file: " + minMaxFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void saveMinMaxValues(double[] minValues, double[] maxValues, String fileName) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
        for (int i = 0; i < minValues.length; i++) {
            writer.write(String.format(Locale.US, "%.6f", minValues[i]) + ";" + String.format(Locale.US, "%.6f", maxValues[i]));
            writer.newLine();
        }
        System.out.println("Valori minimi e massimi salvati nel file: " + fileName);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
