package components;
///This class represents a token within the model. Essentially this is a range of values. A touch is defined to be within a token if the pressure value of the touch falls within this range.
///This class is designed to abstract away the clustering algorithm. This makes the rest of the code far simpler to think about
///Something to look at in the future may be a clustering algorithm that is not equally distributed

public class Token{
	private double min;
	private double max;
	private double standard_deviations; 
	
	private int low_wildcards;
	private int high_wildcards;
	
	///specify the type of token we want to build
	public enum Type{
		//in the linear case, each token is a min, max. This is designed to be used to expose the variation in pressure for a given keycode
		linear,
		//this simply creates a token for a given keycode distribution. The benefit in doing this is that the token can be used to apply rules to the the keycodes. For example, if we want to say that a touch does not constitute a keycode if it falls outside of its pressure distribution's average +-2 sigma, then we can reduce the range_min,max to include on average +- 2 sigma. 
		keycode_mu
	}
	
	
	///allow tokens to be created making each touch mu for its keycode, or in a linear fashion over the distribution for the keycode

	/// allow for creation of tokens with-in some number of standard deviations of a distribution
	public Token(Distribution distribution, int total_tokens, int token_index, double standard_deviations, Type type){
		//TODO check for correctness
		this(	(distribution.get_average()-distribution.get_standard_deviation()*standard_deviations),
				(distribution.get_average()+distribution.get_standard_deviation()*standard_deviations),
				total_tokens, token_index, type);
		this.standard_deviations = standard_deviations;
	}	


	///allow for creation of tokens over a distribution
	public Token(Distribution distribution, int total_tokens, int token_index, Type type){
		this(distribution.get_min(), distribution.get_max(), total_tokens, token_index, type);
		this.standard_deviations = 0;
	}


	/** Implemented in the constructor is the clustering algorithm. This determines how to split up the range into a number of tokens.

		range_min, minimum of the token range
		range_max, maximum of the token range
		total_tokens total number of tokens to split range into
		token_index, integer between 0 and total_tokens-1 indicating into which range this token falls
	**/
	public Token(double range_min, double range_max, int total_tokens, int token_index, Type type){
		//clustering algorithm
		//variation is now ((range_max - range_min) / total_tokens);
		switch(type){
			case linear:
				this.min = ((range_max - range_min) / total_tokens)*token_index;
				this.max = ((range_max - range_min) / total_tokens)*(token_index+1);
				break;
			case keycode_mu:
				//TODO
				this.min = range_min;
				this.max = range_max;
				break;
		}
	}


	///determines if a touch is within this token based on its pressure value
	/// this will return true if a touches pressure equals max or min, so
	/// if max of one token is min of another token, both will return true
	public boolean contains(Touch touch){
		return (touch.get_pressure() <= max) && (touch.get_pressure() >= min);
	}
	
	
	///determine if a touch is a high_wildcard
	public boolean is_high_wildcard(Touch touch){
		return (touch.get_pressure() > max);
	}
	
	
	///determine if a touch is a low wildcard
	public boolean is_low_wildcard(Touch touch){
		return (touch.get_pressure() < min);
	}
	
	
	///adds the the number of high wildcards
	public void increment_high_wildcards(){
		high_wildcards++;
	}
	
	
	///adds the the number of high wildcards
	public void increment_low_wildcards(){
		low_wildcards++;
	}
	
	
	///returns the total number of wildcards
	public int get_total_wildcards(){
		return low_wildcards+high_wildcards;
	}
	
	
	///returns the acceptable number of wildcards
	///@param total_items is the total number of items in the distribution
	public int get_acceptable_wildcards(int total_items){
		// TODO check for correctness
		// percentage should be the number of statically allowable wildcards for the standard deviation with which the token was created
		double percentage;
		
		//Chebyshev's inequality:  no more than 1/k^2 of the distribution's values can be more than k standard deviations 
		percentage = 1/(standard_deviations*standard_deviations);
		
		return (int)(total_items*percentage);
	}
	
	
	///returns true if there are more than an acceptable number of wildcards
	//public boolean is_excessive_wildcards(){
	//	return get_total_wildcards()>get_acceptable_wildcards();
	//}
	
	
	///return minimum
	public double get_min(){
		return min;
	}


	///return the maximum
	public double get_max(){
		return max;
	}

	
	///compares This token to another to determine if they are the same
	@Override
	public boolean equals(Object o_t){
		if(o_t==null){
			return false;
		}
		
		Token other_token = (Token)o_t;
		
		return (this.min == other_token.min) && (this.max == other_token.max);
	}
}
