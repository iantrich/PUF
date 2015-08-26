package components;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

///TODO make the chain's compare_to method be able to update incrementally
///TODO make sure to use get_XXXXXX() instead of the instance variables
///TODO put windows into a Trie data structure for building model faster
///TODO anywhere where I need to compare windows, or Touches I need the option to do this with tokens
///there needs to be a way to set the distribution used for a chain. This is because the authentication chain is evaluated with the distribution of the base chain.
///compute the windows somewhere. This will be based on the threshold, window, token sizes. This may change distributions? if a touch is thrown out?
///this class represents the marcov chain. It contains a sequence of touches and a distribution. I avoid doing any processing on touch being added because eventually this will be called on key press in android. Setting it up this way is more flexible to in the sense that processing may be done at any time.
///caches the result of each computation so it does not have to be repeated.
public class Chain{
	private final Token.Type TOKEN_TYPE = Token.Type.linear;
	
	private Distribution distribution;
	private List<Distribution> key_distribution;
	
	private List<Token> tokens; // tokens into which the range is split
	private List<Touch> touches; // stores a list of all touch objects
	private List<Window> windows; // this seems redundtant at first, but is necessary because a window is not necessarily touch[i,..,i+window]. Factored in are the timestamps associated with each touch.
	private List<Touch> successor_touch; //keep a list of touches that come after windows at the same index

	private int window;
	private int token;
	private int threshold;
	private int model_size;

	private boolean distribution_computed;
	private boolean probability_computed;
	private boolean key_distribution_computed;
	private boolean windows_computed;
	private boolean tokens_computed;

	public Chain(int window, int token, int threshold, int model_size){	
		this.key_distribution = new ArrayList<Distribution>();

		this.tokens = new ArrayList<Token>();
		this.touches = new ArrayList<Touch>();
		this.windows = new ArrayList<Window>();
		this.successor_touch = new ArrayList<Touch>();

		this.window = window;
		this.token = token;
		this.threshold = threshold;
		this.model_size = model_size;

		on_model_update();
	}

	
	///copy constructor. New chain object should have the same state as the old with differant object references.
	//TODO check copy constructors for correctness
	public Chain(Chain c){
		this.key_distribution = new ArrayList<Distribution>(c.key_distribution);
		this.distribution = new Distribution(c.distribution);

		this.tokens = new ArrayList<Token>(c.tokens);
		this.touches = new ArrayList<Touch>(c.touches);
		this.windows = new ArrayList<Window>(c.windows);
		this.successor_touch = new ArrayList<Touch>(c.successor_touch);
		
		this.window = c.window;
		this.token = c.token;
		this.threshold = c.threshold;
		this.model_size = c.model_size;
	
		this.distribution_computed = c.distribution_computed;
		this.probability_computed = c.probability_computed;
		this.key_distribution_computed = c.key_distribution_computed;
		this.model_size = c.model_size;
		this.tokens_computed = c.tokens_computed;
	}


	public void add_touch(Touch touch){
		//TODO check for correctness		
		// handle sliding of the model if adding this touch brings us beyond model_size
		touches.add(touch);

		if(touches.size() > model_size){
			touches.remove(0);
		}

		on_model_update();
	}


	public void add_touch_list(List<Touch> t){
		//TODO check for correctness
		Iterator<Touch> touch_iterator = t.iterator();

		while(touch_iterator.hasNext()){		
			add_touch(touch_iterator.next());
		}
	}


	///allows distribution to be set. If no distribution is set, the distribution for this chain of touches is computed.
	///NOTE the distribution is not maintained when new touches are added.
	public void set_distribution(Distribution distribution, List<Distribution> key_distribution){
		this.distribution = distribution;
		this.key_distribution = key_distribution;
		
		distribution_computed=true;
		key_distribution_computed=true;
		
		//will need to recompute the tokens because they depend on the distribution
		tokens_computed=false;
	}
	
	
	///returns the probability of a given touch (at the i'th index) based on the model. This will depend on the preceeding touches, in Window. A request for one probability will necessarily result in all of the probabilities being computed.
	public double get_touch_probability(Window w, Touch t){
		//TODO check for correctness
		//if the probability has not been computed, compute it
		if(!probability_computed){
			compute_probability();
			probability_computed=true;
		}
				
		if(touches.size() == 0){
			return 0;
		}
		
		if(t == null){
			return 0;
		}
		
		//find touch in the successors list
		Touch successor = null;
		Window predecessor = null;
		for(int i=0;i<successor_touch.size();i++){
			//System.out.println(successor_touch.get(i));
			//System.out.println(t);
			if((successor_touch.get(i).compare_with_token(get_tokens(), t)) &&
					(windows.get(i).compare_with_token(this.get_tokens(), w))){
				successor = successor_touch.get(i);
				predecessor = windows.get(i);
				break;
			}
		}
		
		if((successor == null) || (predecessor == null)){
			//System.out.println("zergling");
			return 0;
		}
		
		return successor.get_probability(predecessor);
	}


