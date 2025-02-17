package arbiter;

import puf.Bit;
import puf.Challenge;

/**
 * This arbiter functions on the mu of the pressure
 * distribution for a given touch
 */
public class MuArbiter implements Arbiter {

    public Bit[] quantize(Challenge challenge){
        Bit[] quantization_bits = new Bit[challenge.get_challenge_string().length()];

        // quantize each bit based on it's max_value compared to the average
        for(int bit_index=0; bit_index<challenge.get_challenge_string().length(); bit_index++) {
            // compare each character in the challenge string to the average of its distribution

            double average_pressure = challenge.get_user_input().get_distribution_list().get(bit_index).get_average();
            double this_character_pressure = char_to_pressure(challenge.get_challenge_string().charAt(bit_index));

            // set bit max_value 1 if this bit's average is
            // greater than equal average over all in the challenge
            // 0 otherwise
            quantization_bits[bit_index] = (this_character_pressure  >= average_pressure) ?
                    (new Bit(Bit.Value.ONE)):(new Bit(Bit.Value.ZERO));
        }

        return quantization_bits;
    }

    /**
     * this is a placeholder method
     */
    private double char_to_pressure(char character){
        //TODO this method should be replaced with some sort
        //TODO of pressure encoding in the challenge  bits
        return 0.5;
    }
}
