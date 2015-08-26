package test;

import java.io.File;
import java.util.List;

import runtime.ChainBuilder;

import components.Chain;
import components.Touch;

//This class will print out the model constructed form the designated file
public class Print_model {
	//TODO define parameters
	final static String data_path = "data_sets/3-30-15_timdee_07924e50.csv";
	final static int window = 5;
	final static int token = 5;
	final static int threshold = 50000;
	final static int model_size = 5000;
	
	public static void main(String[] args){
		Chain chain = new Chain(window, token, threshold, model_size);
		
		// cosntruct the model
		List<Touch> touches = ChainBuilder.parse_csv(new File(data_path));
		chain.add_touch_list(touches);
		
		//cause computations to happen
		chain.get_distribution();
		chain.get_key_distribution();
		chain.get_touch_probability(null, null);
		
		// print out the model
		chain.output_to_csv();
	}
}
