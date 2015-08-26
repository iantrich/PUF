__author__ = "Ian Richardson - iantrich@gmail.com"
__author__ = 'Tim Dee - timdee@iastate.edu'
# Will take in a random set of data try to authenticate the user for a set of token/window/threshold values

import os
import csv
import platform
import myutilities
from decimal import *

# set the precision of all decmial numbers
PRECISION = 50

# debug use
debug_log_lookup_path=os.path.join(myutilities.get_current_dir(),"logs","log_probability_progression.txt")
# debug_log_lookup_file=open(debug_log_lookup_path,'w+')

debug_log_lookup_path_0=os.path.join(myutilities.get_current_dir(),"logs","authentication_probability.txt")
# debug_log_lookup_file_0=open(debug_log_lookup_path_0,'w+')

### globals (i know its bad programming)
# contains the number of windows used for authentication in the last call to multiply_all_probabilities
windows_authenticated = 0

###
# @param: raw_model_data_path is the path to the device user, we want to authenticate the challenge data against this data
# @param: raw_challenge_data_path is the path to the raw data to authenticate against the model data
#
#@return: True if the user is authenticated
#@return: False if the user is not authenticated
###
def authenticate_model(raw_model_data_path, raw_challenge_data_path, window, token, threshold):
    getcontext().prec = PRECISION
    # arbitrary right now. This, multiplied by probability_threshold, is what we expect a user's probability to be greater then to reasonably assure they are a valid user
    # AUTHENTICATION_RATIO is the ratio we expect the challenge data to be compaired to the user data
    AUTHENTICATION_RATIO = 1
    #above this probability we authenticate the user. Below it, we do not.
    #we calculate the probability threshold here, 
    ###
    # will be some function of:
    # the number of windows we are using to authenticate
    # the number of tokens the range is split into
    ###
    # Use a clustering algorithm to find the distribution of touches
    distribution = myutilities.cluster_algorithm(raw_model_data_path, token)
    table = {}

    # Build raw lookup table
    table = myutilities.build_lookup(raw_model_data_path, table, distribution, window, threshold, token, False)

    # Go through table and convert values to probabilities
    table = myutilities.convert_table_to_probabilities(table)


    #find the probability that this raw_data matches the model
    user_probability = multiply_all_probabilities(raw_challenge_data_path,
                                                  table,
                                                  distribution,
                                                  window,
                                                  threshold,
                                                  token)
    #keep track of the number of windows we used to authenticate the challenge data
    challenge_windows = windows_authenticated

    #print 'user_probability: ' + str(user_probability)  # should not be zero :(

    # This is one method of calculating probability threshold which takes an expected probability for each window to the power of the number of windows we will authenticate
    #PROBABILITY_THRESHOLD = calc_expected_probability(distribution,token)**calc_number_windows(raw_challenge_data_path, window, token, threshold)
   
    # This method of finding the probability threshold runs the user space data against the model generated by that data
    PROBABILITY_THRESHOLD = half_user_data_challenge_user_model(raw_model_data_path,window,token,threshold)
    user_windows = windows_authenticated
    #print('probability_threshold: '+str(PROBABILITY_THRESHOLD)) + '\n'

    # now find the ratio of user windows to challenge windows
    # take the challenge probability to this power because we would expect it to be multiplied this many more times
    CHALLENGE_RATIO=1
    if(challenge_windows != 0):
        CHALLENGE_RATIO=Decimal(user_windows)/Decimal(challenge_windows)
    else:
        # we want the authentication to fail
        #print 'failure'
        CHALLENGE_RATIO=1

    #print 'challeng ratio: ' + str(CHALLENGE_RATIO)
    auth_prob = user_probability**CHALLENGE_RATIO
    auth_threshold = PROBABILITY_THRESHOLD*Decimal(AUTHENTICATION_RATIO)
    #print 'auth_prob: ' + str(auth_prob)
    #print 'auth_threshold: ' + str(auth_threshold)

    #print the authentication ratio that would cause it to pass
    #print 'would pass authentication at AUTHENTICATION_RATIO  <= ' + str(auth_prob/PROBABILITY_THRESHOLD)

    #The above code will only quantize one file, the one requested
    if (auth_prob >= auth_threshold):
        auth = True
        debug_log_lookup_file_0.write("user authenticates with likelihood of: "+str(user_probability**CHALLENGE_RATIO)+"\n")
    else:
        auth = False
        debug_log_lookup_file_0.write("user fails authentication with likelihood of: "+str(user_probability**CHALLENGE_RATIO)+"\n")

    return auth


###
# @param path to the user data
#
# @return probabilility when authenicating the user data against the model generated by that data
###
def user_data_challenge_user_model(user_data_path,window,token,threshold):
    distribution = myutilities.cluster_algorithm(user_data_path, token)
    table = {}

    # Build raw lookup table
    table = myutilities.build_lookup(user_data_path, table, distribution, window, threshold, token, False)

    # Go through table and convert values to probabilities
    table = myutilities.convert_table_to_probabilities(table)

    return multiply_all_probabilities(user_data_path, table, distribution, window, threshold, token)


