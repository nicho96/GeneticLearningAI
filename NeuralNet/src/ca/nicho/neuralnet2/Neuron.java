package ca.nicho.neuralnet2;

import java.util.TreeSet;

public abstract class Neuron extends Gene {
	
	protected int activationID;
	public double cachedActivation;
	
	public int depth;
	
	public Neuron(long innovation){
		this.innovation = innovation;
	}
	
	public static double sigmoid(double x) {
		return (1/( 1 + Math.pow(Math.E,(-1*x))));
	}
	
	public double getValue(int activationID){
		if(this.activationID == activationID){
			return cachedActivation;
		}
		
		this.activationID = activationID;
		this.cachedActivation = this.activate(activationID);
		return this.cachedActivation;
	}
	
	protected abstract double activate(int activationID);
	
	@Override
	public String toString(){
		return "N{i:" + innovation + "}";
	}
	
	public abstract boolean hasInput(Neuron n, TreeSet<Long> visited);
	
}
