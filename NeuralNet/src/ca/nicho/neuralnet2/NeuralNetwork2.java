package ca.nicho.neuralnet2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

public class NeuralNetwork2 implements Comparable<NeuralNetwork2> {
	
	public static final float ACTIVATION_THRESHOLD = 0.5F;
	
	public ArrayList<Connection> connectionsArr = new ArrayList<Connection>();
	public LinkedHashMap<Long, Connection> connections = new LinkedHashMap<Long, Connection>();
	
	public ArrayList<NeuronIn> inputsArr = new ArrayList<NeuronIn>();
	public ArrayList<NeuronOut> outputsArr = new ArrayList<NeuronOut>();
	public ArrayList<NeuronHidden> hiddenArr = new ArrayList<NeuronHidden>();

	public LinkedHashMap<Long, NeuronIn> inputs = new LinkedHashMap<Long, NeuronIn>();
	public LinkedHashMap<Long, NeuronOut> outputs = new LinkedHashMap<Long, NeuronOut>();
	public LinkedHashMap<Long, NeuronHidden> hidden = new LinkedHashMap<Long, NeuronHidden>();
	
	public InnovationHandler ih;
	public long maxInnovation = 0;
	
	private double score;
	public boolean simulated = false;
	public boolean mutated = true;
	
	//Allows you to modify the rates of mutation. Must be handled by an evolver, outside of the NeuralNetwork class
	//TODO Probably move this to a genome/species object eventually
	public double[] mutationRates;
	public double mutationRatesSum;
	
	/**
	 * Creates a new network
	 * @param ih The innovation handler, which handles incrementing the innovation count
	 * @param inputSize input size of neurons
	 * @param outputSize output size of neurons
	 */
	public NeuralNetwork2(InnovationHandler ih, int inputSize, int outputSize){
		this.ih = ih;
				
		for(int i = 0; i < inputSize; i++){
			NeuronIn in = new NeuronIn(ih.nextInnovation(this));
			inputs.put(in.innovation, in);
			inputsArr.add(in);
		}
		
		for(int i = 0; i < outputSize; i++){
			NeuronOut out = new NeuronOut(ih.nextInnovation(this));
			outputs.put(out.innovation, out);
			outputsArr.add(out);
		}
	}
	
	/**
	 * Creates a copy of the parent network
	 * @param parent the parent version
	 */
	public NeuralNetwork2(NeuralNetwork2 parent){
		this.ih = parent.ih;
			
		for(NeuronIn in : parent.inputs.values()){
			NeuronIn n = new NeuronIn(in.innovation);
			inputs.put(n.innovation, n);
			inputsArr.add(n);
		}
		
		for(NeuronOut out : parent.outputs.values()){
			NeuronOut n = new NeuronOut(out.innovation);
			outputs.put(n.innovation, n);
			outputsArr.add(n);
		}
		
		for(NeuronHidden hid : parent.hidden.values()){
			NeuronHidden n = new NeuronHidden(hid.innovation);
			hidden.put(n.innovation, n);
			hiddenArr.add(n);
		}
		
		LinkedHashMap<Long, NeuronHidden> tmpHidden = getNonInputNeuronsMap();
		LinkedHashMap<Long, Neuron> tmpAll = getAllNeuronsMap();
		
		for(Connection con : parent.connectionsArr){
			Neuron from = tmpAll.get(con.from.innovation);
			NeuronHidden to = tmpHidden.get(con.to.innovation);
			Connection c = new Connection(from, to, con.innovation, con.weight);
			connections.put(c.innovation, c);
			connectionsArr.add(c);
		}
		
		//Retain the previously made mutationRates
		this.mutationRates = parent.mutationRates.clone();
		this.mutationRatesSum = parent.mutationRatesSum;
		
	}
	
	public void prepareMutationRates(Random r){
		mutationRates = new double[]{r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()};
		for(double d : mutationRates){
			mutationRatesSum += d;
		}
	}
		
	/**
	 * Makes a connection between two neurons
	 * @param from
	 * @param to
	 * @param weight
	 * @return the newly made connection
	 */
	public Connection makeConnection(Neuron from, NeuronHidden to, double weight){
		return makeConnection(from, to, weight, ih.nextInnovation(this));
	}
	
	/**
	 * Internally used to add the innovation number
	 * @param from
	 * @param to
	 * @param weight
	 * @param innovation
	 * @return
	 */
	public Connection makeConnection(Neuron from, NeuronHidden to, double weight, long innovation){
		Connection con = new Connection(from, to, innovation, weight);
		this.connections.put(con.innovation, con);
		this.connectionsArr.add(con);
		return con;
	}
	
	/**
	 * Creates a new hidden neuron
	 * @return
	 */
	public NeuronHidden createHiddenNeuron(){
		return createHiddenNeuron(ih.nextInnovation(this));
	}
	
	/**
	 * Internally used to add an innovation number to the newly created neuron
	 * @param innovation
	 * @return
	 */
	public NeuronHidden createHiddenNeuron(long innovation){
		NeuronHidden hid = new NeuronHidden(innovation);
		hidden.put(hid.innovation, hid);
		hiddenArr.add(hid);
		return hid;
	}
	
	/**
	 * Split a connection into two discrete connections, that end with the same result
	 * @param con the connection to be split
	 */
	public void splitConnection(Connection con){
		
		con.enabled = false;
		
		Neuron left = con.from;
		NeuronHidden middle = createHiddenNeuron();
		NeuronHidden right = con.to;
		
		this.makeConnection(left, middle, 1);
		this.makeConnection(middle, right, con.weight);
		
	}
	
	/**
	 * Update the network's inputs
	 * @param in
	 */
	public void setInputs(double[] in){
		for(int i = 0; i < in.length; i++){
			inputsArr.get(i).value = in[i];
		}
	}
	
