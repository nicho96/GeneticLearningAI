package ca.nicho.xor;

import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.neuralnet.Screen;
import ca.nicho.neuralnet.SimulateDelegate;

public class XORAI {
	
	public static void main(String[] s){
		
		NeuralNetwork nn = NeuralNetwork.createFullNeuralNetwork(2, 3, 1, 1);
		
		new Screen(nn);
		
		double[][] inputs = {{1, 1}, {1, 0}, {0, 1}, {0, 0}};
		double[][] outputs = {{0}, {1}, {1}, {0}};
		
		for(int i = 0; i < 10; i++){
			
			int i1 = (int)(Math.random() * 2);
			int i2 = (int)(Math.random() * 2);
			int value = i1 ^ i2;
			nn.updateInputs(new double[]{i1, i2});
			nn.updateLayers();
			System.out.println(i1 + " " + i2 + " " + value + " : " + nn.outputs[0].value + " " + nn.generateSum());
			
			nn.train(inputs, outputs);
		}
		
	}
	
	private static SimulateDelegate delegate = (NeuralNetwork network) -> {
		double score = 0;
		
		for(int i = 0; i < 20; i++){
			int i1 = (int)(Math.random() * 2);
			int i2 = (int)(Math.random() * 2);
			int value = i1 ^ i2;
			network.updateInputs(new double[]{i1, i2});
			network.updateLayers();
			score += 1 - Math.abs(network.outputs[0].value - value);
		}
								
		network.score = (int)(score * 100);
	};

}