	///returns the distribution of the data as a whole
	public Distribution get_distribution(){
		//if the distribution has not been computed, compute it
		if(!distribution_computed){
			distribution=new Distribution(touches);
			distribution_computed=true;
		}
		
		return distribution;
	}

	
	///returns a list of distributions for each key
	public List<Distribution> get_key_distribution(){
		//if the key_distribution has not been computed, compute it
		if(!key_distribution_computed){
			compute_key_distribution();
			key_distribution_computed=true;
		}
		
		return key_distribution;
	}


	public int get_window(){
		return window;
	}


	public int get_token(){
		return token;
	}


	public int get_threshold(){
		return threshold;
	}
	

	///returns the percent difference between this chain and auth_chain.
	///the value returned will be between 0 and 1
	///0 indicates there is no difference
	///1 indicates there is a large difference
	public double compare_to(Chain auth_chain){
		double difference = 0;
		
		//guarentee that everything has been computed
		this.get_distribution();
		this.get_key_distribution();
		this.get_tokens();
		this.get_windows();
		this.get_touch_probability(null, null);
		
		//for every window in auth_chain
		for(int i=0;i<auth_chain.get_windows().size();i++){
			//find the difference between base_chain and auth_chain's corresponding window
			difference += get_window_difference(this.get_windows().get(i), auth_chain.get_windows().get(i), this.successor_touch.get(i), auth_chain.successor_touch.get(i));
		}

		//return the average of the window differences
		return difference/((double)auth_chain.get_windows().size());
	}
	
	
	///THIS DOES NOT WORK, but there may be useful code here
	///returns a list of percent differences for each compare iteration
	/// a sort of percent difference between this model and the one passed in. The idea is that this may be used to authenticate. Most of this code should come from Model_Compare.py
	//TODO consider doing this on multiple threads if preformance is an issue
	//TODO look at what happens when a window occurrs more than once in a chain
//	public double compare_to_old(Chain auth_chain){
//		//TODO compare two chains and return the percent difference between them
//		//make sure to use the get_x() methods here instead of just using the instance variables. This will guarentee that the values have been calculated by the time they are used.
//		//preform this check to allow the assumption that the base_chain is larger than the auth chain.
//		//this check also forces the windows to be computed if they have not been alreadys
//		if(this.get_windows().size()<auth_chain.get_windows().size()){
//			//preform the comparison the other direction
//			return auth_chain.compare_to_old(this);
//		}
//		
//		//from now on I can assume that this_chain has more windows than auth_chain
//		double differance = 0;
//		ArrayList<Double> differances_list = new ArrayList<Double>();
//		
//		//TODO get a list of probabilities for each compare iteration
//		int end_index = auth_chain.windows.size()-1; // index of the last window to compare in the base_model
//		int start_index = 0;
//		
//		double current_difference = compare(this, auth_chain, start_index, end_index);		
//		differances_list.add(current_difference);
//		start_index++;
//		end_index++;
//		
//		while(end_index <= this.windows.size()-1){
//			//while we are still within the base_chain
//			//do this as an incremental process.... compare should be done once before this loop, then each probability can be incrementally updated
//			//find the differance of the current window, using the current differance
//			current_difference += get_differential_difference(this, auth_chain, start_index, end_index);
//			
//			differances_list.add(current_difference);			
//			
//			start_index++;
//			end_index++;
//		}
//		
//		// use this list of probabilities to get an overall differance
//		double max_probability = 0;
//		double min_probability = 1;
//		double average_probability = 0;
//		double total_probability = 0;
//		
//		//compute the differant metrics that could be used to authenticate based on the probabilities list
//		for(int i=0;i<differances_list.size();i++){
//			if(differances_list.get(i)>max_probability){
//				max_probability = differances_list.get(i);
//			}
//			
//			if(differances_list.get(i)<min_probability){
//				min_probability = differances_list.get(i);
//			}
//			
//			total_probability+=differances_list.get(i);
//		}
//		
//		average_probability = total_probability/differances_list.size();
//		
//		//TODO determine what to return as a differance based on these metrics
//		differance = average_probability; //TODO change this
//		
//		return differance ;
//		
//	}
	
	
	///return the difference between two given windows
	///@param base window successor touch is the touch coming after base_window in the base model
	///@param auth window_touches are the touches which succeeds auth_window
	/// base_window_successor and auth_window_successor should be equivilent. This method simply returns the difference in their probabilities.
	/// the reason this method is broken out is because this is likely to be modified to refine the model
	private double get_window_difference(Window base_window, Window auth_window, Touch base_window_successor_touch, Touch auth_window_successor_touch){
		//we want to know the differences in touch probability for the touches which come after these windows
		double difference = 0;
		
		double base_probability = base_window_successor_touch.get_probability(base_window);
		double auth_probability = auth_window_successor_touch.get_probability(auth_window);
		
		//System.out.println("base_p:"+base_probability+" auth_p:"+auth_probability);
		
		//TODO should this be absolute value?
		difference = Math.abs(base_probability - auth_probability);
		
		return difference;
	}
	
	
	///get the amount the difference would change by adding this window,and removing the oldest window
	///the last window given will be the window added
	///the window being added is the one at base end index, this will correspond the the last 
//	private double get_differential_difference(Chain base_chain, Chain auth_chain, int base_start_index, int base_end_index){
//		//TODO determine what base_window and auth_window should be
//		double removed_window_difference = 0;
//		double added_window_difference = 0;
//		
//		Window base_removed_window = null;
//		Window auth_removed_window = null;
//		Touch base_removed_successor_touch = null;
//		Touch auth_removed_successor_touch = null;
//		removed_window_difference = get_window_difference(base_removed_window, auth_removed_window, base_removed_successor_touch, auth_removed_successor_touch);
//		
//		Window base_added_window = null;
//		Window auth_added_window = null;
//		Touch base_added_successor_touch = null;
//		Touch auth_added_successor_touch = null;
//		added_window_difference = get_window_difference(base_added_window, auth_added_window, base_added_successor_touch, auth_added_successor_touch);
//		
//		return (added_window_difference - removed_window_difference);
//	}
	
	
	///compare two equally sized chains. Return the differance between them
//	private double compare(Chain base_chain, Chain auth_chain, int base_start_index, int base_end_index){
//		//compare all of auth to base
//		//compare between base_start_index and base_end_index
//		double differance = 0;
//		int base_window_index = 0;
//		
//		List<Window> base_windows = base_chain.get_windows();
//		List<Window> auth_windows = auth_chain.get_windows();
//		Window base_window = null;
//		Window auth_window = null;
//		
//		//for each window in auth_windows
//		for(int i=0;i<auth_windows.size();i++){
//			//TODO compare auth window to the same window in base_windows
//			// handle when the window is not found
//			// determine what base_window, auth_window should be
//			auth_window = auth_windows.get(i);
//			base_window_index = get_base_window_index(base_chain, auth_window, auth_chain.successor_touch.get(i), base_start_index, base_end_index);			
//			
//			if(base_window_index != -1){
//				//auth_window was found in base_windows
//				base_window = base_windows.get(base_window_index);
//				differance += get_window_difference(base_window, auth_window, base_chain.successor_touch.get(base_window_index), auth_chain.successor_touch.get(i));
//			}else{
//				//window in auth_windows was not found in base_windows
//				//TODO determine what to do... does this make sense
//				differance+=1;
//			}
//		}
//		
//		return differance;
//	}
	
	
	//returns non -1 only if successor touches are the same for the given window
	//returns the index of auth_window if it is contained in base window
	//will only return the index if the successor touches are equivilent
	//returns -1 if not contained
	///this function will search between base start index, and base end index
	private int get_base_window_index(Chain base_chain, Window auth_window, Touch auth_successor_touch, int base_start_index, int base_end_index){
		//compre with tokens
		int i=base_start_index;
		
		List<Window> base_windows = base_chain.get_windows();
		List<Touch> base_successor_touch = base_chain.successor_touch;
		
		for(i=base_start_index;(i<base_windows.size()) && (i<base_end_index);i++){
			//if the successor touches are the same 
			if(base_successor_touch.get(i).compare_with_token(this.tokens, auth_successor_touch)){
				//also if the windows are equal
				if(base_windows.get(i).compare_with_token(this.get_tokens(), auth_window)){
					break;
				}
			}

		}
		
		return (i==base_windows.size())?(-1):(i);
	}


