__author__ = 'Ian Richardson - iantrich@gmail.com'

import find_best_fit
import user_auth
import user_class
import effectiveness
# import debug
import convertXY

# 1. Find best fit for user profile
# 2. Authenticate user based on profile
# 3. Match user to unknown profile


def print_options():
    print("Select an option\n"
          "1. Find the best omega, window and token size for a user compared to another sam\n"
          "2. Authenticate a user based on new raw data\n"
          "3. Find the likely profile matches for a set of raw data\n"
          "4. Print the effectiveness of different token,window,time sizes\n"
          "5. DEBUG: create log files\n"
<<<<<<< HEAD
          "6. Convert CSV file with x,y points to keyboard code"
=======
          "6. DEBUG: augment data for histogram\n"
>>>>>>> Investigating_calculated_value
          "9. Exit\n")

print_options()
selection = raw_input("Choice: ")

while selection != '9':
    if selection == '1':
        find_best_fit.find_fit()
    elif selection == '2':
        user_auth.authenticate()
    elif selection == '3':
        user_class.match_class()
    elif selection == '4':
        effectiveness.print_effectiveness()
<<<<<<< HEAD
    # elif selection == '5':
    #     debug.debug()
    elif selection == '6':
        convertXY.convert_file()
=======
    elif selection == '5':
        debug.debug()
    elif selection == '6':
        debug.augment_data()
>>>>>>> Investigating_calculated_value
    print_options()
    selection = raw_input("Choice: ")

exit()