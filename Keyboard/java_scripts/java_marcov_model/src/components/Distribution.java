package components;
import java.util.Iterator;
import java.util.List;

///TODO make the computations happen at request time, and cache the result so it does not need to be recomputed. Or leave it as is.... as distribution objects are only created as needed in the rest of the code.
///this class knows how to calculate the distribution of a list of touches
public class Distribution{	
	private double min;
	private double max;
	private double average;
	private double standard_deviation;

	private int associated_keycode;

	private boolean average_computed;
	
	public Distribution(List<Touch> touches){
		average_computed=false;
		associated_keycode=-1;

		min = calc_min(touches);
		max = calc_max(touches);
		average = calc_average(touches);
		standard_deviation = calc_standard_deviation(touches);
	}


	///this constructor allows a keycode to be associated with the distribution
	public Distribution(List<Touch> touches, int keycode){
		average_computed=false;
		associated_keycode=keycode;

		min = calc_min(touches);
		max = calc_max(touches);
		average = calc_average(touches);
		standard_deviation = calc_standard_deviation(touches);
	}


	///copy constructor. This exists because computations are done in the constructor. Copying in this way avoids recomputation.
	public Distribution(Distribution d){
		//TODO does this make sence, will it cause issues
		if(d==null){
			return;
		}
		this.min = d.min;
		this.max = d.max;
		this.average = d.average;
		this.standard_deviation = d.standard_deviation;
	}


	///updates the distribution using a list of touches. This update has nothing to do with the old values in the distribution. It is synonomous to creating a new Distribution object with this list of touches.
	public void update(List<Touch> touches){
		min = calc_min(touches);
		max = calc_max(touches);
		average = calc_average(touches);
		standard_deviation = calc_standard_deviation(touches);
	}


	///calculate min pressure value in touches. Returns 1 if  no touches in list
	private double calc_min(List<Touch> touches){
		//TODO check for correctness
		Iterator<Touch> touches_iterator = touches.iterator();
		double min_pressure = 1;

		while(touches_iterator.hasNext()){
			Touch t = touches_iterator.next();
			
			if(t.get_pressure() < min_pressure){
				min_pressure = t.get_pressure();
			}
		}

		return min_pressure;
	}

	///calculate max pressure value in touches. returns 0 on no touches
	private double calc_max(List<Touch> touches){
		//TODO check for correctness
		Iterator<Touch> touches_iterator = touches.iterator();
		double max_pressure = 0;

		while(touches_iterator.hasNext()){
			Touch t = touches_iterator.next();
			
			if(t.get_pressure() > max_pressure){
				max_pressure = t.get_pressure();
			}
		}

		return max_pressure;
	}


	///calcualte average pressure value in touches. returns 0 on no touches
	private double calc_average(List<Touch> touches){
		//TODO check for correctness
		Iterator<Touch> touches_iterator = touches.iterator();
		double average = 0;
		double total_pressure = 0;

		while(touches_iterator.hasNext()){
			Touch t = touches_iterator.next();
			
			total_pressure += t.get_pressure();
		}

		average = total_pressure/touches.size();

		average_computed=true;
		return average;
	}


	///calculate standard deviation of pressure values in touches
	private double calc_standard_deviation(List<Touch> touches){
		//TODO check for correctness
		double std = 0;

		//if the average has not yet been computed, compute it
		if(!average_computed){
			this.average=calc_average(touches);
		}

		//1. Work out the Mean (the simple average of the numbers)
		//2. Then for each number: subtract the Mean and square the result
		//3. Then work out the mean of those squared differences.
		//4. Take the square root of that and we are done!
		Iterator<Touch> touches_iterator = touches.iterator();
		int count = 0;
		double total_subtract_mean_squared = 0;

		while(touches_iterator.hasNext()){
			Touch t = touches_iterator.next();
			
			total_subtract_mean_squared += Math.pow(t.get_pressure()-this.average, 2);
			count++;
		}

		//std is the square root of the average of these numbers
		std = Math.sqrt(total_subtract_mean_squared / count);

		return std;
	}


	public double get_min(){
		return min;
	}


	public double get_max(){
		return max;
	}


	public double get_average(){
		return average;
	}


	public double get_standard_deviation(){
		return standard_deviation;
	}


	///returns the keycode associated with this distribution. If the distribution does not have an associated keycode, this method will return -1.
	public int get_keycode(){
		return associated_keycode;
	}
	
	
	///determine if this distribution is exactly equal to another distribution
	@Override
	public boolean equals(Object o){
		//TODO test for correctness
		if(o==null){
			//because the '.' operator was used we know this object is not null, so not null does not equals null, return false
			return false;
		}
		
		boolean same = true;
		Distribution other_distribution = (Distribution)o;
		
		if(this.get_max()!=other_distribution.get_max() ||
				this.get_min()!=other_distribution.get_min() ||
				this.get_average()!=other_distribution.get_average() ||
				this.get_keycode()!=other_distribution.get_keycode() ||
				this.get_standard_deviation()!=other_distribution.get_standard_deviation()){
			same = false;
		}
		
		return same;
	}
}