	///called when the model is updated. keeps track of whether a given attribute needs to be recalculated
	private void on_model_update(){
		distribution_computed=false;
		probability_computed=false;
		key_distribution_computed=false;
		windows_computed=false;
		tokens_computed=false;
	}


	///compute the key distribution
	private void compute_key_distribution(){
		//TODO check for correctness
		//this involves creating a distribution object for each key
		//1. create a list for each keycode
		//2. create a distribution object from each of these lists
		//reset key_distribution to an empty array
		key_distribution = new ArrayList<Distribution>();
		ArrayList<List<Touch>> list_of_keycodes = new ArrayList<List<Touch>>();

		//for the entire list of touches. After this loop, all keycodes should be placed in a separate list in list_of_keycodes
		for(int i=0;i<touches.size();i++){
			//get the index of the current keycode
			int keycode_index = keycode_index(list_of_keycodes, touches.get(i).get_key());

			if(keycode_index==-1){
				//the keycode does not already exist in list of keycodes, add it
				ArrayList<Touch> single_touch_list = new ArrayList<Touch>();
				single_touch_list.add(touches.get(i));
				
				list_of_keycodes.add(single_touch_list);
			}else{
				//the keycode already exits. Simply add it to the array at keycode_index
				list_of_keycodes.get(keycode_index).add(touches.get(i));
			}
		}
		
		//create a distribution object for each of the lists in list of keycodes, add this to key distribution
		for(int i=0;i<list_of_keycodes.size();i++){
			key_distribution.add(new Distribution(list_of_keycodes.get(i), list_of_keycodes.get(i).get(0).get_key()));
		}

		//denote that the distribution has been computed
		key_distribution_computed=true;
	}


