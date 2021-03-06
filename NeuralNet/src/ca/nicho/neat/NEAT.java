package ca.nicho.neat;

import java.util.ArrayList;

import ca.nicho.neuralnet.NeuralNetwork;

public abstract class NEAT {
	
	protected int innovationCount = 0;
	protected int neuronCount = 0;
	
	protected ArrayList<NeuralNetwork> networks = new ArrayList<NeuralNetwork>();
	
	/**
	 * Default function called to handle the next generation of simulations.
	 * Can be overridden to accommodate for extended operations.
	 */
	public void nextGeneration(){
		
		//this.mutateGeneration();
		this.simulateGeneration();
		this.selection();		
		this.onGenerationEnd();
		this.crossoverGeneration();
		
	}
	
	/**
	 * Handles mutating the entire generation
	 */
	protected void mutateGeneration(){
		for(NeuralNetwork nn : this.networks){
			this.mutate(nn);
		}
	}
	
	/**
	 * Handles crossing over the entire generation
	 */
	protected abstract void crossoverGeneration();
	
	/**
	 * Handles simulating the generation
	 */
	protected abstract void simulateGeneration();
	
	/**
	 * Mutates a neural network.
	 * 
	 * Remember to use the innovationCount and neuronCount to identify changes to the network.
	 * New evolutions can be kept tracked of and seperated from old ones during crossover.
	 * @param nn The neural network being mutated
	 */
	protected abstract void mutate(NeuralNetwork nn);
	
	/**
	 * Combine two networks together to form a child
	 * @param p1 The first parent network
	 * @param p2 The second parent network
	 * @return the child network as a combination of both parents
	 */
	protected abstract NeuralNetwork crossover(NeuralNetwork p1, NeuralNetwork p2);
	
	/**
	 * Handles the selection of what networks will move on to the next generation.
	 * Should kill off the weaker species.
	 */
	protected abstract void selection();
	
	/**
	 * Optional function. Called by default at the end of a generation.
	 */
	protected void onGenerationEnd(){}
	
}
