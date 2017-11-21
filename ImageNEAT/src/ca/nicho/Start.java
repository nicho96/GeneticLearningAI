package ca.nicho;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import ca.nicho.neuralnet2.InnovationHandler;
import ca.nicho.neuralnet2.NeuralNetwork2;
import ca.nicho.neuralnet2.Neuron;
import ca.nicho.neuralnet2.NeuronHidden;
import ca.nicho.neuralnet2.NeuronOut;

public class Start {

	public static final int INPUT_WIDTH = 10;
	public static final int INPUT_HEIGHT = 10;
	public static final int COLOR_SIZE = 3; //RGB
	
	public static void main(String[] s) throws IOException{
		
		int outputSize = INPUT_WIDTH * INPUT_HEIGHT * COLOR_SIZE;
		int inputSize = 2 * outputSize;
		
		NeuralNetwork2 net = new NeuralNetwork2(new InnovationHandler(), inputSize, outputSize);
		
		for(int i = 0; i < net.outputsArr.size(); i++){
			net.makeConnection(net.inputsArr.get(2 * i), net.outputsArr.get(i), 0.1);
		}
		
		BufferedImage img1 = ImageIO.read(new File("imgs/i1.jpg"));
		BufferedImage img2 = ImageIO.read(new File("imgs/i2.jpg"));
		BufferedImage imgo = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		
		for(int offX = 0; offX < 10; offX++){
			for(int offY = 0; offY < 10; offY++){
				
				double[] inputs = new double[net.inputsArr.size()];
				int i = 0;
				for(int x = 0; x < INPUT_WIDTH; x++){
					for(int y = 0; y < INPUT_HEIGHT; y++){
						
						Color c1 = new Color(img1.getRGB(offX * 10 + x, offY * 10 + y));
						Color c2 = new Color(img2.getRGB(offX * 10 + x, offY * 10 + y));
						inputs[i++] = c1.getRed() / 255.0;
						inputs[i++] = c2.getRed() / 255.0;
						inputs[i++] = c1.getGreen() / 255.0;
						inputs[i++] = c2.getGreen() / 255.0;
						inputs[i++] = c1.getBlue() / 255.0;
						inputs[i++] = c2.getBlue() / 255.0;

					}
				}
				
				int j = 0;
				for(int x = 0; x < INPUT_WIDTH; x++){
					for(int y = 0; y < INPUT_HEIGHT; y++){
						int r = (int)(net.outputsArr.get(j++).getValue(offY) * 255);
						int g = (int)(net.outputsArr.get(j++).getValue(offY) * 255);
						int b = (int)(net.outputsArr.get(j++).getValue(offY) * 255);
						Color c = new Color(r, g, b);
						imgo.setRGB(offX * 10 + x, offY * 10 + y, c.getRGB());
					}
				}
				
			}
		}
		
		File f = new File("imgs/out.png");
		if(!f.exists()){
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
		
		ImageIO.write(imgo, "png", f);
				
	}
	
}
