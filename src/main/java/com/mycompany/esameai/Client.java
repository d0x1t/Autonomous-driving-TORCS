/**
 *
 */
package com.mycompany.esameai;

import knn2.*;

import com.mycompany.esameai.Controller.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import BalanceCSV.*;
/**
 * @author Gruppo13
 *
 */
public class Client {

    private static final int UDP_TIMEOUT = 10000;
    private static final int port = 3001;
    private static final String host = "localhost";
    private static final String clientId = "SCR";
    private static final boolean verbose = false;
    private static final Stage stage = Stage.UNKNOWN;
    private static final String trackName = "unknow";

    private static BlockingQueue<String> csvQueue = new LinkedBlockingQueue<>();
    private static volatile boolean running = true;
    private static BlockingQueue<String> packetQueue = new LinkedBlockingQueue<>();
    private static SocketHandler mySocket = new SocketHandler(host, port, verbose);
    private static String datasetFile = "datasetTORCS.csv";
    private static String outputNormalizedFile = "output_normalized.csv";
    private static Scanner scanner = new Scanner(System.in);
    public static double[] minValues;
    public static double[] maxValues;
    private static boolean fileNormalizedNotExist = false;

    /**
     * @param args viene utilizzato per definire tutte le opzioni del client. -
     * port:N viene utilizzato per specificare la porta per la connessione (il
     * valore predefinito è 3001). - host:INDIRIZZO viene utilizzato per
     * specificare l'indirizzo dell'host dove il server è in esecuzione (il
     * valore predefinito è localhost). - id:ClientID viene utilizzato per
     * specificare l'ID del client inviato al server (il valore predefinito è
     * championship2009). - verbose:on viene utilizzato per attivare la modalità
     * verbose (il valore predefinito è spento). - maxEpisodes:N viene
     * utilizzato per impostare il numero di episodi (il valore predefinito è
     * 1). - maxSteps:N viene utilizzato per impostare il numero massimo di
     * passaggi per ogni episodio (il valore predefinito è 0, che significa
     * numero illimitato di passaggi). - stage:N viene utilizzato per impostare
     * lo stadio corrente: 0 è WARMUP, 1 è QUALIFYING, 2 è RACE, altri valori
     * significano UNKNOWN (il valore predefinito è UNKNOWN). - trackName:nome
     * viene utilizzato per impostare il nome della pista attuale.
     */
    public static void main(String[] args) {

        String input = "";

        do {
            System.out.println("Inserisci:\n0: Guida autonoma\n1: Guida controllata");
            input = scanner.nextLine().trim(); // Leggi l'input e rimuovi spazi bianchi

            // Verifica se l'input è "0" o "1"
            if (!input.equals("0") && !input.equals("1")) {
                System.out.println("Input non valido. Riprova.");
            }

        } while (!input.equals("0") && !input.equals("1"));
        if (input.equals("0")) {
            guidaAutonoma();
        } else {
            guidaControllata();
        }

    }

