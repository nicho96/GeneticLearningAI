package ca.nicho.tetris;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.neuralnet.Population;
import ca.nicho.neuralnet.SimulateDelegate;
import ca.nicho.neuralnet.Species;
import ca.nicho.tetris.controller.PerspectiveNeuralNetworkController;

public class Evolver {

	public static final long BOARD_SEED = 0;
	
	public static final File DIR_PATH = new File("networks");
	
	public ArrayList<Species> genomes = new ArrayList<Species>();
	
	public boolean isPaused = false;
	
	public Evolver(){
		
		//new Thread(inputThread).start();
		
		NeuralNetwork origin = new NeuralNetwork(PerspectiveNeuralNetworkController.INPUT_SIZE, 3, 1);
		
		Population p = new Population(100, origin, delegate);
		while(true){
			p.simulateGeneration();
		}

		
	}
	
	private SimulateDelegate delegate = (NeuralNetwork network) -> {
		Board b = new Board(Evolver.BOARD_SEED);
		b.setController(new PerspectiveNeuralNetworkController(b, network));
		b.simulate();
	};
	
	public void runSimulation(){
		
		if(isPaused){
			isPaused = false;
		}
		
		while(!isPaused){
			
			int bestScore = 0;
			Species best = null;
			
			for(Species genome : genomes){
				genome.nextGeneration();
				if(bestScore < genome.max.score){
					bestScore = genome.max.score;
					best = genome;
				}
			}
			
			System.out.println("Generation: " + best.generation + " - Fittest: " + best.max + ", sum: " + best.max.generateSum());
			
			best.max.save(new File(DIR_PATH, "1layers.dat"));
			
		}
	}
	
	public void pauseSimulation(){
		isPaused = true;
	}
	
	private Scanner sc = new Scanner(System.in);
	
	Runnable inputThread = () -> {
		System.out.print("> ");
		String input = sc.nextLine();
		if(input.toLowerCase().equals("p")){
			this.pauseSimulation();
			System.out.println("Will pause after simulation cycle ends.");
		}else if(input.toLowerCase().equals("s")){
			this.runSimulation();
			System.out.println("Resuming iterations");
		}else if(input.toLowerCase().equals("e")){
			sc.close();
			System.out.println("Will close after simulation cycle ends.");
		}
	};
	
}