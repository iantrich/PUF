__author__="timdee@iastate.edu"

import myutilities
import os
import user_auth
import csv

###
# this is a debugging script that will
#
# log lookup table to log_lookup
# log probability table to log_progability
###

def debug():
	dir_path=os.path.join(myutilities.get_current_dir(),"logs")

	myutilities.create_dir_path(dir_path)

	#log file paths
	log_lookup_path=os.path.join(dir_path,"log_lookup.txt")
	log_probability_path=os.path.join(dir_path,"log_probability.txt")

	#open all log files
	log_lookup_file=open(log_lookup_path,'w+')
	log_probability_file=open(log_probability_path,'w+')


	# log table and probabilities
	table = {}
	token=10
	window=5
	threshold=500
	data_path=os.path.join(myutilities.get_current_dir(),"Data","Raw Data","tim dee","07924e50","2-2-26_timdee_07924e50.csv")
	challenge_data_path=bad_data=os.path.join(myutilities.get_current_dir(),"Data","Raw Data","Ian Richardson","nexus-02","02-25-14_IanRichardson_015d4a82904c0c07.csv")
    
    # Use a clustering algorithm to find the distribution of touches
	distribution = myutilities.cluster_algorithm(data_path,token)

    # Build raw lookup table
	table = myutilities.build_lookup(data_path, table, distribution, window, threshold, token, False)

	log_lookup_file.write(str(table))

    # Go through table and convert values to probabilities
	table = myutilities.convert_table_to_probabilities(table)

	log_probability_file.write(str(table))

	log_lookup_file.close()
	log_probability_file.close()


	# authenticate user data against model, print probability
	expected_probability = user_auth.half_user_data_challenge_user_model(data_path,window,token,threshold)
	print 'expected probability: '+str(expected_probability)

	# probability when using challenge data set against model
	challenge_probability = user_auth.multiply_all_probabilities(challenge_data_path, table, distribution, window, threshold, token)
	print 'challenge_probability: '+str(challenge_probability)

	# try to run the authentication
	auth = user_auth.authenticate_model(data_path,data_path,window,token,threshold)
	print 'authentication pass: '+str(auth)

	return


def augment_data():
	dir_path=os.path.join(myutilities.get_current_dir(),"augmented_data")

	myutilities.create_dir_path(dir_path)

	#data paths
	symbol_path=os.path.join(dir_path,"pressure_by_symbol.txt")
	data_path=os.path.join(myutilities.get_current_dir(),"Data","Raw Data","tim dee","07924e50","2-2-26_timdee_07924e50.csv")

	#data files
	symbol_file=open(symbol_path,'w+')

	#organize the incoming data by symbol based on x,y coordinate
	with open(data_path, 'rt') as csvfile:
		reader = csv.reader(csvfile)
		ch_array = row_to_character(reader)

    #write the array with the rows organized by symbol out to the symbol file
	for row in ch_array:
    	#TODO organize the symbol_file by character
		symbol_file.write(str(row)+'\n')

	symbol_file.close()

	return


#rturns an array of the form    {character, pressure} for every row
def row_to_character(reader):
	count = 0
	keys = keyboard()
	array=[]

	for row in reader:
		x=row[1]
		y=row[2]
		keyboard_code=keys.get_code_by_position(x,y)

		keys.toggle_state(keyboard_code)

		ch=keys.to_string(keyboard_code)
		array.append([ch, row[3]])
		count+=1

	return array


