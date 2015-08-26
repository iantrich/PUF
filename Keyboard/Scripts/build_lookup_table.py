__author__ = 'Ian Richardson - iantrich@gmail.com'
# Written for Python 2.7
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

import myutilities


def build_table(raw_data_path):
    # Window and Token sizes to use
    window_sizes = [3, 4, 5, 6, 7, 8, 9, 10]
    token_sizes = [5, 6, 7, 8, 9, 10, 20, 30, 40, 50]
    time_thresholds = [1000, 1500, 2000]

    tables = []

    # Generate lookup table for each combination of window size and token size
    for win_i, window in enumerate(window_sizes):
        for tok_i, token in enumerate(token_sizes):
            for time_i, threshold in enumerate(time_thresholds):
                table = {}

                # Use a clustering algorithm to find the distribution of touches
                distribution = myutilities.cluster_algorithm(raw_data_path, token)

                # Build raw lookup table
                table = myutilities.build_lookup(raw_data_path, table, distribution, window, threshold, token, False)

                # Go through table and convert values to probabilities
                table = myutilities.convert_table_to_probabilities(table)

                # Append generated table to array of tables
                tables.append({
                    'table': table,
                    'distribution': distribution,
                    'window': window,
                    'token': token,
                    'threshold': threshold
                })
                #print table

                print "Lookup table progress: " + str(win_i * 10 + tok_i) + '%'

    print "Lookup Table building process complete\n"
    return tables