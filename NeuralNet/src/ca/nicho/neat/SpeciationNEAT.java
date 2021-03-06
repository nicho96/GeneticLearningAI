package ca.nicho.neat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import ca.nicho.neuralnet.Axon;
import ca.nicho.neuralnet.NeuralNetwork;

public class SpeciationNEAT extends DefaultNEAT {

	public static double[] W = {1, 1, 1};
	
	public double variationThreshold = 0.2;
	public int lastNumberSpecies = 0;
	
	public SpeciationNEAT(NeuralNetwork origin, SimulateDelegate simulateDelegate){
		super(origin, simulateDelegate);
	}
	
	@Override
	protected void selection(){
		
		Collections.sort(networks);
		Collections.reverse(networks);
		
		//Sepciation portion
		LinkedHashMap<NeuralNetwork, ArrayList<NeuralNetwork>> species = new LinkedHashMap<NeuralNetwork, ArrayList<NeuralNetwork>>();
		for(NeuralNetwork nn : networks){
			boolean added = false;
			for(NeuralNetwork net : species.keySet()){
				if(getVariation(nn, net) < variationThreshold){
					added = true;
					species.get(net).add(nn);
					break;
				}
			}
			//If no species are similar to the network, create a new one
			if(!added){
				ArrayList<NeuralNetwork> arr = new ArrayList<NeuralNetwork>();
				arr.add(nn);
				species.put(nn, arr);
			}
		}
		
		if(Math.abs(lastNumberSpecies - species.size()) < 2){
			variationThreshold -= 0.01;
		}else{
			variationThreshold += 0.01;
		}
		
		lastNumberSpecies = species.size();
		
		verbose("Number of species: " + species.size());
		
		ArrayList<NeuralNetwork> selected = new ArrayList<NeuralNetwork>(this.speciesCapacity);
		
		int index = 0;
		boolean running = true;
		while(running){
			for(ArrayList<NeuralNetwork> list : species.values()){
				if(index < list.size()){
					selected.add(list.get(index));
				}
				if(selected.size() == networks.size() || selected.size() == speciesCapacity){
					running = false;
					break;
				}
			}
			index++;
		}
		
		this.networks = selected;
		
		verbose("Number of networks: " + this.networks.size());
		
		Collections.sort(networks);
		Collections.reverse(networks);
		
	}
	
	public double getVariation(NeuralNetwork n1, NeuralNetwork n2){
		
		double N = Math.max(n1.axons.size(), n2.axons.size());
		
		double same = 0;
		double d_weight = 0;
		
		double disjoint = 0;
		double excess = 0;
		
		long n1_max = n1.axons.get(n1.axons.size() - 1).innovation;
		long n2_max = n2.axons.get(n2.axons.size() - 1).innovation;
		
		//Count the excess, disjoint and matching genes from n1's perspective
		for(Axon a1 : n1.axons){
			Axon a2 = n2.axonsMap.get(a1.innovation);
			if(a2 == null){
				if(a1.innovation > n2_max) excess += 1;
				else disjoint += 1;
			}else{
				same += 1;
				d_weight += Math.abs(a1.weight - a2.weight);
			}
		}
		
		//Add up remaining excess and disjoint genes from n2's perspective
		for(Axon a2: n1.axons){
			Axon a1 = n1.axonsMap.get(a2.innovation);
			if(a1 == null){
				if(a2.innovation > n1_max) excess += 1;
				else disjoint += 1;
			}
		}
		
		//Take linear combination
		double comb = W[0] * disjoint / N + W[1] * excess / N + W[2] * d_weight / same;
				
		return comb;
	}
	
}
