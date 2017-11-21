package ca.nicho.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.nicho.neuralnet.Axon;
import ca.nicho.neuralnet.Layer;
import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.neuralnet.Neuron;

public class Screen extends JPanel {

	public NeuralNetwork network;
	
	public Screen(NeuralNetwork network){
		this.network = network;
		
		this.setSize(600, 500);
		
		JFrame f = prepareFrame();
		f.setLocation(500, 10);
		f.getContentPane().setPreferredSize(new Dimension(getInsets().left + getInsets().right + getWidth(), getInsets().top + getInsets().bottom + getHeight()));
		f.pack();
		f.setVisible(true);	
		f.add(this);
		
		new Thread(drawThread).start();
	}
	
	private JFrame prepareFrame(){
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //TODO may want to save the state of the AI
		frame.getContentPane().setLayout(null);
		frame.setFocusable(false);
		return frame;
	}
	
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		drawNetwork(g);
	}
	
	public void drawNetwork(Graphics g){		
		int dx = 40;
		
		network.computeMaxLayerSize();
		int dy = getHeight() / network.maxLayerSize; //Vertical distance between neurons
		
		int HIDDEN_OFFSET = 10;
				
		//Start at 1, layer 0 is reserved for the inputs
		for(int i = 0; i < network.layers.size(); i++){
			Layer l =  network.layers.get(i);
			int xOff = HIDDEN_OFFSET + dx + i * dx;
			for(int j = 0; j < l.neurons.size(); j++){
				Neuron n = l.neurons.get(j);
				int yOff = dy * j;
				g.setColor(Color.BLACK);
				g.fillOval(xOff, yOff, 25, 25);
				g.setColor(Color.white);
				String s = n.value + "";
				s = s.substring(0, (s.length() >= 4) ? 4 : 3);
				g.drawString(s, xOff, yOff + 17);
				
				for(Axon a : n.outputs){
					if(a.output.layer.index >= 0){
						if(a.weight < 0){
							g.setColor(Color.DARK_GRAY);
						}else{
							g.setColor(Color.white);
						}
						g.drawLine(xOff + 5, yOff + 5, HIDDEN_OFFSET + dx + a.output.layer.index * dx + 5, dy * a.output.indexInLayer + 5);
					}
				}
				
			}
		}
		g.setColor(Color.orange);
		g.drawString("Score " + network.score, getWidth() - 60, 10);
	}
	
	public Runnable drawThread = new Runnable() {
		public void run(){
			long last = System.currentTimeMillis();
			
			while(true){
				long current = System.currentTimeMillis();
				if(current - last > 100){
					repaint();
					last = current;
				}
			}
		}
	};
	
}
