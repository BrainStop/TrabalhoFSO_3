import java.util.Random;

/**
 * Enumerado utilizado para gerar as acoes que o comportametento vaguear 
 * pode realizar
 */
public enum Acao {
	frente, parar, esquerda, direita; 

	private static final Acao[] VALUES = values(); 
	private static final int SIZE = VALUES.length;
	private static final Random RANDOM = new Random();

	/**
	 * Metodo Utilizado para retornar uma acao aleatoria do enumerado Acao
	 * @return Acao
	 */
	public static Acao getAcaoAleatoria()  {
		return VALUES[RANDOM.nextInt(SIZE)];
	}
}