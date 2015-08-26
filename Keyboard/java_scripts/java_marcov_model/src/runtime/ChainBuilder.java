package runtime;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import components.Chain;
import components.Touch;

///TODO write a HashList class (most likely extends HashMap and implemetns list to store the hash of everything. Replace ArrayList with this class whereever arraylist is used. The other option is to use LinkedHashList.
/// handles building of the model based on input events. This may not be necessary in android framework, but it will allow a consistant way of building the model across platforms to allow for easier migration to android device.
/// whenever I add a touch, I take

public class ChainBuilder{
	private int user_model_size;
	private int auth_model_size;
	final int COMPARE_INCREMENT = 1000; // compare the auth_model to the user_model every 1000 new touch inputs handled.
	final boolean INCREMENTAL_AUTHENTICATION_ON = false; // set to true causes the class to authenticate automatically. In some situations this may be preferred

	final int WINDOW = 3; //TODO find the best values for these
	final int TOKEN = 20;
	final int THRESHOLD = 1000;

	private Chain user_chain;
	private Chain auth_chain;	
	private List<Touch> touch_list;
	
	private CompareChains cc;
	
	private int count;
	
	public enum State{
		IN_PROGRESS,
		SUCCESS,
		FAILURE;
	}

	public ChainBuilder(){
		user_model_size=6000;
		auth_model_size=1000;
		
		// auth chain should be built with the same distribution/ tokens as user_chain
		user_chain = new Chain(WINDOW, TOKEN, THRESHOLD, user_model_size);
		auth_chain = new Chain(WINDOW, TOKEN, THRESHOLD, auth_model_size);
		touch_list = new ArrayList<Touch>();
		
		count=0;
	}
	
	
	///allow model size, window, token values to be specified. This is mainly for testing purposes
	public ChainBuilder(int window, int token, int threshold, int user_model_size, int auth_model_size){
		// auth chain should be built with the same distribution/ tokens as user_chain
		user_chain = new Chain(window, token, threshold, user_model_size);
		auth_chain = new Chain(window, token, threshold, auth_model_size);
		touch_list = new ArrayList<Touch>();
		
		count=0;
	}

	
	///this method should be called in some way whenever there is a touch event in android. There should be minimal amounts of processing done here so the input to the device doesn't lag.
	///I don't know by what method percicely this will need to be called in the android souce. It could be another class which simply handles touch events, or from the pre-existing android archetecture.
	public void handle_touch(Touch touch){		
		// add the touch to the touch_list
		touch_list.add(touch);
		
		//make sure the touch list is no larger than necessary
		if(touch_list.size() > (user_model_size+auth_model_size)){
			//remove the oldest touch
			touch_list.remove(0);
		}

		//every so often we want to trigger an authentication if this feature is enabled
		if((count == COMPARE_INCREMENT) && INCREMENTAL_AUTHENTICATION_ON){
			authenticate();
			count=0;
		}
		count++;
	}


	///allow forced authentication from outside of ChainBuilder. this involves starting the CompareChains.
	///this method starts the authentication
	public void authenticate(){
		//begin the compairason. This requires adding the touches to the corresponding chain.
		for(int i=0;i<touch_list.size();i++){
			//we want to add to the base chain if, we are within user_model_size
			if(i<user_model_size){
				user_chain.add_touch(touch_list.get(i));
			}
			
			//we want to add to the auth_chain if, we have passed the user_model_size
			if(i>=user_model_size){
				auth_chain.add_touch(touch_list.get(i));
			}
		}
		System.out.println(touch_list.size());
		//afterward, create the thread to begin the authentication
		cc = new CompareChains(user_chain, auth_chain);
		Thread auth_thread = new Thread(cc);

		auth_thread.start();	
	}
	
	
	///return the thread which is preforming the authentication. This method provides no guarentees about the state of the thread. It may even be null!
	public CompareChains get_authenticate_thread(){
		return cc;
	}
	
	
	///handle requests for the current state of the authentication
	public State get_authenticate_state(){
		State state = null;
		
		if(!cc.get_auth_complete()){
			//the authentication is not yet complete
			state = State.IN_PROGRESS;
		}else{
			if(cc.get_auth_result()){
				state = State.SUCCESS;
			}else{
				state = State.FAILURE;
			}
		}
		
		return state;
	}


	///this code will NOT BE USEFULL ON ANDROID. It will build the model from a csv file in the current working directory. It will however utilize the handle_touch() method to add new touches to the chain. It is simply a matter of where the touches are coming from.
	///TODO move this method to another place. it is only by convience that it exists here now.
	public void build_chain_from_csv(File file){
		//TODO check for correctness
		// read in data from csv file and package them in touches. Then call handle_touch();
		List<Touch> touches = parse_csv(file);
		
		//add each of the items to the chain one at a time
		Iterator<Touch> touch_iterator = touches.iterator();

		while(touch_iterator.hasNext()){
			handle_touch(touch_iterator.next());
		}
	}


	///parse the csv file NOT USEFULL ON ANDROID
	@SuppressWarnings("resource")
	public static List<Touch> parse_csv(File file){
		ArrayList<Touch> touches = new ArrayList<Touch>();
		String line;

		//add everything in the arraylist to thouches
		Scanner scanner=null;
		try {
			scanner = new Scanner(file);
			
			while(scanner.hasNext()){
				//TODO parse the input. Need to know 1) where keycode values are  2) where touch pressure, timestamp are.
				//format of the file is timestamp,keycode,pressure
				line = scanner.nextLine();
				String[] line_parts = line.split(",");
				
				touches.add(new Touch(Integer.valueOf(line_parts[1]), Double.valueOf(line_parts[2]), Long.valueOf(line_parts[0])));
			}
		} catch (FileNotFoundException e) {
			System.out.println("unable to open input file");
			e.printStackTrace();
		}

		return touches;
	}
}
