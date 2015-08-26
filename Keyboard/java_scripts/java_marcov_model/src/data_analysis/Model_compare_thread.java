package data_analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import runtime.ChainBuilder;

import components.Chain;
import components.Touch;

///this thread allows the preforming of a test compairason.
///when the compairason is finished, an instance variable will be set indicating different results.
public class Model_compare_thread implements Runnable{
	public double max_authentication_probability;
	public double min_authentication_probability;
	public double average_authentication_probability;
	
	private String base_data_path;
	private String auth_data_path;
	private int window_size;
	private int token_size;
	private int base_model_size;
	private int auth_model_size;
	private int threshold;
	
	///constructor, allowing user to set different probperties of the model compairason for testing
	public Model_compare_thread(String base_data_path, String auth_data_path, int base_model_size, int auth_model_size, int window_size, int token_size, int threshold){
		this.base_data_path = base_data_path;
		this.auth_data_path = auth_data_path;
		this.window_size = window_size;
		this.token_size = token_size;
		this.base_model_size = base_model_size;
		this.auth_model_size = auth_model_size;
		this.threshold = threshold;
	}
	
	//TODO TODO TODO fix this so it actually compares base_data_path and auth_data_path
	@Override
	public void run() {
		//TODO run the model_compairason test
		// 1) build the models with chain builder
		// 1a) based on the touch data in the csv file
		// 2) call authenticate
		// 3) wait for the authentication to complete
		// 4) put the results of this test into the instance variables
		//I will do an iteration of this process for every base_model_size+auth_model_size in the data file.
		//I will average the results of these iterations
		Chain base_chain = new Chain(window_size, token_size, threshold, base_model_size);
		Chain auth_chain = new Chain(window_size, token_size, threshold, auth_model_size);
		List<Touch> base_touch_list;
		List<Touch> auth_touch_list;
		double result;
		
		//TODO doing this as an incremental process instead of comparing every time would make it go faster
		//TODO adjust this bigger to make the test go faster
		//minimum should be window size because less than that will result in no noticable changes
		int size_increment = auth_model_size;
		List<Double> authentication_probability_list = new ArrayList<Double>();
		
		//first parse the csv file to get a list of touches
		base_touch_list = ChainBuilder.parse_csv(new File(base_data_path));
		auth_touch_list = ChainBuilder.parse_csv(new File(auth_data_path));
		
		//TODO for each auth_model_size of auth data, authenticate against base_model
		for(int a =0;((a<base_touch_list.size()) && (a<auth_touch_list.size()));a++){
			//create base model
			for(int b=0;((b<base_model_size) && (((a*base_model_size)+b)<base_touch_list.size()));b++){
				base_chain.add_touch(base_touch_list.get((a*base_model_size)+b));
			}
			
			//create auth model
			for(int c=0;((c<auth_model_size) && (((a*auth_model_size)+c)<auth_touch_list.size()));c++){
				auth_chain.add_touch(auth_touch_list.get((a*auth_model_size)+c));
			}
			
			//preform the authentication
			result = base_chain.compare_to(auth_chain);
			
			//git the authentication result and add it to the probability list	
			authentication_probability_list.add(1 - result);
		}

		// average the results and place them in the instance variables
		double probability_sum=0;
		int probability_count=0;
		max_authentication_probability=0;
		min_authentication_probability=1;
		for(int i=0;i<authentication_probability_list.size();i++){
			//determine the max,min
			if(authentication_probability_list.get(i) > max_authentication_probability){
				max_authentication_probability=authentication_probability_list.get(i);
			}
			if(authentication_probability_list.get(i) < min_authentication_probability){
				min_authentication_probability=authentication_probability_list.get(i);
			}
			
			probability_sum +=authentication_probability_list.get(i);
			probability_count++;
		}
		
		average_authentication_probability=probability_sum/probability_count;
		
		print_complete_message();
	}
	
	
	//prints out a message indicating the test has completed
	private void print_complete_message(){
		String auth_data_name = auth_data_path;
		String base_data_name = base_data_path;
		
		//put the data paths in a more read-able format
		String[] split_auth_string = auth_data_name.split("/");
		String[] split_base_string = base_data_name.split("/");
		
		auth_data_name = split_auth_string[split_auth_string.length-1];
		base_data_name = split_base_string[split_base_string.length-1];
		
		//output.print("-");
		System.out.println("base_file_name:"+ base_data_name + "\t"
				+ "auth_file_name:" + auth_data_name + "\t"
				+ "window_size:" + window_size + "\t"
				+ "token_size:" + token_size + "\t"
				+ "threshold:" + threshold + "\t"
				+ "base_size:" + base_model_size + "\t"
				+ "auth_size:" + auth_model_size + "\t"
				+ "min_prob:" + min_authentication_probability + "\t"
				+ "max_prob:" + max_authentication_probability + "\t"
				+ "average_prob:" + average_authentication_probability);
	}
	
	
	public String get_base_data_path(){
		return base_data_path;
	}
	
	
	public String get_auth_data_path(){
		return auth_data_path;
	}
	
	
	public int get_window_size(){
		return window_size;
	}
	
	
	public int get_token_size(){
		return token_size;
	}
	
	
	public int get_threshold(){
		return threshold;
	}
	
	
	public int get_base_model_size(){
		return base_model_size;
	}
	
	
	public int get_auth_model_size(){
		return auth_model_size;
	}
}
