__author__ = 'Ian Richardson - iantrich@gmail.com'

###########################################################
#
#   This file contains commonly used functions that are
#   used across numerous scripts
#
###########################################################

import os
import copy
import inspect
import csv
import math
from decimal import *

PRECISION = 50


########################################################
# Returns the absolute directory path from which the
# script is being ran
#
# @return: String
########################################################
def get_current_dir():
    return os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))


########################################################
# @param(path): String representation of an absolute path
#
# Returns a list of directories in a given path
#
# @return: List of Strings
########################################################
def list_dirs(path):
    dirs_list = []
    for dirs in os.listdir(path):
        dirs_list.append(dirs)
    return dirs_list


########################################################
# @param(path): String representation of an absolute path
#
# Returns a List of files in a given path
#
# @return: List of Strings
########################################################
def list_files(path):
    files_list = []
    for files in os.listdir(path):
        files_list.append(files)
    return files_list


########################################################
# @param(selections): List of Strings
#
# Prints selections as a list
#
# @return:
########################################################
def print_selections(selections):
    for i, selection in enumerate(selections):
        print(str(i) + '. ' + str(selection) + '\n')


########################################################
# @param(options): List of numbers
#
# Type-checks user input to be an integer within the
# valid option range and returns the choice
#
# @return: Integer
########################################################
def grab_valid_input(options):
    choice = int(raw_input())
    while 0 > choice > len(options):
        choice = int(raw_input())
    return choice


########################################################
# @param(path): String representation of an absolute path
#
# Create directories to make that absolute path valid in
# the file system in not already present
#
# @return:
########################################################
def create_dir_path(path):
    if not os.path.exists(path):
        os.makedirs(path)


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
def normalize_raw_element(keycode, pressure, distribution, keycode_dist):
    if keycode < 0:
        keycode = 0
    if pressure < keycode_dist[keycode].get('lower') or pressure > keycode_dist[keycode].get('upper'):
        return -1
    pressure = keycode_dist[keycode].get('mean')
    for i, cluster in enumerate(distribution):
        if cluster.get('lower') <= pressure < cluster.get('upper'):
            return i
        if pressure < distribution[0].get('lower'):
            return -1
    return len(distribution) - 1


########################################################
# @param(current_window): List of List touches with
# time as a Long in touch[0] and pressure as a Float in
# touch[1]
#
# Given a current_window List, produce a unique
# hashcode for current_window[:-1] as the last touch
# is the next touch and not a part of the sequence
# but rather a part of the inner probabilities
#
# @return: Integer that represents the hashed value of
# the given current_window
########################################################
def hash_function(current_window):
    hashcode = float(0.0)
    for i, touch in enumerate(current_window):
        if i == len(current_window) - 1:
            break
        hashcode = hashcode * 31 + touch[1]
    return hashcode


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
def match_sequence(hashcode_bin, current_window):
    if hashcode_bin is None:
        return -1
    current_sequence = []
    for touch in current_window:
        current_sequence.append(touch[1])
    for i, v in enumerate(hashcode_bin.get('chain')):
        for i2, v2 in enumerate(v.get('sequence')):
            if current_sequence[i2] != v2:
                break
            if i2 == len(v.get('sequence')) - 1:
                return i
    return -1


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
def increment_probability(hashcode, hashcode_bin, link_index, current_window, table):
    hashcode_bin.get('chain')[link_index].get('probabilities')[current_window[-1][1]] += 1
    hashcode_bin.get('chain')[link_index]['total'] = hashcode_bin.get('chain')[link_index].get('total') + 1
    table[hashcode] = hashcode_bin
    return table


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
def add_link(hashcode, hashcode_bin, current_window, table, token):
    lst = [0] * token
    lst[current_window[-1][1]] = 1
    # Sequence shouldn't include timestamps
    sequence = []
    for touch in current_window:
        sequence.append(touch[1])
    hashcode_bin.get('chain').append({'sequence': sequence[:-1],
                                      'probabilities': lst,
                                      'total': 1})
    table[hashcode] = hashcode_bin
    return table


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
def add_key(hashcode, current_window, table, token):
    lst = [0] * token
    lst[current_window[-1][1]] = 1
    # Sequence shouldn't include timestamps
    sequence = []
    for touch in current_window:
        sequence.append(touch[1])
    table[hashcode] = {'chain': [{'sequence': sequence[:-1],
                                  'probabilities': lst,
                                  'total': 1}]}
    return table


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
def touch_probability(hashcode_bin, current_window, link_index):
    return hashcode_bin.get('chain')[link_index].get('probabilities')[current_window[-1][1]]


########################################################
# @param(table): A hash table of Dictionaries with keys
# 'chain' and 'total'
#
# Convert the raw Markov Model of total to probabilities
#
# @return: Updated hash table
########################################################
def convert_table_to_probabilities(table):
    getcontext().prec = PRECISION
    for key, val in table.items():
        for i, val2 in enumerate(val.get('chain')):
            for j, val3 in enumerate(val2.get('probabilities')):
                val2.get('probabilities')[j] = Decimal(val3) / Decimal(val2.get('total'))
    return table


