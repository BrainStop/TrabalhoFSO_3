import java.util.concurrent.Semaphore;

public class Gestor extends Thread {
	
	/**
	 * 
	 */
	private SeguirParede seguirParede;
	
	/**
	 * 
	 */
	private Vaguear vaguear;
	
	/**
	 * 
	 */
	private ProdutorDistancias prodDist;
	
	/**
	 * 
	 */
	private Semaphore desativar = new Semaphore(0);
	
	/**
	 * 
	 */
	private boolean desativarFlag;
	
	/**
	 * 
	 * @param pd
	 * @param vag
	 * @param sp
	 */
	public Gestor(ProdutorDistancias pd, Vaguear vag, SeguirParede sP) {
		this.prodDist = pd;
		this.vaguear = vag;
		this.seguirParede = sP;
		desativarFlag = true;
	}
	
	/**
	 * 
	 */
	private void esperarTrabalho() {
		System.out.println("G: Esperar Trabalho");
		if(desativarFlag) {
			vaguear.desativar();
			seguirParede.desativar();
			try {
				desativar.acquire();
				desativarFlag = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
	
	public void gerir() {
		System.out.println("G: Gerir");
		if(prodDist.getDistancia() < 100) {
			seguirParede.ativar();
			seguirParede.esperar();
			seguirParede.desativar();
		} else {
		 	seguirParede.desativar();
			vaguear.ativar();
			vaguear.esperar();
			vaguear.desativar();
		}
	}
	
	public void run() {
		while(true) {
			esperarTrabalho();
			gerir();
		}
	}
	
	public void  ativar() {
		System.out.println("G: Ativar");
		desativar.release();
	}
	
	public void desativar() {
		System.out.println("G: Desativar");
		desativarFlag = true;
	}

}
