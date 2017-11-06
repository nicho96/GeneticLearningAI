package ca.nicho.neuralnet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Population {

	public static final File DIR_PATH = new File("networks/");
	
	public static final int SCORE_CHANGE_THRESHOLD = 10;
	public static final int MINIMUM_SPECIES_SIZE = 10;
	public static final double DEVIATION_THRESHOLD = 0;
	
	public long innovationCount;
	public long neuronCount;
	
	public double capacityFactor;
	
	public int speciesCapacity = 40; //This will set a limit to how many species can exist before cross-breeding and killing occures
	public int ecosystemCapacity; //This will account for all neural networks, and sets a maximum.
	
	public ArrayList<Species> species = new ArrayList<Species>();
	public ArrayList<Species> newSpecies = new ArrayList<Species>();
	
	private Random random = new Random();
	
	private SimulateDelegate simulateDelegate;
	
	public Population(){
		
	}
	
	public Population(int capacity, NeuralNetwork net, SimulateDelegate delegate){
		
		this.simulateDelegate = delegate;
		
		Species origin = new Species(this, net, 50, random, simulateDelegate);
		
		//Prepare the first species
		origin.prepareInputs(net);
		origin.prepareOutputs(net);
		origin.simulateParent();
		origin.populateInitial();
	
		species.add(origin);
		
		for(int i = 0; i < capacity / 10; i++){
			this.createNewSpecies(net);
		}
		
		int sum = MINIMUM_SPECIES_SIZE;
		for(int i = 0; i < speciesCapacity; i++){
			sum += i * i;
		}
		capacityFactor = ecosystemCapacity;
		
		
	}
	
	public void simulateGeneration(){
					
		//Will accomodate for the species that were bred
		addNewSpeciesToPool();
		
		for(Species s : species){
			s.populateInitial();
			s.nextGeneration();
			System.out.print("*");
		}
		System.out.println();
		
		addNewSpeciesToPool();
		sortSpeciesByFittest();
		curateList();
		
		if(species.size() > 4){
			breedWithinList();
		}
		
		species.get(0).max.save(new File(DIR_PATH, "test.dat"));
		
		printStats();
		
	}
	
	public void breedWithinList(){
		int bredCount = 0;
		while(bredCount < speciesCapacity){
			NeuralNetwork nn1 = species.get(random.nextInt(species.size())).max;
			NeuralNetwork nn2 = null;
			while(nn2 == null || nn2 == nn1){
				nn2 = species.get(random.nextInt(species.size())).max;
			}
			NeuralNetwork child = this.breedNetwork(nn1, nn2);
			double dev = Math.min(getDeviation(child, nn1), getDeviation(child, nn2));
			if(dev > DEVIATION_THRESHOLD){
				this.createNewSpecies(child);
			}
			bredCount++;
		}
	}
	
	public void curateList(){
		if(species.size() > speciesCapacity){
			species.subList(this.speciesCapacity, species.size()).clear();
		}
	}
	
	public void printStats(){
		
		double sum = 0;
		for(Species s : species){
			sum += s.getSpeciesScore();
		}
		double average = sum / species.size();
		
		System.out.println("=======================");
		
		System.out.println("Average: " + average);
		System.out.print("Top 5: ");
		for(int i = 0; i < 5 && i < species.size(); i++){
			System.out.print("{" + species.get(i).getSpeciesScore() + " " + species.get(i).generationsSinceImprovement + " " + species.get(i).max.generateSum() + "} ");
		}
		System.out.println();
		System.out.println("Number of Species: " + species.size());
		
	}
	
	public void purgeWeakest(){
		
	}
	
	public void sortSpeciesByFittest(){
		Collections.sort(species);
		Collections.reverse(species);
	}
	
	public void createBredSpecies(NeuralNetwork net, NeuralNetwork p1, NeuralNetwork p2){
		Species origin = new Species(this, net, 1, random, simulateDelegate);
		origin.simulateGeneration();
		if(origin.max.score > p1.score && origin.max.score > p2.score){
			System.out.println("BRED");
			createNewSpecies(net);
		}
	}
	
	public void createNewSpecies(NeuralNetwork net){
		Species origin = new Species(this, net, 100, random, simulateDelegate);
		newSpecies.add(origin);
	}
	
	public void addNewSpeciesToPool(){
		species.addAll(newSpecies);
		newSpecies.clear();
	}
		
	public int getTotalNetworks(){
		int count = 0;
		for(Species s : species){
			count += s.networks.size();
		}
		return count;
	}
	

	/**
	 * Breed two networks together by considering their new innovations.
	 * @param fittest
	 * @param other
	 */
	public NeuralNetwork breedNetwork(NeuralNetwork fittest, NeuralNetwork other){
		//Ensures variable fittest is indeed the fittest
		if(fittest.score < other.score){
			NeuralNetwork tmp = fittest;
			fittest = other;
			other = tmp;
		}
		
		boolean equal = fittest.score == other.score;
		
		NeuralNetwork clone = new NeuralNetwork(fittest);
		
		//Mix any new innovations from the other network
		long max = Math.max(fittest.maxInnovation, other.maxInnovation);
		for(long i = 0; i < max; i++){
			if(fittest.axonsMap.containsKey(i) && other.axonsMap.containsKey(i)){
				
				if(equal){
					clone.axonsMap.get(i).weight = (random.nextBoolean()) ? fittest.axonsMap.get(i).weight : other.axonsMap.get(i).weight;
				}
				
				//Weight is inherited by the most fit network, but whether or not it's enabled can be inherited by either (by chance)
				clone.axonsMap.get(i).enabled = (random.nextBoolean()) ? fittest.axonsMap.get(i).enabled : other.axonsMap.get(i).enabled;
				
			}else if(other.axonsMap.containsKey(i)){
				Axon gene = other.axonsMap.get(i);
				
				Neuron node = clone.neuronsMap.get(gene.output.neuronID);
				
				//If the gene's output neuron doesn't exist, create it
				if(node == null){
					node = clone.createNeuron(clone.layers.get(gene.output.layer.index), gene.output.neuronID);
				}
				
				//Make the connection
				clone.connectNeurons(clone.neuronsMap.get(gene.input.neuronID), node, gene.weight, gene.innovation);
				
			}else{
				//Innovation does not exist in either networks. Ignore
			}
		}
		
		//For any disabled axons, 25% chance of being reenabled (https://www.cs.cmu.edu/afs/cs/project/jair/pub/volume21/stanley04a-html/node3.html)
		for(Axon a : clone.axons){
			if(!a.enabled){
				if(random.nextDouble() < 0.25){
					a.enabled = true;
				}
			}
		}
		
		return clone;
		
	}
	
	public double getDeviation(NeuralNetwork nn1, NeuralNetwork nn2){
		
		double max = Math.max(nn1.axons.size(), nn2.axons.size());
		
		//Counts how many unique genes are in the system
		int same = 0;
		double deltaWeightSum = 0;
		int unique = 0;
		for(Axon a1 : nn1.axons){
			Axon found = null;
			for(Axon a2 : nn2.axons){
				if(a1.innovation == a2.innovation){
					found = a2;
					break;
				}
			}
			
			if(found == null){
				unique++;
			}else{
				same++;
				deltaWeightSum = Math.abs(a1.weight - found.weight);
			}
			
		}
		
		double w = (same == 0) ? 0 : deltaWeightSum / same;
				
		double dev = 0.1 * w + unique / (max == 0 ? 1 : max);
		
		//System.out.println(w + " " + unique + " " + same + " " + max + " " + dev);
						
		return dev;
	}
	
}
