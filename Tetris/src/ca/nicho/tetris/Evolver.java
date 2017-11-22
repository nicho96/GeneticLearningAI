package ca.nicho.tetris;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.neuralnet.Species;
import ca.nicho.neuralnet2.NeuralNetwork2;
import ca.nicho.neuralnet2.neat2.DefaultNEAT2;
import ca.nicho.tetris.controller.PerspectiveNeuralNetworkController;

public class Evolver {

	public static final long BOARD_SEED = 0;
	
	public static final File DIR_PATH = new File("networks");
	public static final File POOL_PATH = new File("pools/" + System.currentTimeMillis());
	
	public ArrayList<Species> genomes = new ArrayList<Species>();
	
	public boolean isPaused = false;
	
	public Evolver(){
						
		DefaultNEAT2 neat = null;
		try {
			neat = DefaultNEAT2.loadFromFileDialog(100, delegate);
		} catch (Exception e1) {
			System.exit(-1);
		}

		int generation = 0;
		while(true){
			generation++;
			System.out.println("==== Current Generation: " + generation + " Max: " + neat.getMaxNetwork().getScore() + " ====");
			neat.nextGeneration();
			for(NeuralNetwork2 net: neat.networks.subList(0, (neat.networks.size() >= 5) ? 5 : neat.networks.size())){
				System.out.print("{" + net.getScore() + " " + neat.getVariation(neat.getMaxNetwork(), net) + "} ");
				net.simulated = false; //Force the top 5 to be resimulated (just for testing purposes)
			}
			System.out.println();
			try {
				neat.saveNetworkPool(new File(POOL_PATH, generation + ".pool"));
				neat.getMaxNetwork().save(new File(DIR_PATH, "delete.dat"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private DefaultNEAT2.SimulateDelegate delegate = (NeuralNetwork2 network) -> {
		Board b = new Board(Evolver.BOARD_SEED);
		b.setController(new PerspectiveNeuralNetworkController(b, network));
		b.simulate();
	};
	
	private void save(NeuralNetwork nn){
		File f = new File(DIR_PATH, "test3.dat");
		if(!f.exists()){
			f.getParentFile().mkdirs();
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