###
# @param path to the user data
#
# @return probabilility when authenicating the user data against the model generated by that data
#
# This function is like the previous, but splits the user data in half and attempts
# to authenticate the second half the the data on the first half of the model
# the hope is that this will be more accurate
##
def half_user_data_challenge_user_model(user_data_path,window,token,threshold):
    #split the data into two halves
    part_1_user_data_path=os.path.join(myutilities.get_current_dir(),"temp","part_1_user_data")
    part_2_user_data_path=os.path.join(myutilities.get_current_dir(),"temp","part_2_user_data")

    # create the directory if it does not exit
    myutilities.create_dir_path(os.path.join(myutilities.get_current_dir(),"temp"))

    # figure out how many lines of data i have
    user_data_file=open(user_data_path,'r')
    i=0

    for i,line in enumerate(user_data_file):
        pass

    user_data_lines=i+1
    user_data_file.close()

    #split good data in half by creating a temp file and copying in the first half of good_data
    user_data_file=open(user_data_path,'r')
    part_1_user_data_file=open(part_1_user_data_path,'w+')
    part_2_user_data_file=open(part_2_user_data_path,'w+')

    for i in range(0,user_data_lines/2):
        part_1_user_data_file.write(user_data_file.readline())

    for i in range(((user_data_lines/2)+1),user_data_lines):
        part_2_user_data_file.write(user_data_file.readline())

    user_data_file.close()
    part_1_user_data_file.close()
    part_2_user_data_file.close()

    # Now, authenticate the second half of the data against the first half of the data
    distribution = myutilities.cluster_algorithm(part_1_user_data_path, token)
    table = {}

    # Build raw lookup table with first half of user data
    table = myutilities.build_lookup(part_1_user_data_path, table, distribution, window, threshold, token, False)

    # Go through table and convert values to probabilities
    table = myutilities.convert_table_to_probabilities(table)

    # use the second half of the user data to authenticate against the first half
    return multiply_all_probabilities(part_2_user_data_path, table, distribution, window, threshold, token)


###
# @param  more tokens suggests the probability should be lower for a given token,
#         even in a normal distribution this is true
#
# @return the expected probability of a single window in
#         the authentication data set matching the model
def calc_expected_probability(distribution, tokens):
    getcontext().prec = PRECISION
    # the theory is that some function of the distribution
    # 1 divided by the number of tokens half the range is split into
    return 1/(tokens/2.0)



###
# @param  authentication data set
# @param  size of window
#
# @return number of windows in the authentication data set
def calc_number_windows(raw_challenge_data_path, window_size, token, time_threshold):
    getcontext().prec = PRECISION
    table={}
    #build the chain
    distribution = myutilities.cluster_algorithm(raw_challenge_data_path, token)
    # Build raw lookup table
    table = myutilities.build_lookup(raw_challenge_data_path, table, distribution, window_size, time_threshold, token, False)

     # Go through table and convert values to probabilities
    table = myutilities.convert_table_to_probabilities(table)
    print(table)
    #count the number of links
    #print str(table)
    #return len(table)
    return 10

###
# @param 
#
# @return calculated probability of a challenge data set matching the model
#         This return value is based on the sen sec model where we continually
#         multiply by all probabilities
###
def multiply_all_probabilities(raw_data_file, table, distribution, window, threshold, token):
    normalized = []
    current_window = []
    getcontext().prec = PRECISION
    probability = Decimal(1.0)

    global windows_authenticated
    windows_authenticated = 0

    #find the minimum non-zero probability in the model
    min_probability=find_min_probability(table)

    with open(raw_data_file, 'rt') as csvfile:
        reader2 = csv.reader(csvfile)
    
        # Normalize data based on found distribution
        for row in reader2:
            normalized_item = myutilities.normalize_raw_element(int(row[1]), float(row[2]), distribution[0], distribution[1])
            normalized.append([row[0], int(normalized_item)])

        # Analyze touches
        for touch in normalized:
            if len(current_window) > 0 and long(touch[0]) - long(current_window[-1][0]) >= threshold:
                current_window = []
                current_window.append(touch)
            else:
                current_window.append(touch)

                # Once the window size is filled and a next touch is captured add it to the Markov Model
                if len(current_window) == window + 1:
                    # Hash the touch pressures
                    hashcode = myutilities.hash_function(current_window)
                    if hashcode in table:
                        # Found the hashcode in our table
                        hashcode_bin = table.get(hashcode)
                        # Check if the exact sequence is in the found bin
                        link_index = myutilities.match_sequence(hashcode_bin, current_window)

                        # Multiplying probability calculated so far by probability of the next touch
                        p_touch = myutilities.touch_probability(hashcode_bin, current_window, link_index)

                        if(p_touch > 0):
                            probability *= p_touch
                        else:
                            #need a bettter way to do this.... potentially could find the lowest probability in the table (nonzero)
                            #TODO fix
                            #multiplying by the lowest probability in the table
                            probability *= Decimal(min_probability)
                            #probability *= Decimal(1)

                        windows_authenticated+=1
                        debug_log_lookup_file.write(str(probability)+'\n')

                    current_window.pop(0)

    #print 'length_normalized: ' + str(len(normalized))
    if len(normalized) > 0:
        return probability
    else:
        return 0


###
# Will find the minimum probability in the constructed model
# 
# @param the probability table that has been constructed
# @return the minimum probability in this table
###
def find_min_probability(table):
    minimum = 1

    #go though the entire table to try to find the minimum, nonzero probability
    for key, val in table.items():
        for i, val2 in enumerate(val.get('chain')):
            for j, val3 in enumerate(val2.get('probabilities')):
                value = val2.get('probabilities')[j]
                if(value!=0 and value<minimum):
                    minimum=value

    #print('minimum: '+str(minimum))
    return minimum