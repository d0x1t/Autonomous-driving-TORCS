package BalanceCSV;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PrintBalancedCSV {
    public static void main(String[] args) {
        String filePath = "output_normalized.csv";

        // Read and print the balanced CSV file
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