    public static void guidaAutonoma() {
        File fileOutput = new File(datasetFile);
        int k = 5;
        // Costruisco il mio classificatore a partire dal nome del file dei prototipi
        
        boolean fileOutputExists = fileOutput.exists();
        if (!fileOutputExists) {
            String response = "";
            do {
                System.out.println("Non hai ancora creato un dataset per la guida autonoma.\n"
                        + "Vuoi passare alla guida controllata? [y,n]");
                response = scanner.nextLine().trim().toLowerCase();

                if (response.isEmpty()) {
                    response = "y";
                }

                if (!response.equals("y") && !response.equals("n")) {
                    System.out.println("Input non valido. Per favore, inserisci 'y' o 'n'.");
                }

            } while (!response.equals("y") && !response.equals("n"));

            if (response.equals("y")) {
                guidaControllata();
            } else {
                System.out.println("Sto terminando");
                return;
            }
        } else if (checkUltimaNormalizzazione(datasetFile, outputNormalizedFile)) {
            if(!fileNormalizedNotExist){
                NormalizeCSV.normalizeFile(datasetFile, outputNormalizedFile);
            }else{
            System.out.println("È stata rilevata una versione del training set aggiornata. Vuoi aggiornare la normalizzazione? [y,n]");
            String response = "";
            do {
                response = scanner.nextLine().trim().toLowerCase();

                if (response.isEmpty()) {
                    response = "y";
                }
                if (!response.equals("y") && !response.equals("n")) {
                    System.out.println("Input non valido. Per favore, inserisci 'y' o 'n'.");
                }

            } while (!response.equals("y") && !response.equals("n"));

            if (response.equals("y")) {
                NormalizeCSV.normalizeFile(datasetFile, outputNormalizedFile);
                System.out.println("Normalizzazione terminata");
            }
            }

        }
        scanner.close();

        System.out.println("Sto esaminando il dataset " + outputNormalizedFile + "...");
        NearestNeighbor knn = new NearestNeighbor(outputNormalizedFile);
        NormalizeCSV.loadMinMaxValues();
        System.out.println("KNN Pronto");

        mySocket = new SocketHandler(host, port, verbose);
        String inMsg;
        Controller driver = new SimpleDriver();
        driver.setStage(stage);
        driver.setTrackName(trackName);

        /* Build init string */
        float[] angles = driver.initAngles();
        String initStr = clientId + "(init";
        for (int i = 0; i < angles.length; i++) {
            initStr = initStr + " " + angles[i];
        }
        initStr = initStr + ")";
        System.out.println("In attesa di una risposta dal server...");
        do {

            mySocket.send(initStr);
            inMsg = mySocket.receive(1000);

        } while (inMsg == null || inMsg.indexOf("***identified***") < 0);
        System.out.println("Ready!");
        PacketThread packetThread = new PacketThread();
        Thread thread = new Thread(packetThread);
        thread.start();
        

        double[] actions_inviati = {-3, -3, -3, -3, -3};

        while (true) {
            inMsg = packetQueue.poll();
            packetQueue.clear();

            if (inMsg != null) {
                if (inMsg.indexOf("***shutdown***") >= 0) {
                    driver.shutdown();
                    if (verbose) {
                        System.out.println("Server restarting!");
                    }
                    break;
                }
                if (inMsg.indexOf("***restart***") >= 0) {
                    driver.reset();
                    if (verbose) {
                        System.out.println("Server restarting!");
                    }
                    break;
                }

                SensorModel sensors = new MessageBasedSensorModel(inMsg);
                Action action = new Action();

                double sensorSensor = sensors.getTrackEdgeSensors()[9];
                double sxSensor = sensors.getTrackEdgeSensors()[8];
                double dxSensor = sensors.getTrackEdgeSensors()[10];
                double minimoSxSensor = sensors.getTrackEdgeSensors()[7];
                double minimoDxSensor = sensors.getTrackEdgeSensors()[11];

                double[] inputVector = {sensorSensor, sxSensor - dxSensor, minimoSxSensor - minimoDxSensor, sensors.getTrackPosition(), sensors.getAngleToTrackAxis()};
                double[] normalizedVector = normalizeInputVector(inputVector, minValues, maxValues);
                Sample testPoint = new Sample(normalizedVector[0], normalizedVector[1], normalizedVector[2], normalizedVector[3], normalizedVector[4]);
                //Sample testPoint = new Sample(sensorSensor, sxSensor - dxSensor, minimoSxSensor - minimoDxSensor, sensors.getTrackPosition(), sensors.getAngleToTrackAxis());
                int predictedClass = knn.classify(testPoint, k);

                action = driver.control(sensors, actions_inviati, predictedClass);

                System.out.println("Classe predetta: " + predictedClass);
                mySocket.send(action.toString());

                actions_inviati[0] = action.accelerate;
                actions_inviati[1] = action.brake;
                actions_inviati[2] = action.steering;
                actions_inviati[3] = action.tastoA;
                actions_inviati[4] = action.tastoD;

            }
        }

        /*
		 * Shutdown the controller
         */
        driver.shutdown();
        mySocket.close();
        System.out.println("Client shutdown.");
        System.out.println("Bye, bye!");
    }

