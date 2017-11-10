package ca.nicho.neuralnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import ca.nicho.neat.DefaultNEAT.SimulateDelegate;

public class Species implements Comparable<Species> {

	public static final int KILL_THRESHOLD = 4; //The amount of the population that dies after each generation (e.g 4 will kill off 3/4 of the population)
	public static final double DEVIATION_THRESHOLD = 0.2; //This threshold determines if the deviation from the parent is big enough to merit its own species
	public static final int STAGNATION_POINT = 5; //After this amount of generations without improvements, the species' score will suffer
	public static final double STAGNATION_THRESHOLD = 0.1; //How much the species suffers after the stagnation point is reached per generation (percentage)
	
	private Population population;
	private NeuralNetwork parent;

	public NeuralNetwork max;
	
	private int speciesCapacity;
	public ArrayList<NeuralNetwork> networks = new ArrayList<NeuralNetwork>();
		
	public int generation = 0;
	public int generationsSinceImprovement = 0;
	
	private Random random;
	
	private SimulateDelegate delegate;
	
	public Species(Population population, NeuralNetwork parent, int size, Random random, SimulateDelegate delegate){
		
		this.population = population;
		
		this.random = random;
		
		this.parent = parent;
		
		this.delegate = delegate;
				
		this.max = parent;
		this.speciesCapacity = size;
		
		networks.add(parent);
				
	}
	
	public void simulateParent(){
		if(parent.score == -1){
			simulateNetwork(parent);
		}
	}
	
	/**
	 * This will populate the network array with new slightly varied networks
	 */
	public void populateInitial(){
		while(networks.size() < speciesCapacity){
			NeuralNetwork clone = new NeuralNetwork(parent);
			networks.add(clone);
		}
	}
	
	public void nextGeneration(){
		
		mutateGenerationAndSimulate();
		seperateIntoSubSpecies();
		sortNetworks();
		killAndRepopulateWeakest();
		
		generation++;
						
		if(networks.get(0).score <= max.score){
			generationsSinceImprovement++;
		}else{
			generationsSinceImprovement = 0;
		}
		
		max = networks.get(0);
				
	}
		
	public void mutateGenerationAndSimulate(){
				
		for(int i = 1; i < networks.size(); i++){
			NeuralNetwork nn = networks.get(i);
				
			for(int j = 0; j < nn.axons.size() * 0.02 + 1; j++){
				while(!mutateNetwork(nn));
			}
			simulateNetwork(nn);
			
		}

	}
	
	/**
	 * Will cause all networks to simulate, and sort
	 */
	public void simulateGeneration(){
				
		for(NeuralNetwork nn : networks){
			this.simulateNetwork(nn);
		}
		
		sortNetworks();
		
	}
	
	public void sortNetworks(){
		Collections.sort(networks);
		Collections.reverse(networks);		
	}
	
	public void setCapacity(int capacity){
		this.speciesCapacity = capacity;
		killAndRepopulateWeakest();
	}
	
	public void seperateIntoSubSpecies(){
		
		ArrayList<NeuralNetwork> subSpecies = new ArrayList<NeuralNetwork>(speciesCapacity);
		for(NeuralNetwork nn : networks){
			double dev = population.getDeviation(parent, nn);
			if(dev > DEVIATION_THRESHOLD && parent.score < nn.score){
				subSpecies.add(nn);
			}
		}
		
		for(NeuralNetwork nn : subSpecies){
			networks.remove(nn);
			population.createNewSpecies(nn);
		}
		
	}
	
	public void killAndRepopulateWeakest(){
		
		ArrayList<NeuralNetwork> nextGeneration = new ArrayList<NeuralNetwork>(speciesCapacity);
		
		int rem = ((speciesCapacity <= networks.size()) ? speciesCapacity : networks.size()) / KILL_THRESHOLD;
		double a = (rem + 1) / Math.E;
		
		//Add the fittest networks to the next generation list, if they are not viable to be a new species
		for(int i = 0; i < rem && i < speciesCapacity && i < networks.size(); i++){
			
			NeuralNetwork nn = networks.get(i);
			nextGeneration.add(nn);
			
		}
		
		for(int i = rem; i < speciesCapacity && i < networks.size(); i++){
			int r1 = rem - (int)(Math.exp(random.nextDouble()) * a);
			int r2 = rem - (int)(Math.exp(random.nextDouble()) * a);
			while(r2 == r1){
				r2 = rem - (int)(Math.exp(random.nextDouble()) * a);
			}
			
			nextGeneration.add(population.breedNetwork(networks.get(r1), networks.get(r2)));
		}
		
		this.networks = nextGeneration;
		
	}
	
	//This method must be overriden
	private void simulateNetwork(NeuralNetwork network){
		delegate.simulateNetwork(network);
	}
	
