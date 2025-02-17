package roc_curve_generation;

import components.Chain;
import components.Touch;
import data_analysis.ParameterSet;
import runtime.ChainBuilder;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Boolean.TRUE;

/**
 * Generate a file with all the compare values
 * format:
 * "compare data", "profile data", "compare value"
 * "data_user", "data_device", "profile_user", "profile_device", "compare_value"
 *
 *
 */
public class CompareValueGenerator {
    public static String OUTPUT_FILE_NAME = "src/roc_curve_generation/compare_data.csv";
    public static String DATA_FOLDER_NAME = "data_sets/";

    /* the parameter set for which the ROC curve is to be generated
    *  (window, token, treshold, user_model_size, auth_model_size) */
    public static int MODEL_SIZE = 3200;
    //public static ParameterSet PARAMETER_SET = new ParameterSet(1, 2, 1000, MODEL_SIZE, MODEL_SIZE);

    // If window_size == model_size, is this equivilant to full markov chain
    //TODO compare value is always 0.0
    //TODO what probabilities are actually getting computed when this is done
    //public static ParameterSet PARAMETER_SET = new ParameterSet(1, 2, 1000, MODEL_SIZE, MODEL_SIZE);
    public static ParameterSet PARAMETER_SET = new ParameterSet(1, 2, 1000, 1600, 3200);

    /* only handle challenges within the challenge set */
    public static int[] CHALLENGE_SET = {};

    public static void main(String[] args){
        // generate compare value data with the best ParameterSet
        CompareValueGenerator.generate(PARAMETER_SET, OUTPUT_FILE_NAME);
    }

    /**
     * parameterize the CompareValueGenerator
     * to work on the given:
     *  set of model parameters
     *  output_file_name
     *
     * this data is used to evaluate the influence of
     * each model parameter
     */
    public static void generate(ParameterSet parameter_set, String output_file_name){
        ArrayList<Boolean> positive_list = new ArrayList<>();
        ArrayList<Double> compare_value_list = new ArrayList<>();
        ArrayList<Boolean> same_user_list = new ArrayList<>();
        ArrayList<Boolean> same_device_list = new ArrayList<>();

        // generate dummy results
        //dummy_results_for_testing_r_script(positive_list, compare_value_list);

        // generate real results
        compare_data(positive_list, compare_value_list, same_user_list,
                same_device_list, parameter_set);

        // output results to file
        output_results(positive_list, compare_value_list, same_user_list,
                same_device_list, output_file_name);

        System.out.println();
        System.out.println("data from: " + DATA_FOLDER_NAME);
        System.out.println("output to: " + output_file_name);
    }

