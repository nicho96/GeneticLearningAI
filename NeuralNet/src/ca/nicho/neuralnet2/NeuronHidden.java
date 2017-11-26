package ca.nicho.neuralnet2;

import java.util.ArrayList;
import java.util.TreeSet;

public class NeuronHidden extends Neuron {

	public ArrayList<Connection> inputs = new ArrayList<Connection>();
	
	public NeuronHidden(long innovation) {
		super(innovation);
		this.type = 2;
	}

	@Override
	protected double activate(int activationID) {
		double sum = 0;
		for(Connection c : inputs)
			sum += c.from.getValue(activationID) * c.weight;
		return Neuron.sigmoid(sum);
	}

	@Override
	public boolean hasInput(Neuron n, TreeSet<Long> visited){
		if(this == n){
			return true;
		}
		
		visited.add(this.innovation);
				
		for(Connection con : inputs){
			if(visited.contains(con.from.innovation))
				continue;
			if(con.from.hasInput(n, visited)){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isUnique(){
		for(int i = 0; i < inputs.size(); i++){
			for(int j = 0; j < inputs.size(); j++){
				if(inputs.get(i) == inputs.get(j) && i != j){
					return false;
				}
			}
		}
		return true;
	}
	
}