    public static void guidaControllata() {

        String response = "";
        File file = new File(datasetFile);
        boolean fileExists = file.exists();
        if (fileExists) {
            // Chiede all'utente se vuole sovrascrivere il file esistente
            do {
                System.out.println("Il file " + datasetFile + " esiste già. Vuoi sovrascriverlo? [y,n]");
                response = scanner.nextLine().trim().toLowerCase();
                if (response.isEmpty()) {
                    response = "y";
                }
            } while (!response.equals("y") && !response.equals("n"));

            if (response.equals("y")) {
                fileExists = !fileExists;
                boolean isDeleted = file.delete();
                if (!isDeleted) {
                    System.out.println("Non è stato possibile eliminare il file esistente.");
                    return;
                }

            }
        }
        scanner.close();
        String inMsg;
        try (BufferedWriter bfw = new BufferedWriter(new FileWriter(datasetFile, true))) {
            if (!fileExists) {
                bfw.append("SensoreFrontale;DifferenzaSxDxSensor;DifferenzaSxMinDxMinSensor;PosizioneRispettoAlCentro;AngoloLongTang;Cls");
                bfw.newLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Controller driver = new SimpleDriver();
        driver.setStage(stage);
        driver.setTrackName(trackName);

        float[] angles = driver.initAngles();
        String initStr = clientId + "(init";
        for (int i = 0; i < angles.length; i++) {
            initStr = initStr + " " + angles[i];
        }
        initStr = initStr + ")";
        System.out.println("In attesa di una risposta dal server...");
        do {
            mySocket.send(initStr);
            inMsg = mySocket.receive(1000);
        } while (inMsg == null || inMsg.indexOf("***identified***") < 0);
        System.out.println("Ready"
                + "");
        PacketThread packetThread = new PacketThread();
        Thread thread0 = new Thread(packetThread);
        thread0.start();
        KeyLogger keyLogger = new KeyLogger();
        keyLogger.avviaCattura();
        CSVThread csvThread = new CSVThread();
        Thread thread1 = new Thread(csvThread);
        thread1.start();

        double[] actions_inviati = {-3, -3, -3, -3, -3};

        long lastAppendTimeMillis = 0;

        while (true) {
            long currentTimeMillis = System.currentTimeMillis();
            String tastoPremuto = "";
            inMsg = packetQueue.poll();
            packetQueue.clear();

            if (inMsg != null) {
                if (inMsg.indexOf("***shutdown***") >= 0) {
                    driver.shutdown();
                    if (verbose) {
                        System.out.println("Server restarting!");
                    }
                    break;
                }

                if (inMsg.indexOf("***restart***") >= 0) {
                    driver.reset();
                    if (verbose) {
                        System.out.println("Server restarting!");
                    }
                    break;
                }

                SensorModel sensors = new MessageBasedSensorModel(inMsg);
                Action action = new Action();
                try {
                    tastoPremuto = keyLogger.getTastoPremuto();
                    action = driver.control(sensors, actions_inviati, tastoPremuto);
                    if (currentTimeMillis - lastAppendTimeMillis >= 20 /*&& ((tastoPremuto == "U") || (tastoPremuto == "H") || tastoPremuto == "J" || tastoPremuto == "K" || tastoPremuto == "L")*/) {

                        lastAppendTimeMillis = currentTimeMillis; // Aggiorna l'ultimo timestamp
                        appendToQueue(sensors, tastoPremuto, csvQueue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                actions_inviati[0] = action.accelerate;
                actions_inviati[1] = action.brake;
                actions_inviati[2] = action.steering;
                actions_inviati[3] = action.tastoA;
                actions_inviati[4] = action.tastoD;
                mySocket.send(action.toString());

            }
        }
        driver.shutdown();
        mySocket.close();
        System.out.println("Client shutdown.");
        System.out.println("Bye, bye!");
    }

    public static void appendToQueue(SensorModel sensors, String tastoPremuto, BlockingQueue<String> csvQueue) {
        System.out.println("Stai registrando!");

        int cls = -1;
      try {
              switch (tastoPremuto) {
                case "W":
                    cls = 0;
                    break;
                case "A":
                    cls = 1;
                    break;
                case "S":
                    cls = 2;
                    break;
                case "D":
                    cls = 3;
                    break;
                case "F":
                    cls = 4;
                    break;
                default:
                    cls = 0;
                    break;
            }
         /*   switch (tastoPremuto) {
                case "U":
                    cls = 0;
                    break;
                case "H":
                    cls = 1;
                    break;
                case "J":
                    cls = 2;
                    break;
                case "K":
                    cls = 3;
                    break;
                case "L":

                    cls = 4;
                    break;
                default:
                    cls = 0;
                    break;
            }*/
            StringBuilder data = new StringBuilder();
            double sensorSensor = sensors.getTrackEdgeSensors()[9];
            double sxSensor = sensors.getTrackEdgeSensors()[8];
            double dxSensor = sensors.getTrackEdgeSensors()[10];
            double minimoSxSensor = sensors.getTrackEdgeSensors()[7];
            double minimoDxSensor = sensors.getTrackEdgeSensors()[11];
            double differenzaSxDxSensor = sxSensor - dxSensor;
            double differenzaSxMinDxMinSensor = minimoSxSensor - minimoDxSensor;
           // data.append(Double.toString(sensors.getSpeed())).append(";");
            data.append(Double.toString(sensorSensor)).append(";");
            data.append(Double.toString(differenzaSxDxSensor)).append(";");
            data.append(Double.toString(differenzaSxMinDxMinSensor)).append(";");
            data.append(Double.toString(sensors.getTrackPosition())).append(";");
            data.append(Double.toString(sensors.getAngleToTrackAxis())).append(";");

            data.append(Integer.toString(cls)).append("\n");

            // Inserisci i dati nella coda
            csvQueue.put(data.toString());

        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static class CSVThread implements Runnable {

        @Override
        public void run() {
            try (BufferedWriter bfw = new BufferedWriter(new FileWriter(datasetFile, true))) {
                while (running) {
                    String data = null;
                    try {
                        data = csvQueue.take();

                        bfw.append(data);
                        bfw.flush();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class PacketThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    String inMsg = mySocket.receive(UDP_TIMEOUT);
                    if (inMsg != null) {
                        packetQueue.offer(inMsg); // Mette il pacchetto nella coda
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static double[] normalizeInputVector(double[] inputVector, double[] minValues, double[] maxValues) {
        if (inputVector.length != minValues.length) {
            throw new IllegalArgumentException("La lunghezza del vettore di input deve corrispondere ai valori minimi e massimi.");
        }

        double[] normalizedVector = new double[inputVector.length];
        for (int i = 0; i < inputVector.length; i++) {
            normalizedVector[i] = (inputVector[i] - minValues[i]) / (maxValues[i] - minValues[i]);
        }

        return normalizedVector;
    }

    private static boolean checkUltimaNormalizzazione(String inputFile, String outputFile) {
        File input = new File(inputFile);
        File output = new File(outputFile);

        if (!input.exists()) {
            System.out.println("Il file di input non esiste.");
            return false;
        }

        if (!output.exists()) {
            fileNormalizedNotExist = false;
            // Se il file di output non esiste, dobbiamo eseguire la normalizzazione
            return true;
        }

        long inputLastModified = input.lastModified();
        long outputLastModified = output.lastModified();

        return inputLastModified > outputLastModified;
    }
}