	public int activationID = 0;
	/**
	 * Activate neurons and get outputs
	 * @return
	 */
	public double[] getOuputs(){
		activationID = (activationID + 1) % 2; //Toggling between 0 and 1 is sufficient
		double[] output = new double[outputsArr.size()];
		for(int i = 0; i < outputsArr.size(); i++){
			output[i] = outputsArr.get(i).activate(activationID);
		}
		return output;		
	}
	
	
	/**
	 * Generate a map of all non-input neurons (ignores input layer)
	 * @return
	 */
	public LinkedHashMap<Long, NeuronHidden> getNonInputNeuronsMap(){
		LinkedHashMap<Long, NeuronHidden> tmpHidden = new LinkedHashMap<Long, NeuronHidden>();
		tmpHidden.putAll(hidden);
		tmpHidden.putAll(outputs);
		return tmpHidden;
	}
	
	/**
	 * Generate a list map of all neurons
	 * @return
	 */
	public LinkedHashMap<Long, Neuron> getAllNeuronsMap(){
		LinkedHashMap<Long, Neuron> tmpAll = new LinkedHashMap<Long, Neuron>();
		tmpAll.putAll(inputs);
		tmpAll.putAll(hidden);
		tmpAll.putAll(outputs);
		return tmpAll;
	}
	
	/**
	 * Generate a list of all non-input neurons
	 * @return
	 */
	public ArrayList<NeuronHidden> getNonInputNeuronsArray(){
		return new ArrayList<NeuronHidden>(getNonInputNeuronsMap().values());
	}
	
	/**
	 * Generate a list of all neurons
	 * @return
	 */
	public ArrayList<Neuron> getAllNeuronsArray(){
		return new ArrayList<Neuron>(getAllNeuronsMap().values());
	}
	
	/**
	 * Save the network to a binary file
	 * @param f the output file
	 * @throws IOException
	 */
	public void save(File f) throws IOException {
		if(!f.exists()){
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
		out.writeInt(inputsArr.size());
		out.writeInt(outputsArr.size());
		out.writeLong(ih.currentInnovation());
		out.writeInt(connectionsArr.size());
		out.write(toByteDNA());
		out.close();
		
	}
	
	/**
	 * Each gene is 33 bytes. The structure is as follows:
	 * 
	 * 8 Byte innovation number (long)
	 * 8 Byte weight (double, bounded between -1 and 1)
	 * 8 byte input innovation number (long)
	 * 8 byte output innovation number (long)
	 * 1 byte gene enabled status (boolean)
	 * 
	 * @return an array of bytes that is 33 * connection count bytes long
	 */
	public byte[] toByteDNA(){
		
		ByteBuffer buffer = ByteBuffer.allocate(connectionsArr.size() * 33);

		for(Connection con : connectionsArr){
			buffer.putLong(con.innovation);
			buffer.putDouble(con.weight);
			buffer.putLong(con.from.innovation);
			buffer.putLong(con.to.innovation);
			buffer.put((byte) ((con.enabled) ? 1 : 0));
		}
				
		return buffer.array();
	}
	
	public void setScore(double score){
		this.simulated = true;
		this.mutated = false;
		this.score = score;
	}
	
	public double getScore(){
		return this.score;
	}
	
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder();
		b.append(inputs.values().toString() + "\n");
		b.append(connections.values().toString() + "\n");
		b.append(outputs.values().toString());
		return b.toString();
	}
	
	@Override
	public int compareTo(NeuralNetwork2 other) {
		
		if(this.getScore() < other.getScore()){
			return -1;
		}else if(this.getScore() > other.getScore()){
			return 1;
		}else{
			if(this.connectionsArr.size() > other.connectionsArr.size()){
				return -1;
			}else if(this.connectionsArr.size() < other.connectionsArr.size()){
				return 1;
			}
		}
		
		return 0;
	}
	
	public static NeuralNetwork2 loadFromFile(File f) throws IOException{
		DataInputStream in = new DataInputStream(new FileInputStream(f));
		
		int inputSize = in.readInt();
		int outputSize = in.readInt();
		long currentInnovation = in.readLong();
		int genesSize = in.readInt();
		byte[] dna = new byte[genesSize * 33];
		in.read(dna);
		in.close();
		
		NeuralNetwork2 net = NeuralNetwork2.createFromDNA(inputSize, outputSize, dna);
		
		net.ih.setInnovation(currentInnovation);
		
		
		
		return net;
	}
	
	public static NeuralNetwork2 createFromDNA(int inputSize, int outputSize, byte[] dna){
		int geneCount = dna.length / 33;
		
		NeuralNetwork2 net = new NeuralNetwork2(new InnovationHandler(), inputSize, outputSize);
		
		//Keep a cache of all neurons
		LinkedHashMap<Long, Neuron> cache = net.getAllNeuronsMap();
		
		ByteBuffer buffer = ByteBuffer.wrap(dna);
		for(int i = 0; i < geneCount; i++){
			long c_in = buffer.getLong();
			double c_weight = buffer.getDouble();
			long n_from = buffer.getLong();
			long n_to = buffer.getLong();
			boolean enabled = buffer.get() == 1;
			
			Neuron from = cache.get(n_from);
			NeuronHidden to = (NeuronHidden)cache.get(n_to);
			
			if(from == null){
				from = net.createHiddenNeuron(n_from);
				cache.put(from.innovation, from);
			}
			
			if(to == null){
				to = net.createHiddenNeuron(n_to);
				cache.put(to.innovation, to);
			}
			
			net.makeConnection(from, to, c_weight, c_in).enabled = enabled;
		}
		
		return net;
	}

}
