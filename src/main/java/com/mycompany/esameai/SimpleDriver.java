/**
 * @author Gruppo13
 *
 */

package com.mycompany.esameai;


public class SimpleDriver extends Controller {

    /* Costanti di cambio marcia */
    final int[] gearUp = {40/*superato questo valore vai in seconda */, 120, 160, 200, 240, 300}; //la prima dura da 0 a 3000 rpm
    final int[] gearDown = {0, 35, 80, 150, 190, 230};


    /* Constanti */
    final int stuckTime = 25;
    final float stuckAngle = (float) 0.523598775; // PI/6

    /* Costanti di accelerazione e di frenata */
    final float maxSpeedInCurva = 50;
    final float maxSpeedPreCurva = 70;
    final float maxSpeedDistInCurva = 25;
    final float maxSpeedDistPreCurva = 70;

    final float sin5 = (float) 0.08716;
    final float cos5 = (float) 0.99619;

    /* Costanti di sterzata */
    final float steerLock = (float) 0.785398;
    final float steerSensitivityOffsetRettilineo = (float) 300.0;
    final float steerSensitivityOffsetCurve = (float) 100.0;
    final float wheelSensitivityCoeff = 1;

    /* Costanti del filtro ABS */
    final float wheelRadius[] = {(float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276};
    final float absSlip = (float) 2.0;
    final float absRange = (float) 3.0;
    final float absMinSpeed = (float) 3.0;

    /* Costanti da stringere */
    final float clutchMax = (float) 0.5;
    final float clutchDelta = (float) 0.05;
    final float clutchRange = (float) 0.82;
    final float clutchDeltaTime = (float) 0.02;
    final float clutchDeltaRaced = 100;
    final float clutchDec = (float) 0.01;
    final float clutchMaxModifier = (float) 1.3;
    final float clutchMaxTime = (float) 1.5;


    // current clutch
    private float clutch = 0;

    @Override
    public void reset() {
        System.out.println("Restarting the race!");

    }

    @Override
    public void shutdown() {
        System.out.println("Bye bye!");
    }

    private int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double speed = sensors.getSpeed();
        // se la marcia è 0 (N) o -1 (R) restituisce semplicemente 1 
        if (gear < 1) {
            return 1;
        }

        // controlla se il valore RPM dell'auto è maggiore di quello suggerito
        // per cambiare marcia rispetto a quella attuale
        if (gear < 6 && speed >= gearUp[gear - 1]) { //se sto in prima e supero i 3000 giri allora passo in seconda con gear + 1 
            return gear + 1;
        } else // controlla se il valore RPM dell'auto è inferiore a quello suggerito
        // per scalare la marcia rispetto a quella attuale
        if (gear > 1 && speed <= gearDown[gear - 1]) { //se sto in seconda e sono al di sotto dei 3000 allora passa in prima se sto in 
            return gear - 1;
        } else // otherwhise keep current gear
        {
            return gear;
        }
    }

    private float getSteer(SensorModel sensors) {
        /**
         * L'angolo di sterzata viene calcolato correggendo l'angolo effettivo
         * della vettura rispetto all'asse della pista [sensors.getAngle()] e
         * regolando la posizione della vettura rispetto al centro della pista
         * [sensors.getTrackPos()*0,5].
         */

        float targetAngle = (float) (sensors.getAngleToTrackAxis() - sensors.getTrackPosition() * 0.5);
        // ad alta velocità ridurre il comando di sterzata per evitare di perdere il controllo
        if (sensors.getSpeed() > steerSensitivityOffsetRettilineo) {
            return (float) (targetAngle
                    / (steerLock * (sensors.getSpeed() - steerSensitivityOffsetRettilineo) * wheelSensitivityCoeff));
        } else {
            return (targetAngle) / steerLock;
        }
    }
    


