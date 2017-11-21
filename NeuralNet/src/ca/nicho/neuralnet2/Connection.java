package ca.nicho.neuralnet2;

public class Connection extends Gene {

	public final Neuron from;
	public final NeuronHidden to;
	public double weight;
	public boolean enabled = true;
	
	public Connection(Neuron from, NeuronHidden to, long innovation, double weight){
		this.type = 0;
		this.from = from;
		this.to = to;
		this.innovation = innovation;
		this.weight = weight;
		to.inputs.add(this);
	}
	
	@Override
	public String toString(){
		return "C{i:" + innovation + ", w:" + weight + ", f:" + from + ",t: " + to + ", e:" + enabled + "}";
	}
	
}
