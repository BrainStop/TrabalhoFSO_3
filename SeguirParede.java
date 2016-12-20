import java.util.concurrent.Semaphore;


/**
 * O comportamento SEGUIR PAREDE é uma tarefa JAVA que quando ativa, faz com que
 * o robot ande paralelo á parede, sobre uma linha imaginária a uma distância
 * pré-definida, que se encontra do lado direito do robot.
 * 
 * @author Goncalo Oliveira
 * @author Miguel Marcal
 */
public class SeguirParede extends Thread {
	
	/**
	 *  Distância que o robot se deslocará
	 */
	private static final int DISTANCIA = 15;
	
	/**
	 *  Velocidae a que o robot se desloca
	 */
	private static final double VELOCIDADE = 0.02;
	
	/**
	 * Canal de comunicacao com o robot
	 */
	private final RobotLego robot;
	
	/**
	 * Tarefa que produz distancias com o sensor do robot
	 */
	private final ProdutorDistancias prodDist;

	/**
	 * Semaforo utilizado para ativar ou desactivar a tarefa
	 */
	private Semaphore desativar = new Semaphore(0);
	
	/**
	 * Variavel booleana usada para saber se é necessario desativar o processo
	 */
	private boolean desativarFlag;
	
	/**
	 *  Semaforo utilizado para bloquear ou desbloquear a tarefa
	 */
	private Semaphore bloquear = new Semaphore(0);

	/**
	 * Variavel booleana usada para saber se é necessario bloquear o processo
	 */
	private boolean bloquearFlag;
	
	/**
	 * Semaforo utilizado para a exclusao mutua do acesso ao robot
	 */
	private Semaphore exclusao;
	
	/**
	 * Semaforo utilizado para fazer os processos que chamem o metodo esperar
	 * fiquem á espera que este os liberte
	 */
	private Semaphore espera = new Semaphore(0);

	/**
	 * 
	 * 
	 * @param robot
	 * @param pd
	 * @param exclusao
	 */
	public SeguirParede(RobotLego robot, ProdutorDistancias pd,
			Semaphore exclusao){
		this.exclusao      = exclusao;
		this.robot         = robot;
		this.prodDist      = pd; 
		this.desativarFlag = true;
		this.bloquearFlag  = false;
	}

	/**
	 * Verifica se o processo é necessario ser bloqueado ou desativado
	 */
	private void esperarTrabalho(){
		System.out.println("SP: Esperar Trabalho");
		try {
			if(bloquearFlag) {
				bloquear.acquire();
				bloquearFlag = false;
			}
			if(desativarFlag) {
				desativar.acquire(desativar.availablePermits() + 1);
				desativarFlag = false;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * sub-comportamento faz com que o robo ande paralelo á parede que esteja a 
	 * usa direita 
	 */
	private void seguirParede(){
		System.out.println("SP: Seguir Parede");
		try {
			exclusao.acquire();
			int distI = prodDist.getDistancia(); //Distancia inicial
			robot.Reta(DISTANCIA);
			exclusao.release();

			Thread.sleep((long) (DISTANCIA / VELOCIDADE) * 2);

			exclusao.acquire();
			float distF           = prodDist.getDistancia(); //Distancia final
			float catOposto    = distF - distI;
			float catAdjacente = DISTANCIA;
			int angulo            = (int)(Math.atan(Math.abs(catOposto) /
					catAdjacente) * 57.3);

			if(catOposto < 0)
				robot.CurvarEsquerda(0, angulo);
			else
				robot.CurvarDireita(0, angulo);

			robot.Parar(false);
			exclusao.release();

			System.out.println("///////////////////////////////////"
					+ "dist_F: " + distF + "disf_I: " + distI + "n/"
					+ "catOposto: " + catOposto +  "n/"
					+ "catAdjacente: " + catAdjacente +  "n/"
					+ "angulo: " + angulo
					+ "/////////////////////////////////////");

			double distRot = (angulo*Math.PI)/180; //Distancia de rotacao
			Thread.sleep((long) (distRot / VELOCIDADE) * 2);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sub-comportamento que é executado depois do sub-comportamento seguir 
	 * parede e ajusta a distancia entre o robot e a parede
	 */
	public void ajustarDistancia() {
		System.out.println("SP: Ajustar Distancia");
		try {
			exclusao.acquire();
			int distParede   = prodDist.getDistancia();
			double catOposto = distParede - 50;
			double angulo    = 45;
			double distDesl  = catOposto / Math.sin(Math.abs(angulo));
			
			if(catOposto > 0) {
				robot.CurvarDireita(0, (int)angulo);
				robot.Reta(Math.abs((int)distDesl));
				robot.CurvarEsquerda(0, (int)angulo);
			} else {
				robot.CurvarEsquerda(0, (int)angulo);
				robot.Reta(Math.abs((int)distDesl));
				robot.CurvarDireita(0, (int)angulo);
			}
			
			robot.Parar(false);
			exclusao.release();

			System.out.println("///////////////////////////////////"
					+ "distParede" + distParede + "n/"
					+ "catOposto: " + catOposto +  "n/"
					+ "angulo: " + angulo +  "n/"
					+ "distDesl: " + distDesl 
					+ "/////////////////////////////////////");

			//TODO Verificar o valor
			double distRot = (45*Math.PI)/180; //Distancia de rotacao
			Thread.sleep((long) ((distDesl + distRot) / VELOCIDADE) * 2);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public void run(){
		while(true) {
			esperarTrabalho();
			seguirParede();
			esperarTrabalho();
			ajustarDistancia();
			espera.release();
		}
	}

	/**
	 * Ativa a tarefa
	 * Metodo utilizado pelos processo pai
	 */
	public void ativar() {
		System.out.println("SP: Ativar");
		desativar.release();
	}

	/**
	 * Desativa a tarefa
	 * Metodo utilizado pelo processo pai
	 */
	public void desativar() {
		System.out.println("SP: Desativar");
		desativarFlag = true;
	}

	/**
	 * Desbloqueia a tarefa
	 * Metodo utilizado pelos processos irmao que tenham maior prioridade
	 */
	public void desbloquear() {
		System.out.println("SP: Desbloquear");
		bloquear.release();
	}

	/**
	 * Bloqueia a tarefa
	 * Metodo utilizado pelos processos irmao que tenham maior prioridade
	 */
	public void bloquear() {
		System.out.println("SP: Bloquear");
		bloquearFlag = true;
	}
	
	/**
	 * Faz com que os processo que o chamarem fiquem á espera que ele o liberte
	 */
	public void esperar() {
		try {
			espera.acquire(espera.availablePermits() + 1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
