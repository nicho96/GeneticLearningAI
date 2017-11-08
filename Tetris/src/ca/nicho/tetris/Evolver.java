package ca.nicho.tetris;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import ca.nicho.neuralnet.DefaultNEAT;
import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.neuralnet.Population;
import ca.nicho.neuralnet.Species;
import ca.nicho.tetris.controller.PerspectiveNeuralNetworkController;

public class Evolver {

	public static final long BOARD_SEED = 0;
	
	public static final File DIR_PATH = new File("networks");
	
	public ArrayList<Species> genomes = new ArrayList<Species>();
	
	public boolean isPaused = false;
	
	public Evolver(){
				
		NeuralNetwork origin = new NeuralNetwork(PerspectiveNeuralNetworkController.INPUT_SIZE, 3, 1);
		
		DefaultNEAT neat = new DefaultNEAT(origin, delegate);

		int generation = 0;
		while(true){
			generation++;
			System.out.println("==== Current Generation: " + generation + " ====");
			neat.nextGeneration();
			this.save(neat.getMaxNetwork());
		}
		
	}
	
	private DefaultNEAT.SimulateDelegate delegate = (NeuralNetwork network) -> {
		Board b = new Board(Evolver.BOARD_SEED);
		b.setController(new PerspectiveNeuralNetworkController(b, network));
		b.simulate();
	};
	
	private void save(NeuralNetwork nn){
		File f = new File(DIR_PATH, "test.dat");
		if(!f.exists()){
			f.mkdirs();
			try {
				f.createNewFile();
			} catch (IOException e) {
				System.out.println(e.getMessage());
				return;
			}
		}
		nn.save(f);
	}
	
}
