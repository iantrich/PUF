#Sanity Check
This file goes though some of the files line by line. Manually recording what they do. This sort of check will provide some sort of reasonable assurance that these files are doing what they should do.

Not every line is traced because some of them are self-explainatory.

##myutilities.py
###normalize_raw_element
| line | function |
|:----|:--------|
|   if keycode < 0:| makes the keycode 0 if it is less than 0, this is due to an issue with the key ranges |
|        keycode = 0 | |
|    if pressure < keycode_dist[keycode].get('lower') or pressure > keycode_dist [keycode].get('upper'): |return -1 if the keycode is not within the 2 sigma of the normal distribution for that keycode |
|        return -1 | |
|    pressure = keycode_dist[keycode].get('mean') |gets the average pressue for all points with this keycode |
|    for i, cluster in enumerate(distribution): |for all clusters in the distribution, if the pressue of this point is within the lower, upper bounds (2 sigma) for that keycode, return i. i here is the index of the distribution in which i falls. |
|        if cluster.get('lower') <= pressure < cluster.get('upper'): | |
|            return i | |
|        if pressure < distribution[0].get('lower'): | |
|            return -1 | |
|    return len(distribution) - 1 | |

###match_sequence
TODO

| line | function |
|:-----|:---------|
| line | function |

###touch_probability
| line | function |
|:-----|:---------|
|def touch_probability(hashcode_bin, current_window, link_index): |returns the probability of a thouch occurring after current_window |
|    return hashcode_bin.get('chain')[link_index].get('probabilities')[current_window[-1][1]] |the touch whoes probability we are getting is the last one in the window |

###cluster_algorithm
| line | function |
|:-----|:---------|
|raw_data_dir = myutilities.get_current_dir() + '/Data/Raw Data/' | |
|def cluster_algorithm(raw_data_file, token): | First, a number of tokens evenly spaced over the entire distribution. These tokens are limited by the min, max of all touch pressures. It also returns the min, max, and average of all touch pressures for each keycode. |
|   with open(raw_data_file, 'rt') as csvfile:	reader = csv.reader(csvfile)	key_dist = keycode_distribution(reader) |key_dist now holds the distribution (min,max,average) for each key code | 

|    with open(raw_data_file, 'rt') as csvfile:	reader2 = csv.reader(csvfile)	max_min = find_max_min(reader2) |min_max now contains the minimum and maximum for the entire distribution. |

|   variation = float((max_min.get('max') - max_min.get('min')) / token) 
|variation now contains the size of a single token |

|    i = 0
|distribution = []
|current = max_min.get('min')
|getcontext().prec = PRECISION
|while i < token:
|distribution.append({'lower': current, 'upper': current + variation,
| 'normalized': Decimal(current + current + variation) / Decimal(2)})
|current += variation
|i += 1 |This loop inserts the token ranges into the distribution |

|    return [distribution, key_dist] |return the distribution which now contains the token ranges |

##model_compare.py
| line | function |
|:-----|:---------|
|for base in os.listdir(raw_data_dir): |for all the raw data files contain in the raw_data_dir directory|
|    for raw in os.listdir(raw_data_dir): |for all the raw data files again, meaning we are comparing each data file to every other data file |
|        base_file_path = raw_data_dir + base |base_file_path is the path to the data from which the model for authentication will be built |
|        raw_data_path = raw_data_dir + raw |raw_data_path is the data from which the challenge model will be built |
|        base_file_name = base.split("_") | |
|        base_file_desc = { |derives some properties of the file based on the file name |
|            'date': base_file_name[0], | |
|            'user': base_file_name[1], | |
|            'device': base_file_name[2] | |
|            } | |
|        output_path = myutilities.get_current_dir() + '/Data/User Profiles/' + base_file_desc.get( |This is where the output file will go |
|            'user') + '/' + base_file_desc.get( | |
|            'device') + '/' + base_file_desc.get('date') + '/' | |
|        myutilities.create_dir_path(output_path) |create the output path if it has not been created |
|        base_table = {} | |
|        probabilities = [] | |
|        probs = [] | | 
| | |
|        percentage = 100 |percentage represents how closely the challenge model fits the base model, only if the model is above this percentage of matching will we authenticate |
| | |
|        getcontext().prec = 4 |decimal values will be rounded to the 4 decimal point |
| | |
|        window_sizes = [3, 4, 5, 6] | |
|        token_sizes = [5, 6, 7, 8, 9, 10, 20, 30, 40, 50] | |
|        time_thresholds = [1000, 1500, 2000] | |
|
|        # Generate lookup table for each combination of window size and token size | |
|        for win_i, window in enumerate(window_sizes): | |
|            for tok_i, token in enumerate(token_sizes): | |
|                for time_i, threshold in enumerate(time_thresholds): | |
|                    print 'Window: ' + str(window_sizes[win_i]) + ', Token: ' + str(token_sizes[tok_i]) + ', Threshold: ' + str(time_thresholds[time_i]) |print the current combination we're working on |
| | |
|                    # Use a clustering algorithm to find the distribution of touches | |
|                    distribution = myutilities.cluster_algorithm(base_file_path, token) | |
| | |
|                    # Build raw lookup table | |
|                    base_table = {} | |
|
|                    ret = myutilities.build_lookup(base_file_path, base_table, distribution, window, threshold, token, False) |ret now contains: [0] a hash table of dictionaries with keys 'chain' and 'total', [1] the number of touches in the chain |
|                    base_table = ret[0] | |
| | |
|                    # Used for percentage of a lookup table | |
|                    base_n = ret[1] |base_n holds the number of touches in the chain |
|                    fraction = Decimal(100) / Decimal(percentage) |fraction now holds the fraction of model challenge points that must match points in the model |
|                    n = int(base_n / fraction) |n now contains the number of challenge points that must match the model |
| | |
|                    # Get probabilities | |
|                    base_table = myutilities.convert_table_to_probabilities(base_table) |converts the table to a probabilities table |
|
|                     # Get distance between base and auth models | |
|                    probability = myutilities.build_auth_table(raw_data_path, base_table, distribution, window, threshold, token, n) |probability is now a list of probabilities for each compare iteration |
|                    probabilities.append(probability) |??? This variable is never used ??? |
| | |
|                    ma = Decimal(0.0) |begin ma at 0 |
|                    for prob in probability: |find the greatest probability found when comparing challenge model against user_data model |
|                        if prob > ma: | |
|                            ma = prob | |
| | |
|                    print 'Max probability: ' + str(ma * 100) + '%' | |
| | |
|                    mi = Decimal(1.0) |begin mi at 1 |
|                    for prob in probability: |find the minimum probability found when comparing challenge model against user_data model |
|                        if prob < mi: | |
|                            mi = prob | |
| | |
|                    print 'Min probability: ' + str(mi * 100) + '%' | |
|                    probs.append([ma, mi, window, token, threshold]) |append the max, min probabilities for this window,token,threshold combination to probs |
| | |
|        # Print the probabilities and such to a csv | |
|        with open(output_path + 'against_' + raw + '.csv', 'wb') as csvfile: |output the probs table to a file |
|            w = csv.writer(csvfile) | |
|            # Find which combination gives the largest probability | |
|            best_fit = [0, 0, 0, 0] |??? This variable is not used ??? |
| | | 
|            for i, item in enumerate(probs): | |
|                w.writerow(item) | |