	///returns the index of a given keycode in the list containing lists of touches corresponding to a given key. Returns -1 if the keycode does not exist in the list.
	//TODO this seems like it could be done in less work, could save the indexes in some sort of data structure and retrieve them when needed
	private int keycode_index(ArrayList<List<Touch>> list_of_keycodes, int keycode){
		//TODO check for correctness
		int index;		
		
		for(index=0;index<list_of_keycodes.size();index++){
			if(list_of_keycodes.get(index).get(0).get_key() == keycode){
				break;
			}
		}

		return (index==list_of_keycodes.size())? -1: index;
	}


	///compute the probability
	//TODO consider splitting this up across multiple threads if preformance is an issue. I'm fairly certain this will be the main performance concern.
	private void compute_probability(){
		//TODO
		//assign the appropriate probability to each of the touch objects
		//TODO consider computing windows on a separate thread, and joining this thread before windows are needed.
		//TODO implementing hashing of the windows later
		// basic process
		// for a given window, I want to store in the next touch the probability of that touch coming after this window. This will depend on the other touches which have succeeded this sequence and the number of times the window occurrs. 
		// 1) get a list of windows
		// 2) determine how many times each of the windows occurrs
		// 3) assign a probability to the successor touch based on 1,2
		List<Window> window_list = get_windows();
		int occurrences_of_window;
		int number_successions;
		double probability;

		for(int i=0;i<window_list.size();i++){
			//get the number of occurrences of this window
			occurrences_of_window = occurrence_count(window_list, window_list.get(i));
			
			//get the number of times a touch has succeeded this window. We can use the old probability following this window to figure this out. TODO if this method turns out not be correct, this would be a good place to begin looking for mistakes.
			//number_successions = 1 + (successor_touch.get(i).get_probability(window_list.get(i)) * occurrences_of_window);
			//TODO this can deffonately be done faster (with a prefix tree?)
			number_successions=successor_count(window_list, successor_touch, window_list.get(i), successor_touch.get(i));
			
			//compute the probability
			probability = ((double)number_successions) / ((double)occurrences_of_window);
			//System.out.println("number_successions:"+number_successions+" occurrences_of_windows:"+occurrences_of_window);
			
			//set the probability of the successor touch. To do this, I need to know how many times this touch succeeds this window
			successor_touch.get(i).set_probability(window_list.get(i), probability);
		}
	}
	
	
	///counts the number of times a given touch comes after a given window. in the given window, succesors list
	private int successor_count(List<Window> window_list, List<Touch> successor_list, Window window, Touch touch){
		int count = 0;
		
		for(int i=0;i<window_list.size();i++){
			//for every occurrence of window, successor match, increment count
			if((window_list.get(i).compare_with_token(this.get_tokens(), window)) && (successor_list.get(i).compare_with_token(this.get_tokens(),touch))){
				count++;
			}
		}
		
		return count;
	}

	
	///return the number of occurrences of w in window_list
	///TODO I think this method needs to be faster. Storing windows in a prefix tree would allow for this
	private int occurrence_count(List<Window> window_list, Window w){
		//TODO check for correctness
		int occurrences=0;
		
		for(int i=0;i<window_list.size();i++){
			//determine if the windows are equal
			if(window_list.get(i).compare_with_token(this.get_tokens(),w)){
				occurrences++;
			}
		}

		return occurrences;
	}


