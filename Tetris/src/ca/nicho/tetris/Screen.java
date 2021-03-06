package ca.nicho.tetris;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import ca.nicho.neuralnet2.Connection;
import ca.nicho.neuralnet2.NeuralNetwork2;
import ca.nicho.neuralnet2.Neuron;
import ca.nicho.neuralnet2.NeuronHidden;
import ca.nicho.neuralnet2.NeuronIn;
import ca.nicho.neuralnet2.NeuronOut;

public class Screen extends JPanel {

	public static final int TILE_SIZE = 25;
	public static final int FRAME_WIDTH = TILE_SIZE * Board.BOARD_WIDTH;
	public static final int FRAME_HEIGHT = TILE_SIZE * Board.BOARD_HEIGHT;
	
	
	public Color POSITIVE_AXON = new Color(0x22F57E);
	public Color NEGATIVE_AXON = new Color(0xF56822);
	public Color DISABLED_AXON = new Color(0xFFFFFF);
	
	private BufferedImage image;
	private int[] raster;
	
	public Board board;
	
	public boolean showOverlay = true;
	
	public NeuralNetwork2 network;
	public HashMap<Integer, Integer> depths = new HashMap<Integer, Integer>();
	public HashMap<Neuron, Point> nodes = new HashMap<Neuron, Point>();
	
	public Screen(){
		this.setSize(FRAME_WIDTH + 800, FRAME_HEIGHT);
		image = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
		raster = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();		
		this.setFocusable(true);
		this.setBackground(new Color(0xA4A4A4));
	}
	
	public void setBoard(Board board){
		this.board = board;
	}
	
	public int dx = 50;
	public int dy = 20;
	public int diameter = 10;
	
	public void setNeuralNetwork(NeuralNetwork2 network){
		this.network = network;
		
		//Start from outputs
		for(NeuronHidden n : network.outputsArr){
			rec(n, 0);
		}
		
		boolean unchanged = false;
		while(!unchanged){
			unchanged = true;
			for(Connection c : network.connectionsArr){
				Point p1 = nodes.get(c.from);
				Point p2 = nodes.get(c.to);
				if(p1.x >= p2.x){
					p1.x = p2.x - dx;
					unchanged = false;
				}
			}
		}
				
	}
	
	private void rec(Neuron n, int depth){
		int x = getWidth() - depth * dx - dx;
		if(!depths.containsKey(depth)){
			depths.put(depth, 0);
		}
		int y = depths.get(depth) + dy;
		depths.put(depth, y);
		
		if(nodes.containsKey(n)){
			Point p = nodes.get(n);
			if(p.getX() < x){
				p.x = x;
			}
			if(n instanceof NeuronHidden){
				for(Connection c : ((NeuronHidden)n).inputs){
					rec(c.from, depth + 1);
				}
			}
		}else{
			Point p = new Point(x, y);
			nodes.put(n, p);
			
			if(n instanceof NeuronHidden){
				for(Connection c : ((NeuronHidden)n).inputs){
					rec(c.from, depth + 1);
				}
			}
		}
		
	}
	
	
	
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		g.drawImage(image, 0, 0, null);
		
		if(network != null && showOverlay){
			drawPerspectiveNetwork(g);
		}
		
	}
	
	public void drawPerspectiveNetwork(Graphics g2){
		
		Graphics2D g = (Graphics2D)g2;
		g.setStroke(new BasicStroke(1));
			
		this.updateInputs();
			
		//Draw neurons
		for(Map.Entry<Neuron, Point> entry : nodes.entrySet()){
			if(entry.getKey().cachedActivation < NeuralNetwork2.ACTIVATION_THRESHOLD){
				g.setColor(Color.RED);
			}else{
				g.setColor(Color.GREEN);
			}	
			
			g.fillOval(entry.getValue().x, entry.getValue().y, diameter, diameter);
		}
		
		for(Connection con: network.connectionsArr){
			if(con.weight < 0){
				g.setColor(Color.RED);
			}else{
				g.setColor(Color.GREEN);
			}
			Point p1 = nodes.get(con.from);
			Point p2 = nodes.get(con.to);
			if(p1 != null && p2 != null)
				g.drawLine(p1.x + diameter / 2, p1.y + diameter / 2, p2.x + diameter / 2, p2.y + diameter / 2);
		}
		
	}
	
	private void updateInputs(){
		for(int i = 0; i < network.inputsArr.size(); i++){
			int x = (i % Board.BOARD_WIDTH + board.currentX - 4) * Screen.TILE_SIZE;
			int y = (i / Board.BOARD_WIDTH + board.currentY) * Screen.TILE_SIZE;
			Point p = nodes.get(network.inputsArr.get(i));
			if(p != null)
				p.setLocation(x + Screen.TILE_SIZE / 2 - diameter / 2, y + Screen.TILE_SIZE / 2 - diameter / 2);
		}
	}
	
	public void drawGrid(){
		for(int x = 0; x < FRAME_WIDTH; x ++){
			for(int y = 0; y < FRAME_HEIGHT; y ++){
				if(x % TILE_SIZE == 0 || y % TILE_SIZE == 0){
					raster[x + y * FRAME_WIDTH] = 0xAAAAAA;
				}
			}
		}
	}
	
	public void clearGraphics(){
		for(int i = 0; i < raster.length; i++)
			raster[i] = 0xFFFFFF;
	}
	
	public void drawBoard(){
		//Draw the current board state
		for(int x = 0; x < Board.BOARD_WIDTH; x++){
			for(int y = 0; y < Board.BOARD_HEIGHT; y++){
				if(board.tiles[x][y]) drawTile(x, y, 0xFF0000);
			}
		}
	}
	
	public void drawActiveTile(){
		if(board.currentTile == null)
			return;
		
		for(int x = 0; x < board.currentTile.tile.length; x++){
			for(int y = 0; y < board.currentTile.tile[0].length; y++){
				if(board.currentTile.tile[x][y]) drawTile(board.currentX + x, board.currentY + y, 0x0000FF);
			}
		}
	}
	
	public void drawTile(int x, int y, int color){
		for(int dx = 0; dx < TILE_SIZE; dx++){
			for(int dy = 0; dy < TILE_SIZE; dy++){
				drawPixel(x * TILE_SIZE + dx, y * TILE_SIZE + dy, color);
			}
		}
	}
	
	public void drawPixel(int x, int y, int color){
		raster[x + FRAME_WIDTH * y] = color;
	}
	
	public void updateScreen(){
		clearGraphics();
		drawBoard();
		drawActiveTile();
		drawGrid();
		repaint();
	}
	
	public Runnable drawThread = new Runnable() {
		public void run(){
			long last = System.currentTimeMillis();
			
			while(Start.GAME_RUNNING){
				
				long current = System.currentTimeMillis();
				if(current - last > 20){
		
					if(board == null){
						continue;
					}
										
					if(!board.isFinished)
						board.tick();
					
					board.controller.update(); //Will allow the game to render properly
					
					updateScreen();
					
					last = current;
					
				}
			}
		}
	};
	
}
