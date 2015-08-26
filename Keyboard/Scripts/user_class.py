__author__ = 'Ian Richardson - iantrich@gmail.com'
# Will take in a random set of data and classify the most likely user based on given user profiles
# Log the dataset name, user profiles compared to, token/window sizes and if the classification was correct
# written for pyton 2.7

import os
import csv

# TODO List available raw data sets


def match_class():
    #read all user profiles, they exist in "Data/User Profiles/<user>/<device>/Threshold Calculations.csv"
    print("reading in data from \n");

    #given random set of raw data, decide which user profile matches the best
    #matching user profile may be different for each token/window size


    #create a log in Log
    #contains actual user's name
    #for each token/window size determine most likely user