    /**
     * 
     * @param sensors I dati dei sensori
     * @param actions_precedente l'array delle precedenti azioni eseguite dal veicolo
     * @param tastoPremuto carattere fornito in input dall'utente che indica quale azione eseguire 
     * @return un oggetto Action contenente le azioni da eseguire: accelerazione, frenata, sterzata, cambio di marcia
     */
    @Override
    public Action control(SensorModel sensors, double[] actions_precedente, String tastoPremuto) {

        float sensorSensor = (float) sensors.getTrackEdgeSensors()[9];
        float sxSensor = (float) sensors.getTrackEdgeSensors()[8];
        float dxSensor = (float) sensors.getTrackEdgeSensors()[10];
        float minimoSxSensor = (float) sensors.getTrackEdgeSensors()[7];
        float minimoDxSensor = (float) sensors.getTrackEdgeSensors()[11];

        System.out.println("Velocita: " + sensors.getSpeed());
        System.out.println("Frontale: " + sensorSensor);
        System.out.println("Massimo Angolo: " + (sxSensor - dxSensor));
        System.out.println("Minimo Angolo: " + (minimoSxSensor - minimoDxSensor));
        System.out.println("Track Position: " + sensors.getTrackPosition());
        System.out.println("Angle to track: " + sensors.getAngleToTrackAxis());
        System.out.println("Speed laterale: " + sensors.getLateralSpeed());
        
        if (tastoPremuto.equals("W")) {
            double differenzaAssolutaAngoli = Math.abs(sxSensor - dxSensor);
          //  System.out.println("SONO NELLA W");
            Action action = new Action();
            //mi faccio aiutare con la sterzata solo quando sono nella pista 
            if (sensors.getTrackPosition() > -1 && sensors.getTrackPosition() < 1) {
                action.steering = this.getSteer(sensors);
                //Sto sterzando troppo perche l'auto non è in linea con la strada quindi la velocità
                //deve essere ridotta.
                if (Math.abs(action.steering) >= 0.70d) {
                    action.accelerate = 0.40d;
                }
            }
            action.clutch = this.clutching(sensors, clutch);
            action.gear = this.getGear(sensors);
            if (action.gear == 0) {
                action.gear = 1;
            }

            // Se ho un muro a 20metri posso avere una velocita di al massimo 40km/h perche sono in una curva STRETTA
            //Politica di base:  sono al centro della curva quindi al massimo posso andare a 40km/h
            if ((sensorSensor <= maxSpeedDistInCurva) && (sensors.getSpeed() > maxSpeedInCurva) && ((differenzaAssolutaAngoli >= 0 && differenzaAssolutaAngoli <= 3))) {

                action.accelerate = 0.0d;
                action.brake = getBrake(sensors, actions_precedente);

            } else {
                action.accelerate = 1.00d;
            }

            //Fra pochi istanti l'auto dovrà affrontare una curva PERICOLOSA o a destra o a sinistra.
            //Politica di base: ogni curva NON SEMPLICE la devo affrontare ad un massimo di 70km/h
            if (sensorSensor <= maxSpeedDistPreCurva && (differenzaAssolutaAngoli >= 4 && differenzaAssolutaAngoli <= 15)) {
                action.accelerate = 1.d;

                //Se sono qui allora sono in una curva larga che posso affrontare aumentando la velocita.
                if (Math.abs(minimoSxSensor - minimoDxSensor) > 18) {
                    return action;
                }

                if (sensors.getSpeed() > maxSpeedPreCurva) {
                    action.accelerate = 0.0d;
                    action.brake = getBrake(sensors, actions_precedente);

                }
            }

            return action;
        }
        if (tastoPremuto.equals("F")) {
            Action action = new Action();
            if (sensors.getSpeed() <= 85) {
                action.gear = getGear(sensors);
                return action;
            }

           // System.out.println("SONO NELLA F");

            action.accelerate = 0.0d;

            action.brake = getBrake(sensors, actions_precedente);

            action.gear = this.getGear(sensors);

            return action;
        }
        if (tastoPremuto.equals("S")) {
          //  System.out.println("SONO NELLA S");
            Action action = new Action();
            action.accelerate = 1.00d;
            action.brake = 0.0d;
            action.gear = -1;

            return action;
        }

        if (tastoPremuto.equals("D")) {
          //  System.out.println("SONO NELLA D");

            Action action = new Action();
            //Premo D per la prima volta
            if (actions_precedente[4] == 0) {
                action.tastoD = 1;
                //Sono al centro della curva. Sono sicuro che ho una velocita bassa grazie alla logica di W. 
                if (sensorSensor <= 25 && (Math.abs(sxSensor - dxSensor) >= 0 && Math.abs(sxSensor - dxSensor) <= 6) && sensors.getSpeed() > 15) {
                    action.accelerate = 0.0d;
                } else //Logica di base. Non sono al centro della curva oppure sono in presenza di curve larghe quindi posso
                //fare una sterzata piu ampia. 
                {
                    if (sensors.getSpeed() >= maxSpeedPreCurva) {
                        action.accelerate = 0.0d;
                    } else {
                        action.accelerate = 1.00d;
                    }
                }
                action.gear = this.getGear(sensors);
                action.steering = -0.05d;
                //Sto sterzando troppo rispetto alla curva. Sono pronto 
                //per uscire dalla curva.
                double angoloTangLong = sensors.getAngleToTrackAxis();
                if (angoloTangLong > +0.25f && angoloTangLong < +1) {
                    action.steering = 0.0d;
                    action.accelerate = 0.60d;
                }
                return action;
            } //Sto continuando a premere la D
            else {
                action.tastoD = 1;
                if (actions_precedente[0] <= 0.40d) {

                    action.accelerate = 0.40d;
                    //Sono al centro della curva. Pero nel caso di curve larghe non entro nell'if 
                    //anche perche posso fare di conseguenza la curva pià larga. 
                    if (sensorSensor <= 25 && (Math.abs(sxSensor - dxSensor) >= 0 && Math.abs(sxSensor - dxSensor) <= 6) && sensors.getSpeed() > 15) {
                        action.accelerate = 0.0d;
                    }

                } else //Logica di base. Se continuo a premere D diminuisco man mano la velocità fino ad arrivare a 0,40.
                {
                    action.accelerate = actions_precedente[0] - 0.03d;
                }
                //Logica di base. Se continuo a premere D aumento man mano la sterzata fino ad arrivare a 1.
                action.steering = actions_precedente[2] - 0.05d;
                //Sto quasi uscendo dalla curva iniziamo ad accellerare piano piano.
                if (action.steering <= -0.75d) {
                    action.accelerate = 0.40d;
                }
                action.gear = this.getGear(sensors);
                //Sto sterzando troppo rispetto alla curva. Sono pronto 
                //per uscire dalla curva.
                double angoloTangLong = sensors.getAngleToTrackAxis();
                if (angoloTangLong > +0.25f && angoloTangLong < +1) {
                    action.steering = 0.0d;
                    action.accelerate = 0.60d;
                }
                return action;
            }

        }
        if (tastoPremuto.equals("A")) {
          //  System.out.println("Sono nella A");
            Action action = new Action();
            //Premo A per la prima volta
            if (actions_precedente[3] == 0) {
                action.tastoA = 1;
                //Sono al centro della curva. Sono sicuro che ho una velocita bassa grazie alla logica di W. 
                if (sensorSensor <= 25 && (Math.abs(sxSensor - dxSensor) >= 0 && Math.abs(sxSensor - dxSensor) <= 6) && sensors.getSpeed() > 15) {
                    action.accelerate = 0.0d;
                } else //Logica di base. Non sono al centro della curva oppure sono in presenza di curve larghe quindi posso
                //fare una sterzata piu ampia.
                {
                    if (sensors.getSpeed() >= maxSpeedPreCurva) {
                        action.accelerate = 0.0d;
                    } else {
                        action.accelerate = 1.00d;
                    }
                }
                action.gear = this.getGear(sensors);
                action.steering = +0.05d;
                //Sto sterzando troppo rispetto alla curva. Sono pronto 
                //per uscire dalla curva.
                double angoloTangLong = sensors.getAngleToTrackAxis();
                if (angoloTangLong < -0.25f && angoloTangLong > -1) {
                    action.steering = 0.0d;
                    action.accelerate = 0.60d;
                }
                return action;
            } //Sto continuando a premere la A
            else {
                action.tastoA = 1;
                if (actions_precedente[0] <= 0.40d) {

                    action.accelerate = 0.40d;
                    //Sono al centro della curva. Pero nel caso di curve larghe non entro nell'if 
                    //anche perche posso fare di conseguenza la curva pià larga. 
                    if (sensorSensor <= 25 && (Math.abs(sxSensor - dxSensor) >= 0 && Math.abs(sxSensor - dxSensor) <= 6) && sensors.getSpeed() > 15) {
                        action.accelerate = 0.0d;
                    }

                } else //Logica di base. Se continuo a premere A diminuisco man mano la velocità fino ad arrivare a 0,40.
                {
                    action.accelerate = actions_precedente[0] - 0.03d;
                }
                //Logica di base. Se continuo a premere D aumento man mano la sterzata fino ad arrivare a 1.
                action.steering = actions_precedente[2] + 0.05d;
                //Sto quasi uscendo dalla curva iniziamo ad accellerare piano piano.
                if (action.steering >= +0.75d) {
                    action.accelerate = 0.40d;
                }
                //Sto sterzando troppo rispetto alla curva. Sono pronto 
                //per uscire dalla curva.
                double angoloTangLong = sensors.getAngleToTrackAxis();
                if (angoloTangLong < -0.25f && angoloTangLong > -1) {
                    action.steering = 0.0d;
                    action.accelerate = 0.60d;
                }
                action.gear = this.getGear(sensors);
                return action;
            }
        }

        Action action = new Action();
        action.gear = getGear(sensors);

        return action;
    }

