import java.util.Random;
import java.util.concurrent.Semaphore;


/**
 * O comportamento VAGUEAR é uma tarefa JAVA que quando ativa, faz com que o 
 * robot vagueie pelo espaço até outro qualquer processo mandar terminar a sua 
 * funcionalidade. A definição de vaguear no espaço consiste no robot andar em 
 * frente, curvar à direita, curvar à esquerda e parar de forma aleatória.
 *
 * @author Gonçalo Oliveira
 * @author Miguel Marçal
 */
public class Vaguear extends Thread {

	/**
	 * Tempo que o processo adormece, em milisegundos, na acao Parar
	 */
	private static final int TEMP_PARAR = 1000;
	
	/**
	 * Tempo de espera entre amostragens da flag bloqueante
	 */
	private static final int TEMP_AMST = 100;

	/**
	 * Velocidade estimada que o robot se desloca em milisegundos.
	 */
	private static final double VEL = 0.02;

	/**
	 * Ojecto utilizado para gerar as escolhas do robot aleatoriamente
	 */
	private final Random random; 

	/**
	 * Objeto utilizado para comunicar e controlar o robot
	 */
	private final RobotLego robot;

	/**
	 * Tempo estimado que o robo demora a realizar certa instrução
	 */
	private double td;

	/**
	 * Semaforo utilizado para bloquear a tarefa
	 */
	private Semaphore desativar = new Semaphore(0);
	
	private Semaphore bloquear = new Semaphore(0);
	
	private Semaphore exclusao;

	/**
	 * Flag usada para saber se é necessario bloquear o tarefa
	 */
	private boolean desativarFlag;
	
	private boolean bloquearFlag;

	/**
	 * Semaforo utilizado para fazer os processos que chamem o metodo esperar
	 * fiquem á espera que este os liberte
	 */
	private Semaphore espera = new Semaphore(0);

	/**
	 * Construtor inicia as variaveis necessárias para o funcionamento da 
	 * aplicação
	 */
	public Vaguear(RobotLego robot, Semaphore exclusao) {
		this.exclusao = exclusao;
		this.robot = robot;
		random = new Random();
		desativarFlag = true; // Flag instanciada a true de forma a ficar bloqueado
		bloquearFlag = false;
	}

	/**
	 * Metodo run que é chamado quando o processo é inicializado e chama o 
	 * metodo lerMensagem
	 */
	public void run() {
		esperarTrabalho();
		escolha();
	}

	/**
	 * Estado que testa a flag "desativar" e bloquea a tarefa se ela estiver 
	 * ativa
	 */
	private void esperarTrabalho() {
		System.out.println("V: Esperar Trabalho");
		if(bloquearFlag) {
			try {
				System.out.println("V: Bloquear semaforo");
				bloquear.acquire(); // Bloquear a thread
				System.out.println("V: Desbloquear semaforo");
				bloquearFlag = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(desativarFlag) {
			try {
				System.out.println("V: Desativar semaforo");
				desativar.acquire(desativar.availablePermits() + 1); // Bloquear a thread
				desativarFlag = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Estado que escolhe aleatoriamente uma acao que o robo irá realizar
	 */
	private void escolha() {
		System.out.println("V: Escolha");
		switch (Acao.getAcaoAleatoria()) {
		case frente:
			reta();
			break;
		case direita:
			curvarDir();
			break;
		case esquerda:
			curvarEsq();
			break;
		case parar:
			parar();
			break;
		default:
			System.out.println(
					"V: ERRO Nenhuma escolha válida foi selecionada");
			break;
		}
		esperarInstrucao();
	}

	/**
	 * Acao reta que faz com que o robot avance uma distancia aleatoria ente 
	 * 10 e 75 cm 
	 */
	private void reta() {
		double dist = random.nextInt(66) + 10;
		td = dist / VEL;
		
		try {
			exclusao.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Reta dist: "+dist+" td: "+td);
		robot.Reta((int) dist);
		robot.Parar(false);
		exclusao.release();
	}

	/**
	 * Acao que faz com que o robo curve para a direita com uma distancia e 
	 * curvatura aleatorias
	 */
	private void curvarDir() {
		double raio   = random.nextInt(41) + 10;
		double angulo = random.nextInt(91);
		double dist   = angulo * Math.PI / 180 * raio;
		td = dist / VEL;

		try {
			exclusao.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("CurvaD dist: "+dist+" raio: "+raio+
				" angulo: "+angulo+" td: "+td);
		robot.CurvarDireita((int)raio, (int)angulo);
		robot.Parar(false);
		exclusao.release();
	}

	/**
	 * Acao que faz com que o robo curve para a esquerda com uma distancia e 
	 * curvatura aleatorias
	 */
	private void curvarEsq() {
		double raio   = random.nextInt(41) + 10;
		double angulo = random.nextInt(91);
		double dist   = (angulo*Math.PI)/180 * raio;
		td = dist / VEL;
		
		try {
			exclusao.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("V: CurvaE dist: "+dist+" raio: "+raio+
				" angulo: "+angulo+" td: "+td+".");
		robot.CurvarEsquerda((int)raio, (int)angulo);
		robot.Parar(false);
		exclusao.release();
	}

	/**
	 * Acao que faz com que o robo fique parado um determinado tempo
	 */
	private void parar() {
		td = TEMP_PARAR;
		
		try {
			exclusao.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("V: Parar td: "+td+".");
		robot.Parar(false);
		exclusao.release();
	}

	/**
	 * Estado que obriga a tarefa a esperar que a instrucao tenha sido realizada
	 * antes de permitir serem enviados novas acoes
	 * Este metodo utiliza uma previsao do tempo que o robo demora a realizar a
	 * intrucao e por isso nao tem como saber com certeza se a tarefa ja foi
	 * realizada ou nao
	 * É uma espera activa porque de vai verificando a flag bloqueante.
	 */
	private void esperarInstrucao() {
		double timestamp = System.currentTimeMillis();
		
		System.out.println("V: Esperar");
		while(System.currentTimeMillis() - timestamp < td) {
			esperarTrabalho();
			try {
				Thread.sleep(TEMP_AMST);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		espera.release();
		escolha();
	}

	/**
	 * Ativa a tarefa
	 */
	public void ativar() {
		System.out.println("V: Ativar");
		desativar.release();
	}

	/**
	 * Bloqueia a tarefa
	 */
	public void desativar() {
		System.out.println("V: Desativar");
		desativarFlag = true;
	}
	
	/**
	 * Ativa a tarefa
	 */
	public void desbloquear() {
		System.out.println("V: Desbloquear");
		bloquear.release();
	}

	/**
	 * Bloqueia a tarefa
	 */
	public void bloquear() {
		System.out.println("V: Bloquear");
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
