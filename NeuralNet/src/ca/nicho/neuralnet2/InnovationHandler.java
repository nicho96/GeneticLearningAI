package ca.nicho.neuralnet2;

public class InnovationHandler {

	private long innovation;
	
	public long nextInnovation(NeuralNetwork2 nn){
		nn.maxInnovation = innovation + 1;
		return innovation++;
	}
		
	public long currentInnovation(){
		return innovation;
	}
	
	public void setInnovation(long innovation){
		this.innovation = innovation;
	}
	
}
