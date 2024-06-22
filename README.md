# Autonomus Driving TORCS

Autonomus Driving TORCS è un progetto che esplora la guida autonoma utilizzando il simulatore TORCS (The Open Racing Car Simulator). Il progetto include un client che si connette al server TORCS e offre due modalità di guida:

## Obiettivo del Progetto

L'obiettivo principale è fornire un'esperienza interattiva ed educativa sulla guida autonoma, confrontando le prestazioni della guida autonoma con quella manuale.

## Caratteristiche

### Pagina Iniziale

All'avvio del programma, l'utente seleziona la modalità di guida preferita. Le opzioni disponibili sono:

- **Guida Autonoma**: Il classificatore KNN guida l'auto basandosi su dati di guida raccolti in precedenza e salvati nel file `output_normalized.csv`.
  
- **Guida Controllata**: Questa modalità consente all'utente di controllare direttamente l'auto all'interno del simulatore TORCS.

Il programma richiede all'utente di inserire il numero corrispondente alla modalità desiderata.

## Installazione

### Prerequisiti

- Java Development Kit 17 (JDK) installato.
- Simulatore TORCS installato e configurato.

### Passaggi per l'Installazione

1. **Scaricare il Repository**:
   - Clona o scarica il repository dal seguente link: [Autonomus-Driving-TORCS](https://github.com/d0x1t/Autonomous-driving-TORCS).

2. **Posizionare i File CSV**:
   - I file `output_normalized.csv` e `minMaxFile.csv` devono essere posizionati nella stessa directory del file JAR se si avvia l'applicazione da JAR. Se si esegue l'applicazione dai sorgenti, i file CSV devono essere nella sovradirectory delle cartelle `target` e `src`.

## Esecuzione dell'Applicazione

### Avvio dall'Esecuzione JAR

Per avviare l'applicazione dal file JAR:

  ```bash
java -jar NOME_DEL_FILE_JAR.jar
   ```


### Esecuzione dell'Applicazione

- File Sorgenti: Evitare di spostare i file sorgenti Java dal repository per evitare incoerenze con i package.
- Struttura dei File CSV: Assicurarsi che output_normalized.csv e minMaxFile.csv siano posizionati correttamente come descritto sopra per il corretto funzionamento dell'applicazione.
