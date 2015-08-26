#! /bin/python2
__author__ = 'Ian Richardson - iantrich@gmail.com'

###########################################################
#
#   This script is used to compare the difference in two
#   keyboard models. The first data set is a base that is
#   then compared to the second data set. The max/min
#   of similarities are measured as well as the average of
#   each of being over our threshold of 70% for authentication.
#
###########################################################

# Build training table
# Convert to probabilities
# Build a auth table of size n
# Find the difference between the two tables
# Be sure to take into account any sequence/touches not seen in the auth table
# Will probably want to remove a touch when seen from the training table to make it easiest
import myutilities
import csv
import os
from decimal import *

# Raw Data to generate Lookup Tables From
# Raw Data directory
raw_data_dir = myutilities.get_current_dir() + '/Data/Raw Data/'

model_sizes = [2000,4000,6000,8000,10000]
percentages = [50]

for model_size in model_sizes:
    for percentage in percentages:
        print 'Size ' + str(model_size) + ' at ' + str(percentage) + '%'
        for base in os.listdir(raw_data_dir):
            for raw in os.listdir(raw_data_dir):
                base_file_path = raw_data_dir + base
                raw_data_path = raw_data_dir + raw
                base_file_name = base.split("_")
                base_file_desc = {
                    'date': base_file_name[0],
                    'user': base_file_name[1],
                    'device': base_file_name[2]
                    }
                raw_file_name = raw.split("_")
                raw_file_desc = {
                    'date': raw_file_name[0],
                    'user': raw_file_name[1],
                    'device': raw_file_name[2]
                    }
                output_path = myutilities.get_current_dir() + '/Data/User Profiles/' + str(model_size) + '/' + str(percentage) + '/'
                myutilities.create_dir_path(output_path)
                base_table = {}
                probabilities = []
                probs = []
                getcontext().prec = 4

                window_sizes = [3]
                token_sizes = [17, 18, 19, 21, 22, 23]
                time_thresholds = [1000, 1500, 2000]

                # Generate lookup table for each combination of window size and token size
                for win_i, window in enumerate(window_sizes):
                    for tok_i, token in enumerate(token_sizes):
                        for time_i, threshold in enumerate(time_thresholds):
                            # print 'Window: ' + str(window_sizes[win_i]) + ', Token: ' + str(token_sizes[tok_i]) + ', Threshold: ' + str(time_thresholds[time_i])

                            # Use a clustering algorithm to find the distribution of touches
                            distribution = myutilities.cluster_algorithm(base_file_path, token, model_size)

                            # Build raw lookup table
                            base_table = {}

                            ret = myutilities.build_lookup(base_file_path, base_table, distribution, window, threshold, token, model_size)
                            base_table = ret[0]

                            # Used for percentage of a lookup table
                            base_n = ret[1]
                            fraction = Decimal(100) / Decimal(percentage)
                            n = int(model_size / fraction)

                            # Get probabilities
                            base_table = myutilities.convert_table_to_probabilities(base_table)

                            # Get distance between base and auth models
                            probability = myutilities.build_auth_table(raw_data_path, base_table, distribution, window, threshold, token, n)
                            probabilities.append(probability)

                            ma = Decimal(0.0)
                            mi = Decimal(1.0)
                            s = 0
                            miChanged = False
                            for prob in probability:
                                if prob > ma:
                                    ma = prob
                                if prob < mi:
                                    mi = prob
                                    miChanged = True
                                if prob > Decimal(0.7):
                                    s += 1
                            if miChanged is False:
                                mi = Decimal(0.0)
                            if len(probability) == 0:
                                probs.append([ma, mi, 0])
                            else:
                                probs.append([ma, mi, Decimal(s)/Decimal(len(probability))])

                # Print the probabilities and such to a csv
                with open(output_path + base + 'against_' + raw + '.csv', 'wb') as csvfile:
                    w = csv.writer(csvfile)

                    for i, item in enumerate(probs):
                        w.writerow(item)
