import java.util.concurrent.Semaphore;

/**
 * @author Gonçalo Oliveira
 * @author Miguel Marçal
 */
public class Evitar extends Thread{
		
	/**
	 *  Distância que o robot se deslocará
	 */
	private static final int DIST = -15;
	
	/**
	 * Velocidade estimada que o robot se desloca em milisegundos.
	 */
	private static final double VEL = 0.01;
	
	/**
	 * Raio que o robot utiliza ao curvar
	 */
	private static final int RAIO = 1;
	
	/**
	 * Angulo que o robot utiliza ao curvar
	 */
	private static final int ANGULO = 90;

	/**
	 * Port do sensor de embate
	 */
	private static final int PORT = Robot.S_2;
	
	/**
	 * Tempo de espera entre amostragens da flag bloqueante
	 */
	private static final int TEMP_AMST = 250;

	/**
	 * Objeto utilizado para comunicar e controlar o robot
	 */
	private final RobotLego robot;
	
	/**
	 * Semaforo utilizado para bloquear a class quando é invocado o metodo
	 *  desativar()
	 */
	private Semaphore desativar = new Semaphore(0);
	
	/**
	 * Variavel booleana usada para saber se é necessario bloquear o processo
	 */
	private boolean desativarFlag;
	
	/**
	 * Semaforo utilizado para a exclusao mutua do acesso ao robot
	 */
	private Semaphore exclusao;

	/**
	 * 
	 */
	private Vaguear vaguear;

	/**
	 * 
	 */
	private SeguirParede seguirParede;

	/**
	 * 
	 * @param robot
	 * @param vag
	 * @param sP
	 * @param exclusao
	 */
	public Evitar(RobotLego robot, Vaguear vag, SeguirParede sP,
			Semaphore exclusao) {
		this.robot = robot;
		this.exclusao = exclusao;
		this.vaguear = vag;
		this.seguirParede = sP;
		this.desativarFlag = true;
	}

	/**
	 * 
	 */
	public void run() {
		esperarTrabalho();
	}
	
	/**
	 * 
	 */
	private void esperarTrabalho() {
		System.out.println("E: Esperar Trabalho");
		try {
			if(desativarFlag) {
				desativar.acquire();
				desativarFlag = false;
			}
			Thread.sleep(TEMP_AMST);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		lerSensorEmbate();
	}
	
	/**
	 * 
	 */
	private void lerSensorEmbate() {
		try {
			exclusao.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int sensorE = robot.Sensor(PORT);
		exclusao.release();
		System.out.println("E: Ler Sensor Embate:" + sensorE);
		if (sensorE == 0) {
			esperarTrabalho();
		} else {
			evitar();
		}
	}
	
	/**
	 * 
	 */
	private void evitar() {
		System.out.println("E: Evitar");
		try {
			vaguear.bloquear();
			seguirParede.bloquear();
			exclusao.acquire();

			robot.Parar(true);
			robot.Reta(DIST);
			robot.CurvarEsquerda(RAIO, ANGULO);
			robot.Parar(false);
			
			exclusao.release();
			vaguear.desbloquear();
			seguirParede.desbloquear();
			
			double dist = (ANGULO*Math.PI)/180 * RAIO;
			long td = (long)((dist + Math.abs(DIST))/VEL);
			Thread.sleep(td);
		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		esperarTrabalho();
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
		robot.SetSensorTouch(PORT);
		exclusao.release();
		desativar.release();
	}
	
	/**
	 * 
	 */
	public void desativar() {
		desativar.drainPermits();
		desativarFlag = true;
	}
}
