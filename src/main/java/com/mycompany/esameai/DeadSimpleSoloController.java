package com.mycompany.esameai;

/**
 * @author Gruppo13
 * 
 * 
 */
public class DeadSimpleSoloController extends Controller {

	final double targetSpeed = 15;

	public Action control(SensorModel sensorModel, double[] actions_inviati,int tastoPremuto) {
		Action action = new Action();
		if (sensorModel.getSpeed() < targetSpeed) {
			action.accelerate = 1;
		}
		if (sensorModel.getAngleToTrackAxis() < 0) {
			action.steering = -0.1;
		} else {
			action.steering = 0.1;
		}
		action.gear = 1;
		return action;
	}
        public Action control(SensorModel sensorModel, double[] actions_inviati,String tastoPremuto) {
		Action action = new Action();
		if (sensorModel.getSpeed() < targetSpeed) {
			action.accelerate = 1;
		}
		if (sensorModel.getAngleToTrackAxis() < 0) {
			action.steering = -0.1;
		} else {
			action.steering = 0.1;
		}
		action.gear = 1;
		return action;
	}

	public void reset() {
		System.out.println("Restarting the race!");

	}

	public void shutdown() {
		System.out.println("Bye bye!");
	}
}
