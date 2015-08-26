__author__ = 'Ian Richardson - iantrich@gmail.com'

###########################################################
#   NOTE: This script has been replaced by model_compare.py
#   and is now more for reference. It was used to find the
#   best possible combination of window/token/time sizes.
#   The definition of best changed to look past just the
#   maximum and model_compare.py addressed these changes.
###########################################################

import myutilities
import build_lookup_table
import csv


def find_fit():
    # Raw Data to generate Lookup Tables From
    # Raw Data directory
    raw_data_dir = myutilities.get_current_dir() + '/Data/Raw Data/'

    # List available users with raw data sets
    available_users = myutilities.list_dirs(raw_data_dir)
    myutilities.print_selections(available_users)
    print('Select a user as base:')
    # Grab selected user
    selected_base_user = myutilities.grab_valid_input(available_users)

    # List available devices for the selected user
    available_base_devices = myutilities.list_dirs(raw_data_dir + available_users[selected_base_user])
    myutilities.print_selections(available_base_devices)
    print('Select a device as base:')
    # Grab selected device
    selected_base_device = myutilities.grab_valid_input(available_base_devices)

    # List available raw data sets for the selected user and device
    available_base_sets = myutilities.list_files(
        raw_data_dir + available_users[selected_base_user] + '/' + available_base_devices[selected_base_device])
    myutilities.print_selections(available_base_sets)
    print('Select a data set as base:')
    # Grab selected data set
    selected_base_set = myutilities.grab_valid_input(available_base_sets)

    base_file_path = raw_data_dir + available_users[selected_base_user] + '/' + available_base_devices[
        selected_base_device] + '/' + available_base_sets[selected_base_set]

    base_file_name = available_base_sets[selected_base_set].split("_")

    base_file_desc = {
        'date': base_file_name[0],
        'user': base_file_name[1],
        'device': base_file_name[2]
    }

    output_path = myutilities.get_current_dir() + '/Data/User Profiles/' + base_file_desc.get('user') + '/' + base_file_desc.get(
            'device') + '/' + base_file_desc.get('date') + '/'

    myutilities.create_dir_path(output_path)

    myutilities.print_selections(available_users)
    print('Select the raw data user:\n')
    # Grab selected user
    selected_raw_user = myutilities.grab_valid_input(available_users)

    # List available devices for the selected user
    available_raw_devices = myutilities.list_dirs(raw_data_dir + available_users[selected_raw_user])
    myutilities.print_selections(available_raw_devices)
    print('Select the raw data device:\n')
    # Grab selected device
    selected_raw_device = myutilities.grab_valid_input(available_raw_devices)

    # List available raw data sets for the selected user and device
    available_raw_sets = myutilities.list_files(
        raw_data_dir + available_users[selected_raw_user] + '/' + available_raw_devices[selected_raw_device])
    myutilities.print_selections(available_raw_sets)
    print('Select the raw data set:\n')
    # Grab selected data set
    selected_raw_set = myutilities.grab_valid_input(available_raw_sets)

    raw_data_path = raw_data_dir + available_users[selected_raw_user] + '/' + available_raw_devices[
        selected_raw_device] + '/' + available_raw_sets[selected_raw_set]

    base_lookup_tables = build_lookup_table.build_table(base_file_path)

    all_probabilities = []

    for i, lookup in enumerate(base_lookup_tables):
        base_table = lookup.get('table')
        base_distribution = lookup.get('distribution')
        base_window = lookup.get('window')
        base_token = lookup.get('token')
        base_threshold = lookup.get('threshold')

        # TODO Build lookup table for the auth data and find the distance between the resulting state machine and that or the training data

        probability = myutilities.build_lookup(raw_data_path,
                                               base_table,
                                               base_distribution,
                                               base_window,
                                               base_threshold,
                                               base_token,
                                               True)

        all_probabilities.append([probability, base_window, base_token, base_threshold])
        print "Find best fit progress: " + str(i / len(base_lookup_tables) * 100) + '%'

    # Print the probabilities and such to a csv
    with open(output_path + 'against_' + available_users[selected_raw_user] + '_' + available_raw_devices[
        selected_raw_device] + '_' + available_raw_sets[selected_raw_set] + '.csv', 'w') as csvfile:
        w = csv.writer(csvfile)
        # Find which combination gives the largest probability
        best_fit = [0, 0, 0, 0]
        for item in all_probabilities:
            w.writerow(item)
            if item[0] > best_fit[0] and item[0] != 1:
                best_fit = item

    print("Best results of probability " + str(best_fit[0]) + "are with window size: " +
          str(best_fit[1]) + " and token size: " + str(best_fit[2])) + " and threshold: " + str(best_fit[3])
