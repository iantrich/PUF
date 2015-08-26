package components;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

///This class will store and provide functions for a single window within the model

public class Window implements Comparable<Window>{
	private List<Touch> window;
	private int window_size;
	private int hash; // stores the hash so it doesn't have to be recomputed, unless update

	private boolean hash_computed;

	public Window(List<Touch> touches){
		window = new ArrayList<Touch>();
		window.addAll(touches);

		this.window_size=touches.size();
		hash_computed=false;
	}


	///copy constructor
	public Window(Window w){
		this.window = new ArrayList<Touch>(w.window);
		this.window_size = w.window_size;
		this.hash = w.hash;
	}
	
	
	///used for compairason of windows with a given token set.
	///return true if this window is equal to auth window.
	public boolean compare_with_token(List<Token> tokens, Window other_window){
		boolean same = true;
		
		for(int i=0;i<window.size();i++){
			if(!this.window.get(i).compare_with_token(tokens, other_window.window.get(i))){
				same=false;
				break;
			}
		}
		
		return same;
	}


	///implement a hash function which returns the hash of the current window
	@Override
	public int hashCode(){
		if(!hash_computed){
			compute_hashcode();
			hash_computed=true;
		}
		
		return hash;
	}


	///compare this window to another window. Return negative if this window is less than the other window. Comparason is based on touches' pressure. Returns 0 if they are equal.
	@Override
	public int compareTo(Window other_window){
		//TODO check for correctness
		if(other_window == null){
			//indicate they are not equal
			return 1;
		}
		
		int differance = 0;
		
		//ensure the windows are the same size
		if(this.window_size == other_window.window_size){
			//windows are the same size. compare the touches
			Iterator<Touch> this_iterator = this.window.iterator();
			Iterator<Touch> other_iterator = other_window.window.iterator();

			while(this_iterator.hasNext()){
				Touch this_touch = this_iterator.next();
				Touch other_touch = other_iterator.next();

				//compare the touches
				if(this_touch.compareTo(other_touch)!=0){
					//incrementing differance indicates they are not equal
					differance++;
				}
			}
		}else{
			//windows are differant sizes. return the differance in window sizes
			differance = this.window_size-other_window.window_size;
		}

		return differance;
	}


	///compute the hashcode based on the touches currently in the window
	private int compute_hashcode(){
		//TODO check for correctness
		int hashcode=0;

		for(int i=0;i<window.size();i++){
			hashcode = hashcode*31+window.get(i).hashCode();
		}

		return hashcode;
	}
	
	
	@Override
	public String toString(){
		String string="";
		
		string+=window.get(0).toString();
		for(int i=1;i<window.size();i++){
			string+=", ";
			string+=window.get(i).toString();
		}
		
		return string;
	}
}
