package ca.nicho.neuralnet2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;

public class Screen2 extends JPanel {

	private NeuralNetwork2 network;
	
	public ConcurrentHashMap<Integer, ArrayList<Neuron>> depths;
	public ConcurrentHashMap<Neuron, Point> nodes;
	
	public static final int INPUT_WIDTH = 200;
	
	public String cornerText = "";

	public Screen2(int width, int height){
		this.setSize(width, height);
		this.setBackground(new Color(0xA4A4A4));
	}
	
	public void startAutoDraw(){
		new Thread(drawThread).start();
	}
	
	public int diameter = 10;
	
	public void setNeuralNetwork(NeuralNetwork2 network){
		
		this.nodes = new ConcurrentHashMap<Neuron, Point>();
		this.depths = new ConcurrentHashMap<Integer, ArrayList<Neuron>>();
		this.network = network;
		
		//Start from outputs
		for(NeuronHidden n : network.outputsArr){
			getDepth(n, 0);
		}
		
		//Get deltas in each direction
		int dx = (getWidth() - INPUT_WIDTH - 80) / depths.size();
				
		for(Map.Entry<Integer, ArrayList<Neuron>> entries : depths.entrySet()){
			int dy = (getHeight() - 80) / entries.getValue().size();
			int yOff = (getHeight() - dy * entries.getValue().size()) / 2;
			int x = getWidth() - entries.getKey() * dx - 40;
			for(int y = 0; y < entries.getValue().size(); y++){
				nodes.get(entries.getValue().get(y)).setLocation(x, yOff + y * dy);
			}
		}
		
		//Setup inputs as a square (might want a feature to disable)
		int sq = (int)Math.sqrt(network.inputsArr.size());
		int d = (INPUT_WIDTH - 40) / sq;
		int yOff = (getHeight() - d * sq) / 2;
		for(int i = 0; i < network.inputsArr.size(); i++){
			int x = i % sq;
			int y = i / sq;
			Point p = nodes.get(network.inputsArr.get(i));
			if(p != null){
				p.setLocation(10 + x * d, yOff + y * d);
			}else{
				p = new Point(10 + x * d, yOff + y * d);
				nodes.put(network.inputsArr.get(i), p);
			}
		}
				
	}
	
	private void getDepth(Neuron n, int depth){
		if(!depths.containsKey(depth)){
			depths.put(depth, new ArrayList<Neuron>());
		}
		
		if(nodes.containsKey(n)){ //Already encountered node
			if(n.depth < depth){ //Add move it to next depth (right to left)
				depths.get(n.depth).remove(n);
				depths.get(depth).add(n);
				n.depth = depth;
			}
		}else{
			nodes.put(n, new Point(0, 0)); //Default point
			depths.get(depth).add(n);
			n.depth = depth;
			if(n instanceof NeuronHidden){
				for(Connection c : ((NeuronHidden)n).inputs){
					getDepth(c.from, depth + 1);
				}
			}
		}
		
	}
	
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		drawNetwork(g);
	}
	
	public void drawNetwork(Graphics g2){		
		
		if(this.network == null){
			return;
		}
		
		Graphics2D g = (Graphics2D)g2;
		g.setStroke(new BasicStroke(1));
						
		//Draw neurons
		for(Map.Entry<Neuron, Point> entry : nodes.entrySet()){
			if(entry.getKey().cachedActivation < 0){
				g.setColor(Color.BLUE);
			}else if(entry.getKey().cachedActivation <= NeuralNetwork2.ACTIVATION_THRESHOLD){
				g.setColor(Color.LIGHT_GRAY);
			}else{
				g.setColor(Color.GREEN);
			}	
						
			g.fillOval(entry.getValue().x, entry.getValue().y, diameter, diameter);
		}
		
		for(Connection con: network.connectionsArr){
			if(con.weight < 0){
				g.setColor(Color.BLACK);
			}else{
				g.setColor(Color.WHITE);
			}
			Point p1 = nodes.get(con.from);
			Point p2 = nodes.get(con.to);
			if(p1 != null && p2 != null)
				g.drawLine(p1.x + diameter / 2, p1.y + diameter / 2, p2.x + diameter / 2, p2.y + diameter / 2);
		}
		
		g.setColor(Color.white);
		g.drawString(cornerText, 10, 10);
		g.drawString("Last Score: " + network.getScore(), 10, 25);
		g.drawString("Was Mutated: " + network.mutated, 10, 40);


	}
	
	public NeuralNetwork2 getNetwork(){
		return network;
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