	///compute the windows. This will also fill the successor_touch list
	///this does not taken the token rules into account. this is done later in computing the probability
	private void compute_windows(){
		//TODO check for correctness
		// this takes into account the time delay between touches when adding them to windows. There may be fewer (windows*window_size) than the total number of touches. This is because if there is too long a delay between touches, the window is simply thrown out.
		// 1) normalize the data based on the distribution (this is done already. can call tokens.get(i).contains(touch) to determine if a touch is within a given token.
		// 2) throw out anything outside of 2 sigma ( these will have -1 returned when they are normalized
		// 3) throw out any window where the gap in touches is greater than threshold
		windows = new ArrayList<Window>();
		successor_touch = new ArrayList<Touch>();
		List<Touch> touch_list = new ArrayList<Touch>();

		//for each of the touches (they are in order)
		for(int i=0; i<touches.size(); i++){
			//TODO take into account that touches are also not good if they fall outside of their keycode distribution, or the overall distribution
			//if the touch is good, add it to the touch list. A touch is good if it is within threshold time and it is contained in one of the tokens.
			if(	(get_token_index(touches.get(i)) >=0 ) && 
					((touch_list.size()==0) || 
						((touches.get(i).get_timestamp()-touch_list.get(touch_list.size()-1).get_timestamp()) <= threshold)))
			{
				//the touch is good
				touch_list.add(touches.get(i));
			}else{
				//the touch is no good. Reset the touch list
				//System.out.println("marine");
				touch_list= new ArrayList<Touch>();
			}

			//if touch list has grown big enoguh to fill a window, plus 1 for the successor touch, add it to windows
			if(touch_list.size()==window+1){
				//update windows, successor touch	
				//add_list will contain all but the last touch in touch_list
				List<Touch> add_list;
				add_list = new ArrayList<Touch>(touch_list);
				add_list.remove(add_list.size()-1);
			
				windows.add(new Window(add_list));
				successor_touch.add(touch_list.get(touch_list.size()-1));

				touch_list.remove(0);
			}
		}
	}


	///returns the index corresponding to the token which contains touch. returns -1 if no token contains touch
	private int get_token_index(Touch touch){
		//TODO check for correctness
		List<Token> token_list = get_tokens();

		//take the first token to return true
		for(int i=0;i<token_list.size();i++){
			if(token_list.get(i).contains(touch)){
				return i;
			}
		}
		
		return -1;
	}


	///handle requests for windows
	public List<Window> get_windows(){
		//if windows have not been computed, compute them
		if(!windows_computed){
			compute_windows();
			windows_computed = true;
		}

		return windows;
	}

	
	///compute the tokens
	private void compute_tokens(){
		tokens = new ArrayList<Token>();
		
		if(TOKEN_TYPE == Token.Type.linear){
			//create a set of tokens which is linear across the distribution within 2 sigma of the center
			for(int i=0; i<token; i++){
				tokens.add(new Token(get_distribution(),token,i, 2,Token.Type.linear));
			}
		}else{
			//create tokens over the keycode set
			for(int i=0;i<get_key_distribution().size();i++){
				//1 for each keycode fouch touches within 2 sigma of that keycode
				tokens.add(new Token(get_key_distribution().get(i), 1, i, 2, Token.Type.keycode_mu ));
			}
		}
	}


	///handle requests for tokens
	public List<Token> get_tokens(){
		//if tokens have not been computed, compute them
		if(!tokens_computed){
			compute_tokens();
			tokens_computed = true;
		}

		return tokens;
	}
	
	
	///NOT USEFUL IN ANDROID. This is used for debugging purposes. Outputs the model to a csv file in a readable format.
	public void output_to_csv(){
		PrintWriter output=null;
		
		try {
			output = new PrintWriter("test_model_construction.txt", "UTF-8");
			
			output.println("[preceeding sequence] [touch pressure, probability]");
			for(int i=0;i<windows.size();i++){
				String predecessor_window = windows.get(i).toString();
				double touch_probability = successor_touch.get(i).get_probability(windows.get(i));
				double touch_pressure = successor_touch.get(i).get_pressure();
				
				//output.print("-");
				output.println("["+predecessor_window+"] ["+touch_pressure+", "+touch_probability+"]");
			}
			
			output.close();
		} catch (Exception e) {
			System.out.println("Failed to open output file");
			e.printStackTrace();
		}
	}
}