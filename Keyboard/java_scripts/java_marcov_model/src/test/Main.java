package test;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import runtime.ChainBuilder;
import runtime.CompareChains;
import components.Chain;
import components.Distribution;
import components.Token;
import components.Touch;
import components.Window;

///TODO generate a csv file for testing
///This class is used to test that the model is being built correctly. Also tested is the model compairason. and various classes used in model creating. The idea is to print out the tests which fail.
///This class should have to do no actual work if the program is designed well.
public class Main{
	final static String TOUCH_TEST_FILE_NAME = "test_touch.csv";
	final static int TOUCH_TEST_FILE_LENGTH = 1000;
	final static int SPEED_TEST_CHAIN_SIZE = 10000;
	final static int SPEED_TEST_WINDOW_SIZE = 5;
	final static int SPEED_TEST_TOKEN_SIZE = 10;
	final static int SPEED_TEST_THRESHOLD = 1000;
	
	///enumerate the differant types of tests which may be run
	private enum TestTypes{
		SPEED ("Speed, output execution time of operations",0),
		CORRECTNESS ("Correctness, output pass / fail indicators of correctness",1);

		private final String description;
		private final int identifier;
		
		TestTypes(String description, int identifier){
			this.description = description;
			this.identifier = identifier;
		}

		public String toString(){
			return description;
		}
		
		public int get_identifier(){
			return identifier;
		}
	}

	///enumerate the different types of test files which may be built. Some inner enumerations are defined. The idea is that the test file may be any combination of these inner enumerations.
	private enum TestFiles{
		//TODO um.... check if this works... yeah
		PREEXISTING ("Preexisting file in working directory named: "+TOUCH_TEST_FILE_NAME, 0),
		SELECT ("Build a test file", 1);

		private final String description;
		private final int identifier;
		
		TestFiles(String description, int identifier){
			this.description = description;
			this.identifier = identifier;
		}

		public String toString(){
			return description;
		}
		
		public int get_identifier(){
			return identifier;
		}
		
		//enumerate properties of test files
		//the amount of pressure, this will determine the median pressure
		public enum PressureAmount{
			HIGH ("High pressure, 0.75", 0, .75),
			MEDIUM ("Medium Pressure, 0.5", 1, .5),
			LOW ("Low Pressure, 0.25", 2, .25);

			private final String description;
			private final int identifier;
			private final double value;
			
			PressureAmount(String description, int identifier, double value){
				this.description = description;
				this.identifier = identifier;
				this.value = value;
			}
			
			public String toString(){
				return description;
			}
		
			public int get_identifier(){
				return identifier;
			}

			public double get_value(){
				return value;
			}
		}

		//Defines what type of distribution will be written to the test file
		//TODO decide the value of double
		public enum Distribution{
			NORMAL ("Normal, centered about pressure median", 0, 0),
			ABNORMAL ("Abnormal, centered about pressure median, but inverted", 1, 0),
			RANDOM ("Random, completly and utterly random", 2, 0);

			private final String description;
			private final int identifier;
			private final double value;
			
			Distribution(String description, int identifier, double value){
				this.description = description;
				this.identifier = identifier;
				this.value = value;
			}
			
			public String toString(){
				return description;
			}
		
			public int get_identifier(){
				return identifier;
			}
		
			public double get_value(){
				return value;
			}
		}

		//this determines the width of the distribution. high concentration implies low standard deviation.
		//TODO decide the value
		public enum Concentration{
			HIGH ("High, [std deviation]", 0, 0),
			MEDIUM ("Medium, [std deviation]",1, 0),
			LOW ("Low, [std deviation]",2, 0);

			private final String description;
			private final int identifier;
			private final double value;
			
			Concentration(String description, int identifier, double value){
				this.description = description;
				this.identifier = identifier;
				this.value = value;
			}
			
			public String toString(){
				return description;
			}
		
			public int get_identifier(){
				return identifier;
			}
		
			public double get_value(){
				return value;
			}
		}
	}

	//all methods return true if they pass
	public static void main(String args[]){
		// allow the user to select between tests for correctness and tests for speed
		TestTypes type = get_user_input_test_type();

		//TODO construct the test file
		//build_touch_test_file(TOUCH_TEST_FILE_NAME);
		
		switch(type){
			case CORRECTNESS:
				correctness_test();
				break;
			case SPEED:
				speed_test();
				break;
		}
	}