    /**
     * use the data in data folder to create compare results
     * compare each data set
     *  - build a list of profiles with all but one Response trace
     *  - keep a list of this "left out" Response trace
     *  - compare every response trace against every profile
     *  - response traces compared against data generated
     *      by the same user on the same device should authenticate
     *      => positive = true
     *  - response traces compared against data generated
     *      by a different user or different device should not authenticate
     *      => positive = false
     */
    public static void compare_data(
            ArrayList<Boolean> positive_list, ArrayList<Double> compare_value_list,
            ArrayList<Boolean> user_list, ArrayList<Boolean> device_list,
            ParameterSet parameter_set){
        // I want several (user, device, chain) from each data file
        ArrayList<UDC> udc_list = new ArrayList<>();

        try{
            // format is "time, keycode, pressure"
            File data_folder = new File(DATA_FOLDER_NAME);

            // for each file in folder
            for(File data_file: data_folder.listFiles()){
                // combined string
                String user_device = data_file.getName().split("\\.")[0];

                // extract the user, device from the combined string
                String[] split_user_device = user_device.split("_");
                String user = split_user_device[1];
                String device = split_user_device[3];

                // get a list of Touches contained within the file
                List<Touch> touch_list = ChainBuilder.parse_csv(data_file);

                //TODO this doesn't use all the data for one of the models
                //TODO perhaps there is a cleaner way to do this

                // create one or multiple UDC for for each data file
                // for each set of MODEL_SIZE touches in the file, create UDC
                int larger_model_size = (parameter_set.user_model_size > parameter_set.auth_model_size) ?
                        parameter_set.user_model_size : parameter_set.auth_model_size;
                for(int i=0; i<(touch_list.size()/larger_model_size); i++) {
                    UDC udc = new UDC();

                    udc.user_device = user_device;
                    udc.user = user;
                    udc.device = device;

                    // user chain
                    udc.user_chain = new Chain(parameter_set.window_size,
                            parameter_set.token_size, parameter_set.threshold,
                            parameter_set.user_model_size);
                    for(int j=0; j<parameter_set.user_model_size; j++){
                        udc.user_chain.add_touch(touch_list.get(j + (i*larger_model_size)));
                    }

                    // auth chain
                    udc.auth_chain = new Chain(parameter_set.window_size,
                            parameter_set.token_size, parameter_set.threshold,
                            parameter_set.auth_model_size);
                    for(int j=0; j<parameter_set.auth_model_size; j++){
                        udc.auth_chain.add_touch(touch_list.get(j + (i*larger_model_size)));
                    }

                    // add to the list
                    udc_list.add(udc);
                }
            }
        }catch(Exception e){ e.printStackTrace(); }

        // for each thing in udc_list
        // compare against all OTHER things in udc_list
        for(UDC user_udc : udc_list){
            for(UDC auth_udc : udc_list) {
                // i do actually want to compare the objects
                if(user_udc != auth_udc){
                    // are the user_device strings equal?
                    positive_list.add(user_udc.user_device.equals(auth_udc.user_device));

                    // what is the compare value of the chains
                    // 1-compare_value because compare_to() returns the difference
                    compare_value_list.add(1-user_udc.user_chain.compare_to(auth_udc.auth_chain));

                    // populate the user, device lists
                    // 1 for same user or same device
                    // 0 for different user or different device
                    user_list.add(user_udc.user.equals(auth_udc.user));
                    device_list.add(user_udc.device.equals(auth_udc.device));
                }
            }
        }
    }

    /**
     * defines a combination of (user, device, chain)
     */
    private static class UDC {
        public String user_device;
        public String user;
        public String device;
        /* user chain is used when this is the training UDC */
        public Chain user_chain;
        /* auth chain is used when this is the test UDC */
        public Chain auth_chain;
    }

    /**
     * output the results to file
     */
    public static void output_results(
            ArrayList<Boolean> positive_list, ArrayList<Double> compare_value_list,
            ArrayList<Boolean> user_list, ArrayList<Boolean> device_list,
            String output_file_name){
        // positive is 1 if the compare value came from a user which should authenticate
        String header = "\"positive\", \"compare_value\", \"same_user\", \"same_device\"";

        try{
            PrintWriter file = new PrintWriter(output_file_name, "UTF-8");

            file.println(header);

            for(int i=0; i<positive_list.size(); i++){
                // output information about the comparison
                String positive_string = positive_list.get(i) == TRUE ? "1" : "0";
                String compare_value_string = String.valueOf(compare_value_list.get(i));
                String same_user_string = user_list.get(i) == TRUE ? "1" : "0";
                String same_device_string = device_list.get(i) == TRUE ? "1" : "0";

                file.println(positive_string + ", " + compare_value_string +
                        ", " + same_user_string + ", " + same_device_string);
            }

            file.close();
        }catch(Exception e){ e.printStackTrace(); }
    }

    /**
     * generate some dummy results
     * @param positive_list
     * @param compare_value_list
     */
    public static void dummy_results_for_testing_r_script(
            ArrayList<Boolean> positive_list, ArrayList<Double> compare_value_list){
        int test_data_size = 10;
        Random random = new Random();

        for(int i=0; i<test_data_size; i++){
            positive_list.add(random.nextInt()%2 == 0);
            compare_value_list.add(random.nextDouble());
        }
    }
}
