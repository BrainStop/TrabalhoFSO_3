import java.util.Random;

public class MyRobotLego {
	
	public MyRobotLego() {
	}
	
	public void CloseNXT() {
	}
	
	public void AjustarVMD(int d) {
		// TODO Auto-generated method stub

	}
	
	public void AjustarVME(int d) {
		// TODO Auto-generated method stub

	}

	public boolean OpenNXT(String nomeRobo) {
		// TODO Auto-generated method stub
		return true;
	}

	public void SetSensorTouch(int s2) {
		// TODO Auto-generated method stub
		
	}

	public void CurvarDireita(int raio, int angulo) {
		// TODO Auto-generated method stub
		
	}

	public void CurvarEsquerda(int raio, int angulo) {
		// TODO Auto-generated method stub
		
	}

	public void Parar(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public int Sensor(int s2) {
		// TODO Auto-generated method stub´
		Random r = new Random();
		int num = r.nextInt(4);
		if(num < 3)
			return 0;
		return 1;
	}

	public void reta(int dist) {
		// TODO Auto-generated method stub
		
	}

	public void Reta(int dist) {
		// TODO Auto-generated method stub
		
	}
}