########################################################
# @param(read): A csv file reader
# @param(model_size): Size of Markov model to create
#
# Find the max and min pressure in a set of touches
#
# @return: Dictionary with keys 'max' and 'min'
########################################################
def find_max_min(read, model_size):
    # TODO Use model_size
    minimum = 1.0
    maximum = 0.0
    count = 0
    for r in read:
        # Raw data from twice the final model_size
        if count > model_size * 2:
            break
        if float(r[2]) > maximum:
            maximum = float(r[2])
        if float(r[2]) < minimum:
            minimum = float(r[2])
        count += 1
    return {'max': maximum, 'min': minimum}


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
# Generate a clustering distribution and return as tuple ranges
def cluster_algorithm(raw_data_file, token, model_size):
    with open(raw_data_file, 'rt') as csvfile:
        reader = csv.reader(csvfile)
        key_dist = keycode_distribution(reader, model_size)

    with open(raw_data_file, 'rt') as csvfile:
        reader2 = csv.reader(csvfile)
        max_min = find_max_min(reader2, model_size)

    variation = float((max_min.get('max') - max_min.get('min')) / token)

    i = 0
    distribution = []
    current = max_min.get('min')
    getcontext().prec = PRECISION
    while i < token:
        distribution.append({'lower': current, 'upper': current + variation,
                             'normalized': Decimal(current + current + variation) / Decimal(2)})
        current += variation
        i += 1

    return [distribution, key_dist]


