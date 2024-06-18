package BalanceCSV;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class BalanceCSV {
    public static void main(String[] args) {
        String inputFilePath = "datasetTORCS.csv";
        String outputFilePath = "output_balanced.csv";

        // Read the CSV file
        List<String[]> rows = new ArrayList<>();
        String header = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            header = reader.readLine(); // Read the header
            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(line.split(";"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Count occurrences of each class
        Map<String, List<String[]>> classMap = new HashMap<>();
        for (int i = 0; i <= 5; i++) {
            classMap.put(String.valueOf(i), new ArrayList<>());
        }

        for (String[] row : rows) {
            String cls = row[row.length - 1];
            classMap.getOrDefault(cls, new ArrayList<>()).add(row);
        }

        // Find the minimum number of occurrences among the classes present
        int minCount = classMap.values().stream()
                               .filter(list -> !list.isEmpty())
                               .mapToInt(List::size)
                               .min()
                               .orElse(0);

        // Collect balanced rows
        List<String[]> balancedRows = new ArrayList<>();
        for (List<String[]> classRows : classMap.values()) {
            if (!classRows.isEmpty()) {
                balancedRows.addAll(classRows.subList(0, Math.min(minCount, classRows.size())));
            }
        }

        // Write the balanced CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(header);
            writer.newLine();
            for (String[] row : balancedRows) {
                writer.write(String.join(";", row));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("CSV file balanced and written to " + outputFilePath);
    }
}
