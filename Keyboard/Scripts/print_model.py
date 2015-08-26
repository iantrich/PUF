#! /bin/python2
import csv
import myutilities

#things which can be changed to build differant models
output_file_name = 'test/test_constructed_model'
raw_data_file_name ='Data/Raw Data/3-30-15_timdee_07924e50.csv'

# this method will print all points in model building out to a file
def save_all():
    save_constructed_model()


def save_constructed_model():
    #construct the model
    base_table = {}

    token = 5
    window = 5
    threshold = 500
    model_size = 5000

    # Use a clustering algorithm to find the distribution of touches
    distribution = myutilities.cluster_algorithm(raw_data_file_name, token, model_size)

    # Build raw lookup table
    base_table = {}

    ret = myutilities.build_lookup(raw_data_file_name, base_table, distribution, window, threshold, token, model_size)
    base_table = ret[0]

    # Get probabilities
    base_table = myutilities.convert_table_to_probabilities(base_table)

    # Get distance between base and auth models
    #probability = myutilities.build_auth_table(raw_data_path, base_table, distribution, window, threshold, token, n)

    #save the model to the output file
    #want to save it in format [predecessor window] [touch_pressure, touch_probability]
    w = open(output_file_name, 'w')
    w.write('[predecessor window] [touch_pressure, touch_probability]\n');

    for k, v in base_table.items():
        for i, e in enumerate(v.get('chain')):
            predecessor_window="".join(format(x, "6.3f") for x in e.get('sequence'))
            touch_pressure=0
            touch_probability=0 #is this correct?, look at what probabilities holds

            w.write(str(predecessor_window)+' [%d, %d]\n' % (touch_pressure, touch_probability))
#        for k, v in base_table.items():
#            for i, e in enumerate(v.get('chain')):
#                for k2, v2 in e.items():
#                    predecessor_window=e.get('sequence')
#                    touch_pressure=
#                    touch_probability=

#                    w.writerow([predecessor_window] [touch_pressure, touch_probability])