	///TODO builds a test csv file containing touches. This will be read in to build the marcov model. Make this easy to modify. The goal is to be able to test how different types of data will affect the model.
	private static void build_touch_test_file(String file_name){
		//TODO prompt the user, asking which type of test file they would like to generate.
		Scanner input = new Scanner(System.in);
		Scanner output = null;
		boolean invalid = true;
		TestFiles type = null;
		int choice;
		
		while(invalid){
			prompt_test_file_type();
			
			while(!input.hasNextLine());
			choice = Integer.valueOf(input.nextLine());
			
			for(TestFiles t_f : TestFiles.values()){
				if(t_f.get_identifier()==choice){
					invalid = false;
					type = t_f;
					break;
				}
			}
		}

		switch(type){
			case PREEXISTING:
				break;
			case SELECT:
				//ask for differant properties of the file
				double file_distribution = get_file_distribution();
				double file_concentration = get_file_concentration();
				double file_pressure_amount = get_file_pressure_amount();

				//construct the file based on these values
				try {
					output = new Scanner(new File(file_name));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for(int i=0;i<TOUCH_TEST_FILE_LENGTH;i++){
					//TODO generate touch information
					//TODO write touch information to output
				}

				break;
		}
		
		output.close();
		input.close();
	}


	// get user input from std in
	private static TestTypes get_user_input_test_type(){
		Scanner input = new Scanner(System.in);
		boolean invalid = true;
		TestTypes type = null;
		int choice;
		
		while(invalid){
			prompt_test_type();

			choice = Integer.valueOf(input.nextLine());
			for(TestTypes t_t : TestTypes.values()){
				if(t_t.get_identifier()==choice){
					invalid = false;
					type = t_t;
					break;
				}
			}
		}

		input.close();
		return type;
	}


	// display user options for types of tests
	private static void prompt_test_type(){
		System.out.println("");		
		System.out.println("Which type of test would you like to run:");

		//for all enum values		
		for (TestTypes t_t : TestTypes.values()) {
 			System.out.println(t_t.get_identifier()+") "+t_t.toString());			
		}
	}

	
	// display user options for types of files
	private static void prompt_test_file_type(){
		System.out.println("");		
		System.out.println("Which type of test file would you like to use:");

		//for all enum values		
		for (TestFiles t_f : TestFiles.values()) {
 			System.out.println(t_f.get_identifier()+") "+t_f.toString());			
		}
	}


	// display user options for file distribution, return the value associated with the selection
	private static double get_file_distribution(){
		Scanner input = new Scanner(System.in);
		boolean invalid = true;
		TestFiles.Distribution type = null;
		int choice;
		
		while(invalid){
			display_file_distribution_options();

			choice = Integer.valueOf(input.nextLine());
			for(TestFiles.Distribution t_f : TestFiles.Distribution.values()){
				if(t_f.get_identifier()==choice){
					invalid = false;
					type = t_f;
					break;
				}
			}
		}

		input.close();
		return type.get_value();
	}


	// display user options for file concentration, return the value associated with the selection
	private static double get_file_concentration(){
		Scanner input = new Scanner(System.in);
		boolean invalid = true;
		TestFiles.Concentration type = null;
		int choice = 0;
		
		while(invalid){
			display_file_concentration_options();

			choice = Integer.valueOf(input.nextLine());
			for(TestFiles.Concentration t_f : TestFiles.Concentration.values()){
				if(t_f.get_identifier()==choice){
					invalid = false;
					type=t_f;
					break;
				}
			}
		}

		input.close();
		return type.get_value();
	}


	// display user options for file pressure amount, return the value associated with the selection
	private static double get_file_pressure_amount(){
		Scanner input = new Scanner(System.in);
		boolean invalid = true;
		TestFiles.PressureAmount type = null;
		int choice;
		
		while(invalid){
			display_file_pressure_amount_options();

			choice = Integer.valueOf(input.nextLine());
			for(TestFiles.PressureAmount t_f : TestFiles.PressureAmount.values()){
				if(t_f.get_identifier()==choice){
					invalid = false;
					type = t_f;
					break;
				}
			}
		}

		input.close();
		return type.get_value();
	}

	// display distribution options
	private static void display_file_distribution_options(){
		System.out.println("");		
		System.out.println("What should be the file distribution:");

		//for all enum values		
		for (TestFiles.Distribution t_f : TestFiles.Distribution.values()) {
 			System.out.println(t_f.get_identifier()+") "+t_f.toString());			
		}
	}


	// display concentration options
	private static void display_file_concentration_options(){
		System.out.println("");		
		System.out.println("What should be the file distribution:");

		//for all enum values		
		for (TestFiles.Concentration t_f : TestFiles.Concentration.values()) {
 			System.out.println(t_f.get_identifier()+") "+t_f.toString());			
		}
	}


	// display pressure amount options
	private static void display_file_pressure_amount_options(){
		System.out.println("");		
		System.out.println("What should be the file distribution:");

		//for all enum values		
		for (TestFiles.PressureAmount t_f : TestFiles.PressureAmount.values()) {
 			System.out.println(t_f.get_identifier()+") "+t_f.toString());			
		}
	}
	
	
	//############################
	//##### BEGIN TEST CALLS #####
	//############################
	// call tests for speed. Print out results
	private static void speed_test(){
		//make calls to all of the speed test methods which return a long indicating the amount of time the action took. These actions will allow me to isolate slow points in processing and attempt to fix them.
		//this will call individual methods instead of grouped together class methods. It will time everything and print the results.
		
		//model building speed
		long distribution_calculation_time = time_distribution_calculation();
		long key_distribution_calculation_time = time_key_distribution_calculation();
		long touch_probability_calculation_time = time_touch_probability_calculation();
		long chain_windows_calculation_time = time_chain_windows_calculation();
		long chain_tokens_calculation_time = time_chain_tokens_calculation();
		long overall_model_build_time = time_overall_model_build();

		//model compare speed
		long overall_model_compare_time = time_overall_model_compare();
		
		//output the results
		//building time results
		System.out.println("\noverall_model_build_time: "+overall_model_build_time);
		System.out.println("\tdistribution_calculation_time: "+distribution_calculation_time);
		System.out.println("\tkey_distribution_calculation_time: "+key_distribution_calculation_time);
		System.out.println("\ttouch_probability_calculation_time: "+touch_probability_calculation_time);
		System.out.println("\tchain_windows_calculation_time: "+chain_windows_calculation_time);
		System.out.println("\tchain_tokens_calculation_time: "+chain_tokens_calculation_time);
		
		//compare time results
		System.out.println("\noverall_model_compare_time: " + overall_model_compare_time);
		System.out.println("\t");
	}


	//call tests for correctness. Print out results
	private static void correctness_test(){
		//make calls to all of the classes to be tested
		boolean test_chain_success = test_chain();
		boolean test_distribution_success = test_distribution();
		boolean test_token_success = test_token();
		boolean test_touch_success = test_touch();
		boolean test_window_success = test_window();
		boolean test_chain_builder_success = test_chain_builder();
		boolean test_compare_chains_success = test_compare_chains();

		//print out errors in a readable format
		System.out.println("\nprogram overview error log:");

			if(!test_chain_success){
				System.out.println("\tchain fails");
			}
		
			if(!test_distribution_success){
				System.out.println("\tdistribution fails");
			}
	
			if(!test_token_success){
				System.out.println("\ttoken fails");
			}

			if(!test_touch_success){
				System.out.println("\ttouch fails");
			}
			
			if(!test_window_success){
				System.out.println("\twindow fails");
			}
			
			if(!test_chain_builder_success){
				System.out.println("\tchain_builder fails");
			}
			
			if(!test_compare_chains_success){
				System.out.println("\tcompare_chains fails");
			}
	}

	//###############################
	//###############################
	//#### Tests for correctness ####
	//###############################
	//###############################
	
	//############################
	//### classes to be tested ###
	//############################

	//# Chain class #
	private static boolean test_chain(){
		// call methods to test the functionality of the chain class
		boolean correct=true;

		//test functions in the distribution class
		boolean test_add_touch_success = test_add_touch();
		boolean test_get_touch_probability_success = test_get_touch_probability();
		boolean test_get_distribution_success = test_get_distribution();
		boolean test_get_key_distribution_success = test_get_key_distribution();
		boolean test_chain_compare_to_success = test_chain_compare_to();
		test_output_to_csv();
		
		//print out any errors in a readable way
		System.out.println("\nchain error log:");

		if(!test_add_touch_success){
			System.out.println("\tadd_touch fails");
			correct=false;
		}
		
		if(!test_get_touch_probability_success){
			System.out.println("\tget_touch_probability fails");
			correct=false;
		}

		if(!test_get_distribution_success){
			System.out.println("\tget_distribution fails");
			correct=false;
		}

		if(!test_get_key_distribution_success){
			System.out.println("\tget_key_distribution fails");
			correct=false;
		}
		
		if(!test_chain_compare_to_success){
			System.out.println("\tcompare_to fails");
			correct=false;
		}
		
		return correct;
	}

	
	//# Distribution class #
	private static boolean test_distribution(){
		boolean correct=true;

		//test functions in the distribution class
		boolean test_calc_min_success = test_calc_min();
		boolean test_calc_max_success = test_calc_max();
		boolean test_calc_average_success = test_calc_average();
		boolean test_calc_standard_deviation_success = test_calc_standard_deviation();
		
		//print out any errors in a readable way
		System.out.println("\ndistribution error log:");

		if(!test_calc_min_success){
			System.out.println("\tcalc_min fails");
			correct=false;
		}
		
		if(!test_calc_max_success){
			System.out.println("\tcalc_max fails");
			correct=false;
		}

		if(!test_calc_average_success){
			System.out.println("\tcalc_average fails");
			correct=false;
		}

		if(!test_calc_standard_deviation_success){
			System.out.println("\tcalc_standard_deviation fails");
			correct=false;
		}
		
		return correct;
	}

	
	//# Token class #
	private static boolean test_token(){
		boolean correct=true;

		//test functions in the token class
		boolean test_constructors_success = test_token_constructors();
		boolean test_contains_success = test_token_contains();
		boolean test_get_min_max_success = test_get_min_max();
		boolean test_token_equals_success = test_token_equals();
		
		//print out any errors in a readable way
		System.out.println("\ntoken error log:");

		if(!test_constructors_success){
			System.out.println("\ttoken constructors fails");
			correct=false;
		}
		
		if(!test_contains_success){
			System.out.println("\ttoken_contains fails");
			correct=false;
		}

		if(!test_get_min_max_success){
			System.out.println("\tget_min_max fails");
			correct=false;
		}

		if(!test_token_equals_success){
			System.out.println("\ttoken_equals fails");
			correct=false;
		}
		
		return correct;
	}

	
	//# Touch class #
	private static boolean test_touch(){
		// call methods to test the functionality of the chain class
		boolean correct=true;

		//test functions in the distribution class
		boolean test_set_probability_success = test_set_probability();
		boolean test_get_probability_success = test_get_probability();
		boolean test_touch_hashCode_success = test_touch_hashCode();
		boolean test_touch_compareTo_success = test_touch_compareTo();
		boolean test_compare_with_token_touch_success = test_compare_with_token_touch();
		
		//print out any errors in a readable way
		System.out.println("\ntouch error log:");

		if(!test_set_probability_success){
			System.out.println("\tset_probability fails");
			correct=false;
		}
		
		if(!test_get_probability_success){
			System.out.println("\tget_probability fails");
			correct=false;
		}
		
		if(!test_touch_hashCode_success){
			System.out.println("\thashCode fails");
			correct=false;
		}
		
		if(!test_touch_compareTo_success){
			System.out.println("\tcompareTo fails");
			correct=false;
		}
		
		if(!test_compare_with_token_touch_success){
			System.out.println("\tcompare_with_token fails");
			correct=false;
		}
		
		return correct;
	}


	//# Window class #
	private static boolean test_window(){
		//call methods to test the functionality of the chain class
		boolean correct=true;

		//test functions in the distribution class
		boolean test_window_compareTo_success = test_window_compareTo();
		boolean test_window_hashCode_success = test_window_hashCode();
		boolean test_window_compare_with_token_success = test_compare_with_token();
		
		//print out any errors in a readable way
		System.out.println("\nwindow error log:");

		if(!test_window_compare_with_token_success){
			System.out.println("\tcompare_with_token fails");
			correct=false;
		}
		
		if(!test_window_compareTo_success){
			System.out.println("\tcompareTo fails");
			correct=false;
		}
		
		if(!test_window_hashCode_success){
			System.out.println("\thashCode fails");
			correct=false;
		}
		
		return correct;
	}
	
	
	//# ChainBuilder class #
	private static boolean test_chain_builder(){
		// call methods to test the functionality of the chain class
		boolean correct=true;

		//test functions in the distribution class
		boolean test_handle_touch_success = test_handle_touch();
		boolean test_authenticate_success = test_authenticate();
		boolean test_build_chain_from_csv_success = test_build_chain_from_csv();
		
		//print out any errors in a readable way
		System.out.println("\nchainBuilder error log:");

		if(!test_handle_touch_success){
			System.out.println("\thandle_touch fails");
			correct=false;
		}
		
		if(!test_authenticate_success){
			System.out.println("\tauthenticate fails");
			correct=false;
		}
		
		if(!test_build_chain_from_csv_success){
			System.out.println("\tbuild_chain_from_csv fails");
			correct=false;
		}
		
		return correct;
	}
	
	
	//# CompareChains #
	private static boolean test_compare_chains(){
		//TODO call methods to test the functionality of the chain class
		boolean correct=true;

		//test functions in the distribution class
		boolean test_compare_chains_run_success = test_compare_chains_run();
		
		//print out any errors in a readable way
		System.out.println("\ncompareChains error log:");

		if(!test_compare_chains_run_success){
			System.out.println("\trun fails");
			correct=false;
		}
		
		return correct;
	}
	
	
	//############################
	//### methods to be tested ###
	//############################
	
	//TODO
	//# Chain class#
	private static boolean test_add_touch(){
		boolean correct;
		
		// window 2, tokens 10, threshold 500, size 5
		Chain chain = new Chain(2,10,500,5);
		
		//model size is 5. Add 10 touches to the model and see if the sliding is working correctly. The most rescent 5 touches should be retained.
		for(int i=0;i<10;i++){
			chain.add_touch(new Touch('a',.1*i,100*i));
		}
		
		Distribution dist = chain.get_distribution();
		// most rescent 5 touches are  pressure .9, .8, .7, .6, .5
		correct = (dist.get_max() == .9) && (dist.get_min()== .5);
		
		return correct;
	}
	
	
	private static boolean test_get_touch_probability(){
		boolean correct;
		
		// window 2, tokens 10, threshold 500, size 5
		Chain chain = new Chain(2,10,500,10);
		List<Touch> chain_touches = new ArrayList<Touch>();
		List<Touch> touches = new ArrayList<Touch>();
		Window window;
		
		//model size is 5. Add 10 touches to the model and see if the sliding is working correctly. The most rescent 5 touches should be retained.
		for(int i=0;i<10;i++){
			Touch touch = new Touch('a',.1*i,100);
			
			chain_touches.add(touch);
			chain.add_touch(touch);
		}
		
		// create a window and tests that the correct probability is retrieved afterward
		touches.add(chain_touches.get(0));
		touches.add(chain_touches.get(1));
		
		window = new Window(touches);
		double probability = chain.get_touch_probability(window, chain_touches.get(2));
		
		//System.out.println(probability);
		correct = (probability==1);
		//----------
		//TODO test if the correct probability is returned when the same window is succeeded by two unique touches
		Chain chain_2 = new Chain(2,10,500,10);
		List<Touch> chain_touches_2 = new ArrayList<Touch>();
		List<Touch> touches_2 = new ArrayList<Touch>();
		Window window_2;
		
		//model size is 5. Add 10 touches to the model and see if the sliding is working correctly. The most rescent 5 touches should be retained.
		for(int i=0;i<6;i++){
			Touch touch = new Touch('a',.1*i,100);
			
			chain_touches_2.add(touch);
			chain_2.add_touch(touch);
		}
		
		for(int i=0;i<3;i++){
			Touch touch = new Touch('a',.1*(i%2),100);
			
			chain_touches_2.add(touch);
			chain_2.add_touch(touch);
		}
		
		// create a window and tests that the correct probability is retrieved afterward
		touches_2.add(chain_touches_2.get(0));
		touches_2.add(chain_touches_2.get(1));
		
		window_2 = new Window(touches_2);
		double probability_2 = chain_2.get_touch_probability(window_2, chain_touches_2.get(2));
		
		//System.out.println(probability_2);
		correct = correct && (probability_2==.5);
		//----------
		//TODO test if the correct probability is returned when the same window is succeeded by two same touches
		Chain chain_3 = new Chain(2,10,500,10);
		List<Touch> chain_touches_3 = new ArrayList<Touch>();
		List<Touch> touches_3 = new ArrayList<Touch>();
		Window window_3;
		
		//model size is 5. Add 10 touches to the model and see if the sliding is working correctly. The most rescent 5 touches should be retained.
		for(int i=0;i<10;i++){
			//0,.1,.2,.3,.4,0,.1,.2,.3,.4
			Touch touch = new Touch('a',.1*(i%5),100);
			
			chain_touches_3.add(touch);
			chain_3.add_touch(touch);
		}
		
		// create a window and tests that the correct probability is retrieved afterward
		touches_3.add(chain_touches_3.get(0));
		touches_3.add(chain_touches_3.get(1));
		
		window_3 = new Window(touches_3);
		double probability_3 = chain_3.get_touch_probability(window_3, chain_touches_3.get(2));
		
		//System.out.println(probability_3);
		correct = correct && (probability_3==1);
		
		return correct;
	}
	
	
	private static boolean test_get_distribution(){
		// works per test_add_touch();
		return true;
	}
	
	
	private static boolean test_get_key_distribution(){
		boolean correct = true;
		
		// window 2, tokens 10, threshold 500, size 9
		Chain chain = new Chain(2,10,500,9);
		
		//model size is 5. Add 10 touches to the model and see if the sliding is working correctly. The most rescent 5 touches should be retained.
		for(int i=0;i<9;i++){
			//.1 though .9 pressures.... average is .5.... keycode is half a, half b
			chain.add_touch(new Touch('a'+(i%2),.1*(i+1),100*i));
		}
		
		//create distributions which should be created for the key_distribution
		Distribution a_key_dist;
		Distribution b_key_dist;
		
		List<Touch> a_touch_list = new ArrayList<Touch>();
		List<Touch> b_touch_list = new ArrayList<Touch>();
		
		//for 'a'
		for(int i=0;i<9;i+=2){
			a_touch_list.add(new Touch('a',.1*(i+1),100*i));
		}
		
		a_key_dist = new Distribution(a_touch_list, 'a');
		
		//for 'b'
		for(int i=1;i<9;i+=2){
			b_touch_list.add(new Touch('b',.1*(i+1),100*i));
		}
		
		b_key_dist = new Distribution(b_touch_list, 'b');
		
		List<Distribution> key_dist = chain.get_key_distribution();
		//each index of key_distribution contains a distribution object.
		//i need to determine if these distribution objects are correct
		//this will be based on the touches i have added to the chain
		if(key_dist.get(0).get_keycode()=='a'){
			correct = correct && key_dist.get(0).equals(a_key_dist);
			correct = correct && key_dist.get(1).equals(b_key_dist);
		}else{
			correct = correct && key_dist.get(1).equals(a_key_dist);
			correct = correct && key_dist.get(0).equals(b_key_dist);
		}
		
		return correct;
	}
	
	
	private static boolean test_chain_compare_to(){
		//TODO test this to a greater degree
		boolean correct = true;
		
		// window 2, tokens 10, threshold 500, size 10
		Chain base_chain = new Chain(2,10,500,10);
		Chain auth_chain = new Chain(2,10,500,10);
		
		// add touches to the chains
		for(int i=0;i<10;i++){
			base_chain.add_touch(new Touch('a',.1*(i),100));
			auth_chain.add_touch(new Touch('a',0,100));
		}
		
		// should be true; compare two chains which are the same
		correct = correct && (0==base_chain.compare_to(base_chain));
		
		// compare two chains which are different completly
		correct = correct && (1==base_chain.compare_to(auth_chain));
		//System.out.println(base_chain.compare_to(auth_chain));
		
		return correct;
	}
	
	
	private static boolean test_output_to_csv(){
		// window 2, tokens 10, threshold 500, size 9
		Chain chain = new Chain(5,10,500,1000);
		
		//model size is 5. Add 10 touches to the model and see if the sliding is working correctly. The most rescent 5 touches should be retained.
		for(int i=0;i<10000;i++){
			//.1 though .9 pressures.... average is .5.... keycode is half a, half b
			Touch touch =new Touch('a'+(i%2),.1*((i+1)%11),100);
			//System.out.println(touch);
			
			chain.add_touch(touch);
		}
		
		//cause all computations to happen
		chain.get_distribution();
		chain.get_key_distribution();
		chain.get_touch_probability(null, null);
		
		//output the chain to a csv file
		chain.output_to_csv();
		
		return true;
	}
	
	
	//# Distribution class #
	private static boolean test_calc_min(){
		boolean correct = false;	
		ArrayList<Touch> touches = new ArrayList<Touch>();
		Distribution dist;

		//create a List of touches with a minimum value
		touches.add(new Touch('a',.1, 100));
		touches.add(new Touch('a',.2, 200));
		touches.add(new Touch('a',.3, 300));
		touches.add(new Touch('a',.4, 400));
		touches.add(new Touch('a',.5, 500));
		touches.add(new Touch('a',.6, 600));
		touches.add(new Touch('a',.7, 700));
		touches.add(new Touch('a',.8, 800));
		touches.add(new Touch('a',.9, 900));

		
		//test to see that calc_min finds this value correctly
		dist = new Distribution(touches);

		if(dist.get_min()==.1){
			correct=true;
		}
		
		return correct;
	}


	private static boolean test_calc_max(){
		boolean correct = false;	
		ArrayList<Touch> touches = new ArrayList<Touch>();
		Distribution dist;

		//create a List of touches with a minimum value
		touches.add(new Touch('a',.1, 100));
		touches.add(new Touch('a',.2, 200));
		touches.add(new Touch('a',.3, 300));
		touches.add(new Touch('a',.4, 400));
		touches.add(new Touch('a',.5, 500));
		touches.add(new Touch('a',.6, 600));
		touches.add(new Touch('a',.7, 700));
		touches.add(new Touch('a',.8, 800));
		touches.add(new Touch('a',.9, 900));
		
		//test to see that calc_min finds this value correctly
		dist = new Distribution(touches);

		if(dist.get_max()==.9){
			correct=true;
		}
		
		return correct;
	}


	private static boolean test_calc_average(){
		boolean correct = false;	
		ArrayList<Touch> touches = new ArrayList<Touch>();
		Distribution dist;

		//create a List of touches with a minimum value
		touches.add(new Touch('a',.1, 100));
		touches.add(new Touch('a',.2, 200));
		touches.add(new Touch('a',.3, 300));
		touches.add(new Touch('a',.4, 400));
		touches.add(new Touch('a',.5, 500));
		touches.add(new Touch('a',.6, 600));
		touches.add(new Touch('a',.7, 700));
		touches.add(new Touch('a',.8, 800));
		touches.add(new Touch('a',.9, 900));

		
		//test to see that calc_min finds this value correctly
		dist = new Distribution(touches);

		if(dist.get_average()==.5){
			correct=true;
		}
		
		return correct;
	}


	private static boolean test_calc_standard_deviation(){
		boolean correct = false;	
		ArrayList<Touch> touches = new ArrayList<Touch>();
		Distribution dist;

		//create a List of touches with a minimum value
		touches.add(new Touch('a',.1, 100));
		touches.add(new Touch('a',.2, 200));
		touches.add(new Touch('a',.3, 300));
		touches.add(new Touch('a',.4, 400));
		touches.add(new Touch('a',.5, 500));
		touches.add(new Touch('a',.6, 600));
		touches.add(new Touch('a',.7, 700));
		touches.add(new Touch('a',.8, 800));
		touches.add(new Touch('a',.9, 900));

		
		//test to see that calc_min finds this value correctly
		dist = new Distribution(touches);

		//actual value should be around .2582
		if(dist.get_standard_deviation()>(.258)&&dist.get_standard_deviation()<(.2585)){
			correct=true;
		}
		
		return correct;
	}
	

	//# Token class #
	private static boolean test_token_constructors(){
		boolean correct = true;
		
		//create a distribution to be used in the construction of tokens
		ArrayList<Touch> touches = new ArrayList<Touch>();
		Distribution dist;

		//create a List of touches with a minimum value
		touches.add(new Touch('a', 0, 0));
		touches.add(new Touch('a',.1, 100));
		touches.add(new Touch('a',.2, 200));
		touches.add(new Touch('a',.3, 300));
		touches.add(new Touch('a',.4, 400));
		touches.add(new Touch('a',.5, 500));
		touches.add(new Touch('a',.6, 600));
		touches.add(new Touch('a',.7, 700));
		touches.add(new Touch('a',.8, 800));
		touches.add(new Touch('a',.9, 900));
		touches.add(new Touch('a', 1, 1000));

		
		//test to see that calc_min finds this value correctly
		dist = new Distribution(touches);
		
		//make a call to Token() constructors. Test one of them for correctness. Test the rest for equality to the fist.
		ArrayList<Token> tokens_0 = new ArrayList<Token>();
		ArrayList<Token> tokens_1 = new ArrayList<Token>();
		ArrayList<Token> tokens_2 = new ArrayList<Token>();
		
		ArrayList<Token> tokens_3 = new ArrayList<Token>();
		ArrayList<Token> tokens_4 = new ArrayList<Token>();
		ArrayList<Token> tokens_5 = new ArrayList<Token>();
		
		//create 10 tokens over the range 0 to 1 using the three different constructors.
		for(int i=0;i<10;i++){
			tokens_0.add(new Token(dist, 10, i, 2, Token.Type.linear)); // this one should necessarily be differant from the other two
			tokens_1.add(new Token(dist, 10, i, Token.Type.linear));
			tokens_2.add(new Token(0, 1, 10, i, Token.Type.linear));
			
			tokens_3.add(new Token(dist, 10, i, 2, Token.Type.keycode_mu)); // this one should necessarily be differant from the other two
			tokens_4.add(new Token(dist, 10, i, Token.Type.keycode_mu));
			tokens_5.add(new Token(0, 1, 10, i, Token.Type.keycode_mu));
		}
		
		//check for the correctness of each of the token sets
		boolean token_0_incorrect = false;
		boolean token_1_incorrect = false;
		boolean token_2_incorrect = false;
		double sigma = dist.get_standard_deviation();
		
		//test the linear tokenset
		for(int i=0;i<10;i++){
			//test each of the token sets for correctness
			//System.out.print(((.4*sigma*i)+(dist.get_average()-2*sigma)));
			//System.out.println("\t"+tokens_0.get(i).get_min());
			
			//TODO make sure this is a valid test
			token_0_incorrect = !within_episilon(tokens_0.get(i).get_min(), ((.4*sigma*(i+1))+dist.get_average()-2*sigma), .01);
			token_1_incorrect = !(tokens_1.get(i).get_min()==(.1*i));
			token_2_incorrect = !(tokens_2.get(i).get_min()==(.1*i));
			
			if(	token_0_incorrect ||
				token_1_incorrect ||
				token_2_incorrect){
				correct = false;
			}
		}
		
		//TODO test the keycode_mu token set
		
		return correct;
	}
	
	
	private static boolean test_token_contains(){
		boolean correct = true;
		
		//create a distribution to be used in the construction of tokens
		ArrayList<Touch> touches = new ArrayList<Touch>();
		Distribution dist;

		//create a List of touches with a minimum value
		touches.add(new Touch('a', 0, 0));
		touches.add(new Touch('a',.1, 100));
		touches.add(new Touch('a',.2, 200));
		touches.add(new Touch('a',.3, 300));
		touches.add(new Touch('a',.4, 400));
		touches.add(new Touch('a',.5, 500));
		touches.add(new Touch('a',.6, 600));
		touches.add(new Touch('a',.7, 700));
		touches.add(new Touch('a',.8, 800));
		touches.add(new Touch('a',.9, 900));
		touches.add(new Touch('a', 1, 1000));

		
		//test to see that calc_min finds this value correctly
		dist = new Distribution(touches);
		
		//make a call to Token() constructors. Test one of them for correctness. Test the rest for equality to the fist.
		ArrayList<Token> tokens_2 = new ArrayList<Token>();
		
		//create 10 tokens over the range 0 to 1 using the three different constructors.
		for(int i=0;i<10;i++){
			tokens_2.add(new Token(0, 1, 10, i, Token.Type.linear));
		}
		
		//check for the correctness of each of the token sets
		boolean token_2_incorrect;
		
		for(int i=0;i<10;i++){
			//test each of the token sets for correctness
			Touch t = new Touch('a',.5+i, 0);
			
			token_2_incorrect = (tokens_2.get(i).contains(t));
			
			if(token_2_incorrect){
				correct = false;
			}
		}
		
		return correct;
	}
	
	
	private static boolean test_get_min_max(){
		boolean correct;
		
		Token t = new Token(0, 1, 10, 0, Token.Type.linear);
		
		//System.out.println(t.get_min());
		//System.out.println(t.get_max());
		correct = t.get_max() == .1;
		correct = correct && t.get_min()==0;
		
		return correct;
	}
	
	
	private static boolean test_token_equals(){
		boolean correct;
		
		Token t_1 = new Token(0, 1, 10, 0, Token.Type.linear);
		Token t_2 = new Token(0, 1, 10, 0, Token.Type.linear);
		Token t_3 = new Token(0, 1, 10, 5, Token.Type.linear);
		
		correct = t_1.equals(t_2);
		correct = correct && !t_3.equals(t_2);
		
		
		return correct;
	}

	
	//# Touch class #
	private static boolean test_set_probability(){
		boolean correct;
		
		Touch touch = new Touch('a', .5, 100);
		Window window = new Window(new ArrayList<Touch>());
		
		touch.set_probability(window, .2);
		correct = touch.get_probability(window)==.2;
		
		return correct;
	}
	
	
	private static boolean test_get_probability(){
		boolean correct;
		
		Touch touch = new Touch('a', .5, 100);
		Window window = new Window(new ArrayList<Touch>());
		
		List<Touch> touches = new ArrayList<Touch>();
		touches.add(touch);
		
		touch.set_probability(window, .2);
		//test with a null window
		correct = touch.get_probability(window)==.2;
		
		//test that a window not set returns 0
		correct = correct && (0==touch.get_probability(new Window(touches)));
		
		//test that a window can be set and gotten if the window is not null
		touch.set_probability(new Window(touches),1);
		correct = correct && (1==touch.get_probability(new Window(touches)));
		
		return correct;
	}
	
	
	private static boolean test_touch_hashCode(){
		//TODO
		boolean correct;
		
		Touch touch = new Touch('a', .5, 100);
		Window window = new Window(new ArrayList<Touch>());
		
		touch.set_probability(window, .2);
		correct = (touch.hashCode() > 0);
		
		return correct;
	}
	
	
	private static boolean test_touch_compareTo(){
		boolean correct;
		
		Touch touch_0 = new Touch('a', .5, 100);
		Touch touch_1 = new Touch('a', .5, 100);
		Touch touch_2 = new Touch('b', .5, 100);
		Touch touch_3 = new Touch('a', .6, 100);
		Touch touch_4 = new Touch('a', .5, 200);
		
		Window window = new Window(new ArrayList<Touch>());
		
		List<Touch> touches = new ArrayList<Touch>();
		touches.add(touch_0);
		
		//get the touches for equality
		correct = touch_0.compareTo(touch_1) == 0; // are they equal?
		
		//change a touch
		touch_0.set_probability(window, .2);
		
		//are they still equal? they shouldn't be.
		correct = correct && (touch_0.compareTo(touch_1) > 0);
	
		//are the touches origionally not equal, still not equal?
		correct = correct && (touch_1.compareTo(touch_2) > 0);
		correct = correct && (touch_1.compareTo(touch_3) > 0);
		correct = correct && (touch_1.compareTo(touch_4) > 0);

		return correct;
	}
	
	
	private static boolean test_compare_with_token_touch(){
		boolean correct=true;
		Touch base_touch = new Touch('a', .55, 100);
		Touch auth_touch_1 = new Touch('b', .58, 100);
		Touch auth_touch_2 = new Touch('c', .62, 100);
		List<Token> tokens = new ArrayList<Token>();
		
		for(int i=0;i<10;i++){
			tokens.add(new Token(0,1,10,i,Token.Type.linear));
		}
		
		//should be true; compare to a touch in the same token
		correct = correct && base_touch.compare_with_token(tokens, auth_touch_1);
				
		//should be false; compare to a touch in a different token
		correct = correct && !base_touch.compare_with_token(tokens, auth_touch_2);
		
		return correct;
	}
	
	
	//# Window class #
	private static boolean test_compare_with_token(){
		boolean correct=true;
		List<Touch> base_touches = new ArrayList<Touch>();
		List<Touch> auth_touches_1 = new ArrayList<Touch>();
		List<Touch> auth_touches_2 = new ArrayList<Touch>();
		
		for(int i=0;i<3;i++){
			base_touches.add(new Touch('c', .41, 100));
			auth_touches_1.add(new Touch('c', .49, 100));
			auth_touches_2.add(new Touch('c', .62, 100));
		}
		
		Window base_window = new Window(base_touches);
		Window auth_window_1 = new Window(auth_touches_1);
		Window auth_window_2 = new Window(auth_touches_2);
		
		List<Token> tokens = new ArrayList<Token>();
		
		for(int i=0;i<10;i++){
			tokens.add(new Token(0,1,10,i,Token.Type.linear));
		}
		
		//should be true; compare to a window containing touches in the same token
		correct = correct && base_window.compare_with_token(tokens, auth_window_1);
				
		//should be false; compare to a window containing touches in a different token
		correct = correct && !base_window.compare_with_token(tokens, auth_window_2);
		
		return correct;
	}
	
	
	private static boolean test_window_compareTo(){
		boolean correct;
		
		ArrayList<Touch> touches_0 = new ArrayList<Touch>();
		ArrayList<Touch> touches_1 = new ArrayList<Touch>();
		ArrayList<Touch> touches_2 = new ArrayList<Touch>();
		
		Touch touch_1 = new Touch('a', 0, 0);
		Touch touch_2 = new Touch('a',.1, 100);
		Touch touch_3 = new Touch('a',.2, 200);
		Touch touch_4 = new Touch('b',.1, 300);
		
		touches_0.add(touch_1);
		touches_0.add(touch_2);
		touches_0.add(touch_3);
		
		touches_1.add(touch_1);
		touches_1.add(touch_2);
		touches_1.add(touch_3);
		
		touches_2.add(touch_1);
		touches_2.add(touch_2);
		touches_2.add(touch_4);
		
		Window window_0 = new Window(touches_0);
		Window window_1 = new Window(touches_0); 
		Window window_2 = new Window(touches_1);
		Window window_3 = new Window(touches_2);
		Window window_4 = new Window(new ArrayList<Touch>());
		
		//compare two windows created with the same touch list
		correct = window_0.compareTo(window_1) == 0;
		//compare two windows created with equal, but different touch lists. Should still be the same
		correct = correct && window_0.compareTo(window_2) == 0;
		//compare two windows created with unequal touch lists. They should be unequal
		correct = correct && window_0.compareTo(window_3) > 0;
		//compare two windows of differing size. They should not be equal
		correct = correct && window_0.compareTo(window_4) > 0;
		
		return correct;
	}
	
	
	private static boolean test_window_hashCode(){
		//TODO
		return true;
	}
	

	//# ChainBuilder class
	private static boolean test_handle_touch(){
		boolean correct = true;
		
		ChainBuilder chain_builder = new ChainBuilder();
		
		try{
			for(int i=0;i<10000;i++){
				chain_builder.handle_touch(new Touch('a',1.0/i,100));
			}
		}catch(Exception e){
			e.printStackTrace();
			correct=false;
		}
		
		return correct;
	}
	
	
	private static boolean test_authenticate(){
		//TODO none of these will actually work yet because compare is not implemented. Right now I am more testing whether this causes an error that needs to be taken care of.
		boolean correct;
		
		//window, token, threshold, user_model_size, auth_model_size, 
		ChainBuilder chain_builder = new ChainBuilder(5,5,1000,1000,500);
		
		//try to authenticate two chains which are the same
		for(int i=0;i<3000;i++){
			chain_builder.handle_touch(new Touch('a',1.0/i,100));
		}
		
		chain_builder.authenticate();
		//wait for the authentication to finish
		while(chain_builder.get_authenticate_state()==ChainBuilder.State.IN_PROGRESS){
			try{
				Thread.sleep(100);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		correct = (chain_builder.get_authenticate_state()==ChainBuilder.State.SUCCESS);
		
		//TODO try to authenticate two chains which are different
		
		
		//TODO try to authenticate two chains who's differance should amount to just above the authentication threshold
		
		
		//TODO try to authenticate two chains who's differance should amount to just below the authentication threshold
		
		
		return correct;
	}
	
	
	private static boolean test_build_chain_from_csv(){
		//TODO
		return true;
	}
	
	
	//# CompareChains class #
	private static boolean test_compare_chains_run(){
		//TODO in testing chain_builder, this method is also tested in authenticate
		
		return true;
	}

	
	//#########################
	//#########################
	//#### Tests for speed ####
	//#########################
	//#########################
	// run the important, or time consuming methods for each class, and time them
	
	//TODO
	//# Chain class#
	private static long time_distribution_calculation(){
		Chain chain = create_chain(SPEED_TEST_CHAIN_SIZE);
		
		long start_time = System.currentTimeMillis();
		//do the method
		chain.get_distribution();
		long end_time = System.currentTimeMillis();

		return end_time-start_time;
	}
	
	
	private static long time_key_distribution_calculation(){
		Chain chain = create_chain(SPEED_TEST_CHAIN_SIZE);
		
		long start_time = System.currentTimeMillis();
		//do the method
		chain.get_key_distribution();
		long end_time = System.currentTimeMillis();

		return end_time-start_time;
	}
	
	
	private static long time_touch_probability_calculation(){
		Chain chain = create_chain(SPEED_TEST_CHAIN_SIZE);
		Window window;
		List<Touch> touches = new ArrayList<Touch>();
		
		for(int i=0;i<SPEED_TEST_WINDOW_SIZE;i++){
			touches.add(new Touch('a',.5,100));
		}
		
		window=new Window(touches);
		
		long start_time = System.currentTimeMillis();
		//do the method
		chain.get_touch_probability(window, touches.get(0));
		long end_time = System.currentTimeMillis();

		return end_time-start_time;
	}
	
	
	private static long time_chain_windows_calculation(){
		Chain chain = create_chain(SPEED_TEST_CHAIN_SIZE);
		
		long start_time = System.currentTimeMillis();
		//do the method
		chain.get_windows();
		long end_time = System.currentTimeMillis();

		return end_time-start_time;
	}
	
	
	private static long time_chain_tokens_calculation(){
		Chain chain = create_chain(SPEED_TEST_CHAIN_SIZE);
		
		long start_time = System.currentTimeMillis();
		//do the method
		chain.get_tokens();
		long end_time = System.currentTimeMillis();

		return end_time-start_time;
	}
	
	
	private static long time_overall_model_build(){
		Chain chain = create_chain(SPEED_TEST_CHAIN_SIZE);
		Window window;
		List<Touch> touches = new ArrayList<Touch>();
		
		for(int i=0;i<SPEED_TEST_WINDOW_SIZE;i++){
			touches.add(new Touch('a',.5,100));
		}
		
		window=new Window(touches);
		
		long start_time = System.currentTimeMillis();
		//do the method
		chain.get_distribution();
		chain.get_key_distribution();
		chain.get_key_distribution();
		chain.get_touch_probability(window, touches.get(0));
		chain.get_windows();
		chain.get_tokens();
		long end_time = System.currentTimeMillis();

		return end_time-start_time;
	}
	
	//TODO
	//# Distribution class #
	
	
	//TODO
	//# Token class #
		

	//TODO
	//# Touch class #

		
	//TODO
	//# Window class #


	//TODO
	//# ChainBuilder class
		
		
	//TODO
	//# CompareChains class #	
	private static long time_overall_model_compare(){
		Chain base_chain = create_chain(SPEED_TEST_CHAIN_SIZE);
		Chain auth_chain = create_chain(SPEED_TEST_CHAIN_SIZE);
		
		CompareChains cc = new CompareChains(base_chain, auth_chain);
		Thread thread = new Thread(cc);
		
		long start_time = System.currentTimeMillis();
		//do the method
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long end_time = System.currentTimeMillis();

		return end_time-start_time;
	}
	
	
	//This is a generic method for testing the time of something
	private static long test_time(){
		//initialize things
		ChainBuilder chain_builder = new ChainBuilder();

		//initialize
		//chain_builder

		long start_time = System.currentTimeMillis();
		//do the method
		long end_time = System.currentTimeMillis();

		return end_time-start_time;
	}


	//###################
	//###################
	//#### Utilities ####
	//###################
	//###################
	//there are useful across all test cases
	
	///creates a chain of the specified size
	private static Chain create_chain(int size){
		Chain chain = new Chain(SPEED_TEST_WINDOW_SIZE, SPEED_TEST_TOKEN_SIZE, SPEED_TEST_THRESHOLD, size);
		
		//add touches to the chain
		for(int i=0;i<size;i++){
			chain.add_touch(new Touch('a', (i%11)*.1, 100));
		}
		
		return chain;
	}
	
	
	///determines whether a is within episilon of b. true if a is within episilon of b.
	private static boolean within_episilon(double a, double b, double episilon){
		return ((a>(b-episilon)) && (a<(b+episilon)));
	}
}