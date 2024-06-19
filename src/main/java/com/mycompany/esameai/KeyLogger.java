package com.mycompany.esameai;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class KeyLogger implements NativeKeyListener {
    private String tastoPremuto = "";

    public void avviaCattura() {
        try {
             Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("Impossibile registrare NativeHook: " + ex.getMessage());
            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(this); // Aggiungi l'istanza corrente come key listener
    }
   public void stopCattura() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("Impossibile deregistrare NativeHook: " + ex.getMessage());
        }
        GlobalScreen.removeNativeKeyListener(this); // Rimuovi il listener della tastiera
    }
    @Override
    public synchronized void nativeKeyPressed(NativeKeyEvent e) {
        String tasto = NativeKeyEvent.getKeyText(e.getKeyCode());
        
        //   try {
          /*      FileWriter csvWriter = new FileWriter(CSV_FILE_PATH, true);
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                String formattedDateTime = now.format(formatter);
                csvWriter.append(formattedDateTime + ";");
                csvWriter.append(tasto + "\n");
                csvWriter.flush();
                csvWriter.close();
            */    
                // Imposta il tasto premuto
                System.out.println(tasto);
                tastoPremuto = tasto;
                
                // Notifica il thread principale in attesa
                this.notify();
           /* } catch (IOException ex) {
                ex.printStackTrace();
            }*/
        
    }

    public synchronized String getTastoPremuto() throws InterruptedException {
        // Attendi finché il tasto premuto non è stato impostato
        while (tastoPremuto.isEmpty()) {
            this.wait();
        }
        
        return tastoPremuto;
    }
    public void setTastoPremuto(String testo){
        this.tastoPremuto = testo;
    }

    // Gli altri metodi dell'interfaccia NativeKeyListener
    @Override
    public void nativeKeyTyped(NativeKeyEvent nke) {
    
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nke) { }
    
}