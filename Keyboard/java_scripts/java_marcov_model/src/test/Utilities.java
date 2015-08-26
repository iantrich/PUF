package test;
import components.Chain;
import components.Distribution;
import components.Touch;


///This file contains utilities for building the model and working with directories. I will prioritize the ones related to building the model
public class Utilities{
	///get the current working directory
	public static String get_current_dir(){
	}


	///list directories in a given path
	public static ArrayList<Path> list_dirs(Path path){
	}


	///list files in a given path
	public static ArrayList<File> list_files(path){
	}


	///print selections as a list
	public static void print_selections(){
	}


	///preforms input validation making sure the user input is an integer within the valid range
	public static int grab_valid_input(List<String> options){
	}


	///create directories to make the absolute path valid in the file system. Will not create directories if they are already present
	public static void create_dir_path(Path path){
	}


	/*
	########################################################
	# @param(keycode): integer
	# @param(pressure): float
	# @param(distribution): List of distributions
	# containing a dictionary in
	# each index with keys 'lower' and 'upper'
	# @param(keycode_dist): List of distributions for each
	# keycode with a dictionary in each index with keys
	# 'lower', 'upper', 'mean' and 'std'
	#
	# Calculate a number representing the index that the given
	# pressure value for the given keycode falls within
	# and -1 if not
	#
	# @return:
	########################################################
	*/
	public static void normalize_raw_element(char keycode, double pressure,List<Distribution> distribution, List<Distribution> keycode_dist){
		//TODO check for correctness
		// the keycode_dist should contain keycodes at the index based on their ascii character value
		//determine if the pressure value is out of range for this key	
		if(pressure < keycode_dist.get(keycode).get_min() || pressure > keycode_dist.get(keycode).get_max()){
			return -1;
		}

		pressure = keycode_dist.get(keycode).get_average();
		for(int i=0;i<distribution.size();i++){
			Distribution d;
			d = distribution.get(i);

			if((pressure >= d.get_min()) && (pressure < c)){
				return i;
			}
			if(pressure < d.get(0).get_min(){
				return -1;
			}
		}

		return distribution.size()-1;
	}


	/*
	########################################################
	# @param(hashcode_bin): Dictionary value at the hashcode
	# which is a Dictionary with keys 'chain' and 'total'
	# @param(current_window): List of List touches with
	# time as a Long in touch[0] and pressure as a Float in
	# touch[1]
	#
	# Search the hashcode_bin for the current_window sequence
	#
	# @return: Integer representing the index in the chain
	# that matches the current_window sequence or -1 if not
	# found in the chain
	########################################################
	*/
	public static int match_sequence(Chain hashcode_bin, List<Touch> current_window){
		//TODO finish implementing
		if(hashcode_bin==null){
			return -1;
		}

		current_sequence = new ArrayList<Touch>();
		//for(int i=0;i<
	}


	/*
	########################################################
	# @param(hashcode): Integer that represents the hashed value of
	# the given current_window
	# @param(hashcode_bin): Dictionary value at the hashcode
	# which is a Dictionary with keys 'chain' and 'total'
	# @param(link_index): Integer representing an index into
	# the List of sequences
	# @param(current_window): List of List touches with
	# time as a Long in touch[0] and pressure as a Float in
	# touch[1]
	# @param(table): A hash table of Dictionaries with keys
	# 'chain' and 'total'
	#
	# Given the index of the node in the bin, increment the value
	# of current_window[len(current_window) - 1]
	#
	# @return: Updated hash table
	########################################################
	*/
	public static void increment_probability(int hashcode, Chain hashcode_bin, int link_index, List<Touch> current_window, List<Chain> table){
	//TODO
	}


	/*
	########################################################
	# @param(hashcode): Integer that represents the hashed value of
	# the given current_window
	# @param(hashcode_bin): Dictionary value at the hashcode
	# which is a Dictionary with keys 'chain' and 'total'
	# @param(current_window): List of List touches with
	# time as a Long in touch[0] and pressure as a Float in
	# touch[1]
	# @param(table): A hash table of Dictionaries with keys
	# 'chain' and 'total'
	# @param(token): Integer representing the number of tokens
	# used in this Markov Model distribution
	#
	# # Add link to bin with {'sequence': current_window[0...n-1]
	# and current_window[n-1]: 1}
	#
	# @return: Updated hash table
	########################################################
	*/
	public static void add_link(int hashcode, Chain hashcode_bin, List<Touch> current_window, List<Chain> table, int token){
		//TODO
	
	}


	/*
	########################################################
	# @param(hashcode): Integer that represents the hashed value of
	# the given current_window
	# @param(current_window): List of List touches with
	# time as a Long in touch[0] and pressure as a Float in
	# touch[1]
	# @param(table): A hash table of Dictionaries with keys
	# 'chain' and 'total'
	# @param(token): Integer representing the number of tokens
	# used in this Markov Model distribution
	#
	# # Add a bin with {'chain': [{'sequence': current_window[0...n-1]
	# and current_window[n-1]: 1}]}
	#
	# @return: Updated hash table
	########################################################
	*/
	public static void add_key(int hashcode, List<Touch> current_window, List<Chain> table, int token){
	//TODO
	}


	/*
	########################################################
	# @param(hashcode_bin): Dictionary value at the hashcode
	# which is a Dictionary with keys 'chain' and 'total'
	# @param(current_window): List of List touches with
	# time as a Long in touch[0] and pressure as a Float in
	# touch[1]
	# @param(link_index): Integer representing an index into
	# the List of sequences
	#
	# Get the probability of a touch after a given sequence
	#
	# @return:
	########################################################
	*/
	public static void touch_probability(Chain hashcode_bin, List<Touch> current_window, int link_index){
	//TODO
	}


	/*
	########################################################
	# @param(table): A hash table of Dictionaries with keys
	# 'chain' and 'total'
	#
	# Convert the raw Markov Model of total to probabilities
	#
	# @return: Updated hash table
	########################################################
	*/
	public static void convert_table_to_probabilities(List<Chain> table){
	//TODO
	}


	/*
	########################################################
	# @param(read): A csv file reader
	# @param(model_size): Size of Markov model to create
	#
	# Find the max and min pressure in a set of touches
	#
	# @return: Dictionary with keys 'max' and 'min'
	########################################################
	*/
	public static Distribution find_max_min(List<Touch> read, int model_size){
	//TODO
	}


	/*
	########################################################
	# @param(raw_data_file): String representing an absolute
	# file path to the file
	# @param(token): Integer representing the number of tokens
	# used in this Markov Model distribution
	# @param(model_size): Size of Markov model to create
	#
	# Generate a distribution of equally spaced tokens limited
	# by the max and min of the touch pressures as well as a
	# distribution of average and standard deviation for each
	# keycode present in the set of touches
	#
	# @return: A List with a List for the overall
	# distribution in index 0 and the overall keycode
	# distribution in index 1
	########################################################
	*/
	public static Distribution[] cluster_algorithm(File raw_data_file, int token, int model_size){
	//TODO
	}


	/*
	########################################################
	# @param(reader): A csv file reader
	# @param(model_size): Size of Markov model to create
	#
	# Generate a keycode distribution based on a set of touches
	# with a mean, standard deviation, lower bound and upper bound
	#
	# @return: A List representing the overall keycode distribution
	########################################################
	*/
	public static Distriubtion keycode_distribution(List<Touch> reader, int model_size){
	//TODO
	}


	/*
	########################################################
	# @param(raw_data_file): String representing an absolute
	# file path to the file
	# @param(table): A hash table of Dictionaries with keys
	# 'chain' and 'total'
	# @param(distribution): A List with a List for the overall
	# base distribution in index 0 and the overall keycode
	# distribution in index 1
	# @param(window): Integer representing the window size
	# @param(threshold): Integer representing the time threshold
	# @param(token): Integer representing the number of tokens
	# used in this Markov Model distribution
	# @param(model_size): Size of Markov model to create
	#
	# Generate a Markov Model
	#
	# @return: Hash table that represents the Markov Model
	########################################################
	*/
	public static void build_lookup(File raw_data_file, List<Chain> table, Distribution distribution, int window, int threshold, int token, int model_size){
	//TODO
	}


	/*
	########################################################
	# @param(raw_data_file): String representing an absolute
	# file path to the file
	# @param(base_table): A hash table of Dictionaries with keys
	# 'chain' and 'total'
	# @param(distribution): A List with a List for the overall
	# base distribution in index 0 and the overall keycode
	# distribution in index 1
	# @param(window): Integer representing the window size
	# @param(threshold): Integer representing the time threshold
	# @param(token): Integer representing the number of tokens
	# used in this Markov Model distribution
	# @param(n): Markov Model size for new data
	#
	# Generate a moving window Markov Model that is compared to
	# a base Markov Model
	#
	# @return: List of probabilities for each compare iteration
	########################################################
	*/
	public static void build_auth_table(File raw_data_file, List<Chain> base_table, Distribution distribution, int window, int threshold, int token, int n){
	//TODO
	}


	/*
	########################################################
	# @param(base): A hash table of Dictionaries with keys
	# 'chain' and 'total'
	# @param(auth): A hash table of Dictionaries with keys
	# 'chain' and 'total'
	#
	# Determine the difference in the base and auth Markov Models
	#
	# @return: List of sum of probabilities in index 0 and
	# number of unique sequences in index 1
	########################################################
	*/
	public static double compare(List<Chain> base, List<Chain> auth){
	//TODO
	}


	/*
	########################################################
	# @param(auth): A hash table of Dictionaries with keys
	# 'chain' and 'total'
	# @param(base): A hash table of Dictionaries with keys
	# 'chain' and 'total'
	# @param(current): List of List touches with
	# time as a Long in touch[0] and pressure as a Float in
	# touch[1]
	#
	# Get the difference in probability of current of base - auth
	#
	# @return: Decimal of precision 4
	########################################################
	*/
	public static double get_probability(List<Chain> auth, List<Chain> base, List<Touch>){
	//TODO
	}


	/*
	########################################################
	# @param(table): A hash table of Dictionaries with keys
	# 'chain' and 'total'
	# @param(current): List of List touches with
	# time as a Long in touch[0] and pressure as a Float in
	# touch[1]
	#
	# Remove the current sequence from the table
	#
	# @return: Return the updated hash table
	########################################################
	*/
	public static void remove_oldest(Chain hashcode_bin, List<Touch> current){
	//TODO
	}


	/*
	########################################################
	# @param(base_sequence): List of touches
	# @param(auth_chain): List of sequences
	#
	# Given a list of touches, find sequence in the auth_chain
	#
	# @return: Return the index of the matching sequence in
	# auth_chain and -1 if not found
	########################################################
	*/
	public static int find_sequence(List<Touch> base_sequence, List<List<Touch>> auth_chain){
	//TODO
	}


	//origionally from the build_lookup_talbe script
	/*
	# This script will take the CSV file from the keyboard and build a lookup table for the Markov model of probabilities
	# Algorithm
	# 1. Make new array of [times][normalized_values]
	# 2. Iterate through new array and find valid [window + next touch] sizes that fall within time threshold
	# 3. Use hash function to create a key: key = key * 31 + val for the actual [window]
	# 4. If key exists, see if sequence matches current sequence
	# 5.    If sequence matches, increment probability for [next touch]
	# 6.    Else, add go to next link for key and see if sequence matches
	# 7.    If no matches to sequence, add a new link for the key with {[sequence]{next: 1}}
	# 8. Else, add a new key with a link of {[sequence]{next: 1}}
	*/
	public static List<Table> build_table(File raw_data_path){
	//TODO
	}
}