# this class represents the potential characters of the keyboard
class keyboard:
	def __init__(self):
		self.state=0 #will indicate whether the shift key is active

	#toggles the state of the keyboard based on the code passed in
	#if the code is the shift key we toggle state
	def toggle_state(self, code):
		#test to see if code is the shift key or keyboard down key
		if(code==keyboard.symbols_toggle):
			#toggle state
			if(self.state==0):
				self.state=1
			else:
				self.state=0
		elif(code==keyboard.keyboard_down):
			#always reset the state
			self.state=0

		return

	#returns the character code based on the x,y position and state of the keyboard
	#DOES NOT update the state
	def get_code_by_position(self, x_in, y_in):
		#y=0 is at the top of the keyboard.... it goes up to 400
		code = -1

		#keyboard size
		keyboard_x_size=1200
		keyboard_y_size=400

		#keysize
		key_standard_width=120
		key_standard_height=100

		#convert the inputs to floats
		y=float(y_in)
		x=float(x_in)

		#test the ranges to determine what character x,y are on the keyboard
		if self.state==0:
			#we are in the normal character set
			#figure out what row we're in
			if(0 <= y <= 100):
				#row 1
				code=int(x/key_standard_width)
			elif(y>100 and y<=200):
				#row 2
				code=10+int((x-60)/key_standard_width)
			elif(y>200 and y<=300):
				#row3
				if(x>180 and x<=1020):
					#it is a letter
					code=20+int((x-180)/key_standard_width)
				elif(x>=0 and x<=180):
					#it is the shift key
					code=keyboard.shift
				elif(x>1020 and x<=1200):
					#it is the del key
					code=keyboard.delete
			elif(y>300 and y<=400):
				#row4
				if(x>=0 and x<240):
					code=keyboard.keyboard_down
				elif(x>=240 and x<420):
					code=keyboard.symbols_toggle
				elif(x>=420 and x<780):
					code=keyboard.space
				elif(x>=780 and x<960):
					code=keyboard.symbol_period
				elif(x>=960 and x<=1200):
					code=keyboard.search

		else:
			#we are in the symbols character set
			#figure out what row we're in
			if(y>=0 and y <= 100):
				#row 1
				code=100+int(x/key_standard_width)
			elif(y>100 and y<=200):
				#row 2
				code=110+int(x/key_standard_width)
			elif(y>200 and y<=300):
				#row3
				if(x>180 and x<=1020):
					#it is a letter
					code=120+int((x-180)/key_standard_width)
				elif(x>=0 and x<=180):
					#it is the shift key
					code=keyboard.shift
				elif(x>1020 and x<=1200):
					#it is the del key
					code=keyboard.delete
			elif(y>300 and y<=400):
				#row4
				if(x>=0 and x<240):
					code=keyboard.keyboard_down
				elif(x>=240 and x<420):
					code=keyboard.symbols_toggle
				elif(x>=420 and x<780):
					code=keyboard.space
				elif(x>=780 and x<960):
					code=keyboard.symbol_comma
				elif(x>=960 and x<=1200):
					code=keyboard.symbol_enter

		#print str(code) + ' '+str(x) + ' '+str(y)
		return code


	#returns the equivilent string based on the character code defined in this class
	def to_string(self, code):
		string_equivilent = ''

		if(code==keyboard.q):
			string_equivilent = 'q'

		if(code==keyboard.w):
			string_equivilent = 'w'

		if(code==keyboard.e):
			string_equivilent = 'e'

		if(code==keyboard.r):
			string_equivilent = 'r'

		if(code==keyboard.t):
			string_equivilent = 't'

		if(code==keyboard.y):
			string_equivilent = 'y'

		if(code==keyboard.u):
			string_equivilent = 'u'

		if(code==keyboard.i):
			string_equivilent = 'i'

		if(code==keyboard.o):
			string_equivilent = 'o'

		if(code==keyboard.p):
			string_equivilent = 'p'

		if(code==keyboard.a):
			string_equivilent = 'a'

		if(code==keyboard.s):
			string_equivilent = 's'

		if(code==keyboard.d):
			string_equivilent = 'd'

		if(code==keyboard.f):
			string_equivilent = 'f'

		if(code==keyboard.g):
			string_equivilent = 'g'

		if(code==keyboard.h):
			string_equivilent = 'h'

		if(code==keyboard.j):
			string_equivilent = 'j'

		if(code==keyboard.k):
			string_equivilent = 'k'

		if(code==keyboard.l):
			string_equivilent = 'l'

		if(code==keyboard.z):
			string_equivilent = 'z'

		if(code==keyboard.x):
			string_equivilent = 'x'

		if(code==keyboard.c):
			string_equivilent = 'c'

		if(code==keyboard.v):
			string_equivilent = 'v'

		if(code==keyboard.b):
			string_equivilent = 'b'

		if(code==keyboard.n):
			string_equivilent = 'n'

		if(code==keyboard.m):
			string_equivilent = 'm'

		if(code==keyboard.shift):
			string_equivilent = 'shift'

		if(code==keyboard.delete):
			string_equivilent = 'del'

		if(code==keyboard.keyboard_down):
			string_equivilent = 'keyboard_down'

		if(code==keyboard.space):
			string_equivilent = 'space'

		if(code==keyboard.symbols_toggle):
			string_equivilent = '123'

		if(code==keyboard.search):
			string_equivilent = 'search'

		if(code==keyboard.number_one):
			string_equivilent = '1'

		if(code==keyboard.number_two):
			string_equivilent = '2'

		if(code==keyboard.number_three):
			string_equivilent = '3'

		if(code==keyboard.number_four):
			string_equivilent = '4'

		if(code==keyboard.number_five):
			string_equivilent = '5'

		if(code==keyboard.number_six):
			string_equivilent = '6'

		if(code==keyboard.number_seven):
			string_equivilent = '7'

		if(code==keyboard.number_eight):
			string_equivilent = '8'

		if(code==keyboard.number_nine):
			string_equivilent = '9'

		if(code==keyboard.number_zero):
			string_equivilent = '0'

		if(code==keyboard.symbol_at):
			string_equivilent = '@'

		if(code==keyboard.symbol_pound):
			string_equivilent = '#'

		if(code==keyboard.symbol_money):
			string_equivilent = '$'

		if(code==keyboard.symbol_percent):
			string_equivilent = '%'

		if(code==keyboard.symbol_ampersand):
			string_equivilent = '&'

		if(code==keyboard.symbol_asterisk):
			string_equivilent = '*'

		if(code==keyboard.symbol_dash):
			string_equivilent = '-'

		if(code==keyboard.symbol_equals):
			string_equivilent = '='

		if(code==keyboard.symbol_left_parenthesis):
			string_equivilent = '('

		if(code==keyboard.symbol_right_parenthesis):
			string_equivilent = ')'

		if(code==keyboard.symbol_exclamation):
			string_equivilent = '!'

		if(code==keyboard.symbol_double_quote):
			string_equivilent = '\"'

		if(code==keyboard.symbol_apostrophe):
			string_equivilent = '\''

		if(code==keyboard.symbol_colon):
			string_equivilent = ':'

		if(code==keyboard.symbol_semicolon):
			string_equivilent = ';'

		if(code==keyboard.symbol_forward_slash):
			string_equivilent = '/'

		if(code==keyboard.symbol_question_mark):
			string_equivilent = '?'

		if(code==keyboard.symbol_period):
			string_equivilent = '.'

		if(code==keyboard.symbol_comma):
			string_equivilent = ','

		return string_equivilent


	#character codes... do not change these, or else
	q=0
	w=1
	e=2
	r=3
	t=4
	y=5
	u=6
	i=7
	o=8
	p=9
	a=10
	s=11
	d=12
	f=13
	g=14
	h=15
	j=16
	k=17
	l=18
	z=20
	x=21
	c=22
	v=23
	b=24
	n=25
	m=26
	shift=50
	delete=51
	keyboard_down=52
	space=53
	symbols_toggle=54
	search=55
	number_one=100
	number_two=101
	number_three=102
	number_four=103
	number_five=104
	number_six=105
	number_seven=106
	number_eight=107
	number_nine=108
	number_zero=109
	symbol_at=110
	symbol_pound=111
	symbol_money=112
	symbol_percent=113
	symbol_ampersand=114
	symbol_asterisk=115
	symbol_dash=116
	symbol_equals=117
	symbol_left_parenthesis=118
	symbol_right_parenthesis=119
	symbol_exclamation=120
	symbol_double_quote=121
	symbol_apostrophe=122
	symbol_colon=123
	symbol_semicolon=124
	symbol_forward_slash=125
	symbol_question_mark=126
	symbol_period=56
	symbol_comma=57
	symbol_enter=58