	private boolean mutateNetwork(NeuralNetwork nn){
		double r = random.nextDouble();
		
		if(r < 0.25){
			return this.randomNeuronConnection(nn);
		}else if(r < 0.50){
			return this.randomAxonWeightChange(nn);
		}else if(r < 0.75){
			return this.splitRandomConnection(nn);
		}else{
			return this.randomAxonToggle(nn);
		}
			
	}
	
	public boolean splitRandomConnection(NeuralNetwork nn){
		
		//Only axons that are separated by two or more layers should be considered.
		ArrayList<Axon> possibilities = new ArrayList<Axon>();
		for(Axon a : nn.axons){
			if(a.output.layer.index - a.input.layer.index > 1 && a.enabled){
				possibilities.add(a);
			}
		}
		
		if(possibilities.size() == 0){
			return false; //No possibilities
		}
		
		
		Axon a = possibilities.get(random.nextInt(possibilities.size()));
		
		//Disable this connection, as it's about to be split. We do not remove it however, as it may mutate later.
		a.enabled = false; 
				
		//Create neuron directly in the layer between both (e.g input->layer 2 output->layer 6 will place the neuron in (6-2) / 2 + 2 = 4 after integer division)
		Neuron neuron = nn.createNeuron(nn.layers.get((a.output.layer.index - a.input.layer.index) / 2 + a.input.layer.index), population.neuronCount++);
		
		//The connection from the input starts off with weight 1 (this actually keeps the functionality of the network identical, until there is a mutation).
		nn.connectNeurons(a.input, neuron, 1, population.innovationCount++);
				
		//The connection to the output maintains the weight of the original connection
		nn.connectNeurons(neuron, a.output, a.weight, population.innovationCount++);
				
		return true;
		
	}
	
	public boolean randomAxonToggle(NeuralNetwork nn){
		if(nn.axons.size() == 0)
			return false;
		Axon a = nn.axons.get(random.nextInt(nn.axons.size()));
		a.enabled = !a.enabled;
		return true;
	}

	public boolean randomNeuronConnection(NeuralNetwork nn){	
		
		//Get the non-empty layers from the array
		ArrayList<Layer> possibilities = new ArrayList<Layer>();
		for(Layer l : nn.layers){
			if(l.neurons.size() > 0){
				possibilities.add(l);
			}
		}
		
		//There are not two layers available to create a random neuron connection
		if(possibilities.size() < 2){
			return false;
		}
		
		//Get a split index
		int splitIndex = random.nextInt(possibilities.size() - 1);
		
		//Get neurons before the split
		ArrayList<Neuron> left = new ArrayList<Neuron>();
		for(int i = 0; i <= splitIndex; i++){
			for(Neuron n : possibilities.get(i).neurons){
				left.add(n);
			}
		}
		
		//Get neurons after the split
		ArrayList<Neuron> right = new ArrayList<Neuron>();
		for(int i = splitIndex + 1; i < possibilities.size(); i++){
			for(Neuron n : possibilities.get(i).neurons){
				right.add(n);
			}
		}
		
		Neuron n1 = left.get(random.nextInt(left.size()));
		Neuron n2 = right.get(random.nextInt(right.size()));
		nn.connectNeurons(n1, n2, random.nextDouble() * 2 - 1, population.innovationCount++);
		
		return true;
		
	}
	
	public boolean randomAxonWeightChange(NeuralNetwork nn){
		if(nn.axons.size() == 0) //No axons to change
			return false;
		Axon a = nn.axons.get(random.nextInt(nn.axons.size()));
		a.weight = random.nextDouble() * 2 - 1;
		return true;
	}
	
	
	/**
	 * Populates the input array
	 */
	public void prepareInputs(NeuralNetwork nn){
		for(int i = 0; i < nn.inputs.length; i++){
			nn.inputs[i] = nn.createNeuron(nn.layers.get(0), population.neuronCount++);
		}
	}
	
	/**
	 * This function should only be called after all layers have been created. It will add the output neurons to the network.
	 */
	public void prepareOutputs(NeuralNetwork nn){
		for(int i = 0; i < nn.outputs.length; i++){
			nn.outputs[i] = nn.createNeuron(nn.outputLayer, population.neuronCount++);
		}
	}
	
	public double getSpeciesScore(){
		double det = (generationsSinceImprovement > STAGNATION_POINT) ? STAGNATION_THRESHOLD * (generationsSinceImprovement - STAGNATION_POINT) : 0;
		return max.score - det;
	}
	
	@Override
	public int compareTo(Species other){
		double s1 = getSpeciesScore();
		double s2 = other.getSpeciesScore();
		if(s1 < s2){
			return -1;
		}else if(s1 > s2){
			return 1;
		}else{
			return 0;
		}
	}
	
	
}