    float clutching(SensorModel sensors, float clutch) {

        float maxClutch = clutchMax;

        // Controlla se la situazione attuale è l'inizio della gara
        if (sensors.getCurrentLapTime() < clutchDeltaTime
                && sensors.getDistanceRaced() < clutchDeltaRaced) {
            clutch = maxClutch;
        }

        // Regolare il valore attuale della frizione
        if (clutch > 0) {
            double delta = clutchDelta;
            if (sensors.getGear() < 2) {

                // Applicare un'uscita più forte della frizione quando la marcia è una e la corsa è appena iniziata.
                delta /= 2;
                maxClutch *= clutchMaxModifier;
                if (sensors.getCurrentLapTime() < clutchMaxTime) {
                    clutch = maxClutch;
                }
            }

            // Controllare che la frizione non sia più grande dei valori massimi
            clutch = Math.min(maxClutch, clutch);

            // Se la frizione non è al massimo valore, diminuisce abbastanza rapidamente
            if (clutch != maxClutch) {
                clutch -= delta;
                clutch = Math.max((float) 0.0, clutch);
            } // Se la frizione è al valore massimo, diminuirla molto lentamente.
            else {
                clutch -= clutchDec;
            }
        }
        return clutch;
    }

    float getBrake(SensorModel sensors, double[] action_precedenti) {
          
       if (sensors.getSpeed() <= 120) {
            if (action_precedenti[1] == 0.40f) {
                return 0.10f;
            } else {
                return 0.40f;
            }
        } else if (action_precedenti[1] == 0.80f) {
            return 0.30f;
        } else {
            return 0.80f;
        }

    }

    @Override
    public float[] initAngles() {

        float[] angles = new float[19];

        /*
		 * set angles as
		 * {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90}
         */
        for (int i = 0; i < 5; i++) {
            angles[i] = -90 + i * 15;
            angles[18 - i] = 90 - i * 15;
        }

        for (int i = 5; i < 9; i++) {
            angles[i] = -20 + (i - 5) * 5;
            angles[18 - i] = 20 - (i - 5) * 5;
        }
        angles[9] = 0;
        return angles;
    }

}
