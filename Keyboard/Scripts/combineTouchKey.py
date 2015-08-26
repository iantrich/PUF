__author__ = 'Ian Richardson - iantrich@gmail.com'

###########################################################
#
#   This script is to be used to combine the pressure/time
#   values csv with the associated keycode values for a
#   given user from the PUF keyboard application.
#
###########################################################

import sys
import myutilities
import csv

keycodeFile = sys.argv[2]
pressureFile = sys.argv[1]

currdir = myutilities.get_current_dir()

times = []
keycodes = []
pressures = []

with open(currdir + '/' + keycodeFile, 'rt') as csvfile:
        reader = csv.reader(csvfile)
        for row in reader:
            keycodes.append(row[0])

with open(currdir + '/' + pressureFile, 'rt') as csvfile:
        reader = csv.reader(csvfile)
        for row in reader:
            times.append(row[0])
            pressures.append(row[3])

with open(currdir + '/' + pressureFile + '_converted.csv', 'wb') as csvfile:
        writer = csv.writer(csvfile)
        for t, k, p in zip(times, keycodes, pressures):
            writer.writerow([t, k, p])