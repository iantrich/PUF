__author__ = 'Ian Richardson - iantrich@gmail.com'

###########################################################
#
#   This script is to be used to combine the pressure/time
#   values csv with the associated keycode values for a
#   given user from the PUF keyboard application.
#
#   NOTE: This should not be needed after the change to the
#   PUF keyboard that now captures keycodes
#
###########################################################

import csv
import myutilities
import re
import os


def convert_file():
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

    base_file_name = re.split('_|\.', available_base_sets[selected_base_set])

    base_file_desc = {
        'date': base_file_name[0],
        'user': base_file_name[1],
        'device': base_file_name[2]
    }

    output_path = raw_data_dir + base_file_desc.get(
        'user') + '/' + base_file_desc.get(
        'device') + '/'

    if os.name is 'nt':
        output_path = os.path.normpath(output_path)

    myutilities.create_dir_path(output_path)

    with open(base_file_path, 'rt') as csvfile:
        reader = csv.reader(csvfile)
        converted = row_to_character(reader)
    with open(output_path + available_base_sets[selected_base_set] + '_converted.csv', 'wb') as csvfile:
        writer = csv.writer(csvfile)
        for row in converted:
            writer.writerow(row)


# returns an array of the form    {character, pressure} for every row
def row_to_character(reader):
    keys = Keyboard()
    array = []

    for row in reader:
        x = row[1]
        y = row[2]
        keyboard_code = keys.get_code_by_position(x, y)
        array.append([row[0], str(keyboard_code), row[3]])
    return array


# this class represents the potential characters of the keyboard
class Keyboard:
    def __init__(self):
        self.state = 0  # will indicate whether the shift key is active

    # toggles the state of the keyboard based on the code passed in
    # if the code is the shift key we toggle state
    def toggle_state(self, code):
        # test to see if code is the shift key or keyboard down key
        if code == Keyboard.symbols_toggle:
            # toggle state
            if self.state == 0:
                self.state = 1
            else:
                self.state = 0
        elif code == Keyboard.keyboard_down:
            # always reset the state
            self.state = 0

        return

    # returns the character code based on the x,y position and state of the keyboard
    # DOES NOT update the state
    def get_code_by_position(self, x_in, y_in):
        # y=0 is at the top of the keyboard.... it goes up to 400
        code = -1

        # keyboard size
        keyboard_x_size = 1200
        keyboard_y_size = 400

        # keysize
        key_standard_width = 120
        key_standard_height = 100

        # convert the inputs to floats
        y = float(y_in)
        x = float(x_in)

        # test the ranges to determine what character x,y are on the keyboard
        if self.state == 0:
            # we are in the normal character set
            # figure out what row we're in
            if 0 <= y <= 100:
                # row 1
                code = int(x / key_standard_width)
            elif 100 < y <= 200:
                # row 2
                code = 10 + int((x - 60) / key_standard_width)
            elif 200 < y <= 300:
                # row3
                if 180 < x <= 1020:
                    # it is a letter
                    code = 20 + int((x - 180) / key_standard_width)
                elif 0 <= x <= 180:
                    # it is the shift key
                    code = Keyboard.shift
                elif 1020 < x <= 1200:
                    # it is the del key
                    code = Keyboard.delete
            elif 300 < y <= 400:
                #
                if 0 <= x < 240:
                    code = Keyboard.keyboard_down
                elif 240 <= x < 420:
                    code = Keyboard.symbols_toggle
                elif 420 <= x < 780:
                    code = Keyboard.space
                elif 780 <= x < 960:
                    code = Keyboard.symbol_period
                elif 960 <= x <= 1200:
                    code = Keyboard.search

        else:
            # we are in the symbols character set
            # figure out what row we're in
            if 0 <= y <= 100:
                # row 1
                code = 100 + int(x / key_standard_width)
            elif 100 < y <= 200:
                # row 2
                code = 110 + int(x / key_standard_width)
            elif 200 < y <= 300:
                # row3
                if 180 < x <= 1020:
                    # it is a letter
                    code = 120 + int((x - 180) / key_standard_width)
                elif 0 <= x <= 180:
                    # it is the shift key
                    code = Keyboard.shift
                elif 1020 < x <= 1200:
                    # it is the del key
                    code = Keyboard.delete
            elif 300 < y <= 400:
                # row4
                if 0 <= x < 240:
                    code = Keyboard.keyboard_down
                elif 240 <= x < 420:
                    code = Keyboard.symbols_toggle
                elif 420 <= x < 780:
                    code = Keyboard.space
                elif 780 <= x < 960:
                    code = Keyboard.symbol_comma
                elif 960 <= x <= 1200:
                    code = Keyboard.symbol_enter
        return code

    # character codes... do not change these, or else
    q = 0
    w = 1
    e = 2
    r = 3
    t = 4
    y = 5
    u = 6
    i = 7
    o = 8
    p = 9
    a = 10
    s = 11
    d = 12
    f = 13
    g = 14
    h = 15
    j = 16
    k = 17
    l = 18
    z = 20
    x = 21
    c = 22
    v = 23
    b = 24
    n = 25
    m = 26
    shift = 50
    delete = 51
    keyboard_down = 52
    space = 53
    symbols_toggle = 54
    search = 55
    number_one = 100
    number_two = 101
    number_three = 102
    number_four = 103
    number_five = 104
    number_six = 105
    number_seven = 106
    number_eight = 107
    number_nine = 108
    number_zero = 109
    symbol_at = 110
    symbol_pound = 111
    symbol_money = 112
    symbol_percent = 113
    symbol_ampersand = 114
    symbol_asterisk = 115
    symbol_dash = 116
    symbol_equals = 117
    symbol_left_parenthesis = 118
    symbol_right_parenthesis = 119
    symbol_exclamation = 120
    symbol_double_quote = 121
    symbol_apostrophe = 122
    symbol_colon = 123
    symbol_semicolon = 124
    symbol_forward_slash = 125
    symbol_question_mark = 126
    symbol_period = 56
    symbol_comma = 57
    symbol_enter = 58