package ca.nicho.xor;

import java.util.Random;

import ca.nicho.neat.DefaultNEAT;
import ca.nicho.neat.SpeciationNEAT;
import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.neuralnet.Screen;

public class XORAI {
	
	public static void main(String[] s){
		
		NeuralNetwork origin = new NeuralNetwork(2, 2, 1);
		
		SpeciationNEAT neat = new SpeciationNEAT(origin, delegate);

		NeuralNetwork network = null;
		int generation = 0;
		while(generation < 50){
			generation++;
			System.out.println("==== Current Generation: " + generation + " ====");
			neat.nextGeneration();
			
			network = neat.getMaxNetwork();
			
			for(int i1 = 0; i1 < 2; i1++){
				for(int i2 = 0; i2 < 2; i2++){
					double value = i1 ^ i2;
					network.updateInputs(new double[]{i1, i2});
					network.updateLayers();
					System.out.println(i1 + " " + i2 + " {" + network.outputs[0].value +", " + network.outputs[1].value + "} " + value);
				}
			}
			
		}
		
		new Screen(network);
		
	}
	
	private static DefaultNEAT.SimulateDelegate delegate = (NeuralNetwork network) -> {
		double score = 0;
			
		for(int i1 = 0; i1 < 2; i1++){
			for(int i2 = 0; i2 < 2; i2++){
				int value = i1 ^ i2;
				network.updateInputs(new double[]{i1, i2});
				network.updateLayers();
				
				//Correct output, we want this to be as close to 1 as possible
				double o1 = network.outputs[value].value;
				
				//Incorrect output, we want this to be as close to 0 as possible
				double o2 = network.outputs[(value + 1) % 2].value;
				
				score += o1 - 2 * o2;
				
			}
		}
								
		network.score = (int)(score * 100);
	};

}
