package ca.nicho.tetris;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import ca.nicho.neuralnet2.NeuralNetwork2;
import ca.nicho.neuralnet2.Screen2;
import ca.nicho.tetris.controller.PerspectiveNeuralNetworkController;

public class Start extends JFrame {
	
	public static boolean GAME_RUNNING = true;
	
	public static boolean EVOLVING = false;
	
	public static void main(String[] s){		
		
		if(EVOLVING) new Evolver();
		else{
			Board board = new Board(Evolver.BOARD_SEED);
						
			NeuralNetwork2 net = null;
			try {
				net = NeuralNetwork2.loadFromFile(new File("networks/strange-1228.dat"));
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
								
			PerspectiveNeuralNetworkController controller = new PerspectiveNeuralNetworkController(board, net);
			board.setController(controller);
			Start start = new Start();
			start.setVisible(true);
			start.startWithNetwork(net, board);
		}
		
	}
	
	private Screen screen;
	
	public Start(){
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //TODO may want to save the state of the AI
		this.getContentPane().setLayout(null);
		this.setResizable(false);
		
		this.setFocusable(false);
		
		screen = new Screen();
		this.add(screen);
		
		this.getContentPane().setPreferredSize(new Dimension(getInsets().left + getInsets().right + screen.getWidth(), getInsets().top + getInsets().bottom + screen.getHeight()));
		this.pack();
	}
	
	public void startWithNetwork(NeuralNetwork2 network, Board board){
		screen.setBoard(board);
		screen.setNeuralNetwork(network);
		new Thread(screen.drawThread).start();		
	}

}
