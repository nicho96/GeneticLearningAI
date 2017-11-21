package ca.nicho.neuralnet2;

public class NeuronIn extends Neuron {

	public double value;
	
	public NeuronIn(long innovation) {
		super(innovation);
		this.type = 1;
	}

	@Override
	protected double activate(int activationID) {
		return value;
	}
	
	@Override
	public boolean hasInput(Neuron n){
		return this == n;
	}
	
}