########################################################
# @param(reader): A csv file reader
# @param(model_size): Size of Markov model to create
#
# Generate a keycode distribution based on a set of touches
# with a mean, standard deviation, lower bound and upper bound
#
# @return: A List representing the overall keycode distribution
########################################################
# Keycode distributions
def keycode_distribution(reader, model_size):
    data = [[] for i in range(126)]
    # data = {k: [] for k in range(126)}
    distribution = {k: {} for k in range(126)}
    count = 0

    for row in reader:
        # Raw data from twice the final model_size
        if count > model_size * 2:
            break
        data[int(row[1])].append(float(row[2]))
        count += 1

    for key, value in enumerate(data):
        n = len(value)
        if n > 0:
            m = sum(value) / n
            sd = math.sqrt(sum((x - m) ** 2 for x in value) / n)
            distribution[key] = {'std': sd, 'mean': m, 'lower': m - 2 * sd, 'upper': m + 2 * sd}
        else:
            distribution[key] = {'std': 0, 'mean': 0, 'lower': 0, 'upper': 0}

    return distribution


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
def build_lookup(raw_data_file, table, distribution, window, threshold, token, model_size):
    normalized = []
    current_window = []
    getcontext().prec = PRECISION
    count = 0

    with open(raw_data_file, 'rt') as csvfile:
        reader2 = csv.reader(csvfile)

        # Normalize data based on found distribution
        for row in reader2:
            normalized_item = normalize_raw_element(int(row[1]), float(row[2]), distribution[0], distribution[1])
            normalized.append([row[0], int(normalized_item)])

        # Analyze touches
        for touch in normalized:
            if count > model_size:
                break
            # Throw out pressure values less than 0 as these are ones that were not within their keycode's
            # distribution of 2-sigma
            if touch[1] < 0:
                current_window = []
            if len(current_window) > 0 and long(touch[0]) - long(current_window[-1][0]) >= threshold:
                current_window = []
                current_window.append(touch)
            else:
                current_window.append(touch)

                # Once the window size is filled and a next touch is captured add it to the Markov Model
                if len(current_window) == window + 1:
                    count += 1
                    # Hash the touch pressures
                    hashcode = hash_function(current_window)
                    if hashcode in table:
                        # Found the hashcode in our table
                        hashcode_bin = table.get(hashcode)
                        # Check if the exact sequence is in the found bin
                        link_index = match_sequence(hashcode_bin, current_window)
                        if link_index == -1:
                            # Sequence not found; Add a new link with the sequence and next touch
                            table = add_link(hashcode, hashcode_bin, current_window, table, token)
                        else:
                            # Sequence found, increment next touch
                            table = increment_probability(hashcode, hashcode_bin, link_index, current_window, table)
                    else:
                        # Hashcode not found; Add a new bin with a link to that sequence and initial touch event
                        table = add_key(hashcode, current_window, table, token)
                    # Pop off the oldest touch
                    current_window.pop(0)
        return [table, count]


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
def build_auth_table(raw_data_file, base_table, distribution, window, threshold, token, n):
    normalized = []
    current_window = []
    probabilities = []
    sequences = []
    getcontext().prec = 4
    table = {}
    s = 0
    base_n = 0
    i = 0

    with open(raw_data_file, 'rt') as csvfile:
        reader2 = csv.reader(csvfile)

        # Normalize data based on found distribution
        for row in reader2:
            normalized_item = normalize_raw_element(int(row[1]), float(row[2]), distribution[0], distribution[1])
            normalized.append([row[0], int(normalized_item)])

        # Analyze touches
        for touch in normalized:
            if touch[1] < 0:
                current_window = []
            elif len(current_window) > 0 and long(touch[0]) - long(current_window[-1][0]) >= threshold:
                current_window = []
                current_window.append(touch)
            else:
                current_window.append(touch)
                # Once the window size is filled and a next touch is captured add it to the Markov Model
                if len(current_window) == window + 1:
                    i += 1
                    # Save window sequence for removal once model comparison threshold met
                    sequences.append(copy.deepcopy(current_window))
                    # Hash the touch pressures
                    hashcode = hash_function(current_window)
                    if hashcode in table:
                        # Found the hashcode in our table
                        hashcode_bin = table.get(hashcode)
                        # Check if the exact sequence is in the found bin
                        link_index = match_sequence(hashcode_bin, current_window)
                        if link_index == -1:
                            # Sequence not found; Add a new link with the sequence and next touch
                            table = add_link(hashcode, hashcode_bin, current_window, table, token)
                        else:
                            # Sequence found, increment next touch
                            table = increment_probability(hashcode, hashcode_bin, link_index, current_window, table)
                    else:
                        # Hashcode not found; Add a new bin with a link to that sequence and initial touch event
                        table = add_key(hashcode, current_window, table, token)
                    # Do initial comparison of tables
                    if i == n:
                        ret = compare(base_table, table)
                        s = ret[0]
                        base_n = ret[1]
                        probabilities.append(1 - abs(Decimal(s) / Decimal(base_n)))
                    if i > n:
                        # Subtract the oldest sequence probability from the total sum
                        s -= get_probability(table, base_table, sequences[0])
                        # Remove the oldest sequence from the authentication lookup table
                        table = remove_oldest(table, sequences[0])
                        # Remove the oldest sequence from the saved list
                        sequences.pop(0)
                        # Add the newest sequence probability to the total sum
                        s += get_probability(table, base_table, sequences[-1])
                        # Append the new probability to the list
                        probabilities.append(1 - abs(Decimal(s) / Decimal(base_n)))
                    # Pop off the oldest touch

                    current_window.pop(0)
    return probabilities


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
def compare(base, auth):
    # Sum of probabilities
    s = 0
    # Number of sequences in base
    n = 0

    for key in base.keys():
        if key in auth:
            for chain in base.get(key).get('chain'):
                # Find if each sequence in base chain is in auth chain
                chain_index = find_sequence(chain.get('sequence'), auth.get(key).get('chain'))
                if chain_index != -1:
                    auth_seq_tot = auth.get(key).get('chain')[chain_index].get('total')
                    # Compare each touch probability
                    for i, prob in enumerate(chain.get('probabilities')):
                        auth_touch_prob = auth.get(key).get('chain')[chain_index].get('probabilities')[i]
                        auth_prob = Decimal(auth_touch_prob) / Decimal(auth_seq_tot)
                        # Difference of base to auth probabilities
                        s += prob - auth_prob
                else:
                    s += 1
        else:
            s += len(base.get(key).get('chain'))

        n += len(base.get(key).get('chain'))

    # Handle keys in auth that are not in base
    for key in auth.keys():
        if key in base:
            for seq in auth.get(key).get('chain'):
                chain_index = find_sequence(seq.get('sequence'), base.get(key).get('chain'))
                if chain_index == -1:
                    s -= 1
                    n += 1
        else:
            n += len(auth.get(key).get('chain'))
            s -= len(auth.get(key).get('chain'))

    return [s, n]


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
def get_probability(auth, base, current):
    getcontext().prec = 4
    auth_hashcode_bin = auth.get(hash_function(current))
    base_hashcode_bin = base.get(hash_function(current))
    auth_link_index = match_sequence(auth_hashcode_bin, current)
    base_link_index = match_sequence(base_hashcode_bin, current)
    auth_prob = Decimal(auth_hashcode_bin.get('chain')[auth_link_index].get('probabilities')[current[-1][1]]) / Decimal(
        auth_hashcode_bin.get('chain')[auth_link_index]['total'])
    if base_link_index is -1:
        return -1
    base_prob = Decimal(base_hashcode_bin.get('chain')[base_link_index].get('probabilities')[current[-1][1]])
    return base_prob - auth_prob


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
def remove_oldest(table, current):
    hashcode = hash_function(current)
    hashcode_bin = table.get(hashcode)
    link_index = match_sequence(hashcode_bin, current)
    if link_index == -1:
        return table
    if hashcode_bin.get('chain')[link_index]['total'] == 1:
        # Remove link if no more touches present
        del hashcode_bin.get('chain')[link_index]
    else:
        hashcode_bin.get('chain')[link_index].get('probabilities')[current[-1][1]] -= 1
        hashcode_bin.get('chain')[link_index]['total'] = hashcode_bin.get('chain')[link_index].get('total') - 1

    table[hashcode] = hashcode_bin
    return table


########################################################
# @param(base_sequence): List of touches
# @param(auth_chain): List of sequences
#
# Given a list of touches, find sequence in the auth_chain
#
# @return: Return the index of the matching sequence in
# auth_chain and -1 if not found
########################################################
def find_sequence(base_sequence, auth_chain):
    for i, link in enumerate(auth_chain):
        if base_sequence == link.get('sequence'):
            return i
    return -1
