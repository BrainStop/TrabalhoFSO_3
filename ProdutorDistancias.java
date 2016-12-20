import java.util.concurrent.Semaphore;

public class ProdutorDistancias extends Thread {
	
	private static final int SLEEP = 250;
	
	private static final int PORT = Robot.S_1;
	
	private RobotLego robot;
	
	private int distancia;

	private Semaphore desativar = new Semaphore(0);

	private boolean desativarFlag;
	
	private Semaphore exclusao;
	
	public ProdutorDistancias(RobotLego robot, Semaphore exclusao) {
		this.exclusao  = exclusao;
		this.robot     = robot;
		this.desativarFlag = true;
	}
	
	/**
	 * 
	 */
	public void ativar() {
		try {
			exclusao.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("setSensor Distancia");
		robot.SetSensorLowspeed(PORT);
		exclusao.release();
		desativar.release();
	}
	
	/**
	 * 
	 */
	public void desativar() {
		desativar.drainPermits();
		desativarFlag = false;
	}
	
	/**
	 * 
	 */
	private void getSensorDistance() {
		try {
			exclusao.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		distancia = robot.SensorUS(PORT);
		exclusao.release();
		System.out.println("P: Get Distancia: "+distancia);
		try {
			Thread.sleep(SLEEP);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public int getDistancia() {
		return distancia;
	}
	
	/**
	 * 
	 */
	public void run () {
		while(true) {
			if(desativarFlag)
				try {
					desativar.acquire();
					desativarFlag = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			getSensorDistance();
		}
	}
}