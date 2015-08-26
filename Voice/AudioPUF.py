from __future__ import division
import math
import numpy
import scipy
import os
#import SoundUtil
from os.path import join
import io


# What % longer or shorter a sample can be before being summarily rejected.
TIME_THRESHOLD = 0.15
# How many standard deviations away a point can be without failing
DEVS = 2
# What % of points need to pass for verification
PASS_LIMIT = 0.8


# Returns an array of (start, end) sample locations of each
# "token" in the clip. We define token here as a distinct period
# of intentional high amplitude in the recording, mainly corresponding
# to a word or string of words with no real pause between them.
# Variables N and amp_threshold can be tweaked to change output
# accuracy
# Param: pcmFile - path of soundfile to be analyzed
def findTokens(pcmFile):
    #Opens the file into an array
    data = numpy.memmap(pcmFile, dtype='int16')
    
    
    #transform window size
    N = 200
    
    #amplitude threshold discerning token from silence
    amp_threshold = 200
    
    
    
    min_token_size = 200
    clip_start = 2000
    
    token_arr = []
    tokens = []
    
    data = abs(data)    
    
    #low-pass filter
    for i in range(N, len(data)-N):
        stat = []
        for j in range (1, N):
            stat.append(data[i+j])
            stat.append(data[i-j])
        data[i] = numpy.mean(stat)
        
    state = 'OUT'
    for i in range(clip_start, len(data)-N):
        if state == 'OUT':
            if data[i] > amp_threshold:
                state = 'IN'
                token_arr.append(i+N)
                
        if state == 'IN':
            if data[i] < amp_threshold:
                state = 'OUT'
                token_arr.append(i+N)
                
    for i in xrange(0, len(token_arr), 2):
        if (token_arr[i+1] - token_arr[i]) < min_token_size:
            continue
        else:
            tokens.append( (token_arr[i], token_arr[i+1]) )
            print 'start: %d   end: %d' % (token_arr[i], token_arr[i+1])
        
    return tokens
    
#Expands/Contracts lists of data samples to all be of length N, for profiling
def buildProfile(dataSets):
      avg_samples = 0
      
      #find average number of samples in the data
      for dataSet in dataSets:
		avg_samples += len(dataSet)
      avg_samples = int(avg_samples/len(dataSets))
      print 'average samples %d' % avg_samples
      sample_period = 1/22050
      pre_profile = []
	#for each clip in raw profile
      for clip in range( len(dataSets) ):
             print 'Processing clip %d' % clip
             #calculate new sample period
             num_samples = len(dataSets[clip])
             new_period = sample_period * ( avg_samples / num_samples)
             new_data = []
             for sample in range(0, avg_samples):
                 
                 #catch first index
                 if sample == 0:
                     new_data.append( dataSets[clip][sample] )
                     
                 else:
                     #the time of the sample we want to interpolate for
                     sample_time = sample * sample_period
                     
                     #the index of that sample in the time-shifted data set
                     shifted_index = sample_time / new_period
                     
                     #catch the case of the shifted index being an integer
                     if shifted_index % 1 == 0:
                         data = dataSets[clip][int(shifted_index)]
                         
                     #otherwise find index to the left shifted index
                     else:
                         left_sample = math.floor( shifted_index )
                         
                         #catch last index
                         if left_sample == len(dataSets[clip]) - 1:
                             data = dataSets[clip][left_sample]
                         
                         else:
                             #and to the right
                             right_sample = left_sample + 1
                             
                             #then interpolate
                             data = dataSets[clip][left_sample] + (dataSets[clip][right_sample] - dataSets[clip][left_sample]) * ( sample_time - left_sample*new_period ) / ( right_sample*new_period - left_sample*new_period )
                    
                    
                     new_data.append(data)
             pre_profile.append(new_data)
	
      profile = []
      for normalized_sample in range(0, avg_samples):
          if normalized_sample % 500 == 0:
              print 'Adding processed data to profile: %d / %d' % (normalized_sample, avg_samples)
          stat = []
          
          #for each sample in n, add ith sample from all clips to stat array 
          for clip in range( len(pre_profile) ):
              for sample in range(len(pre_profile[clip])):
                  if sample == normalized_sample:
                      stat.append(pre_profile[clip][sample])
                      break
                  
          #perform statisical analysis on ith sample from each clip
          mean = numpy.mean(stat)
          stdev = numpy.std(stat)
          
          #add that sample's data to profile
          profile.append( (mean, stdev) )
  
      return profile


#Takes a profile and a new set of adjusted length and determines if it passes
#Could also add the sample to the profile if it passes, to allow learning
def verifyUser(profile_path, test_path):    
    AMPLITUDE = 0   #index of amplitude in profile data
    STDDEV = 1      #index of std_dev in profile data    
    passedPoints = 0
    
    print 'Loading profile'
    profile_samples = 0
    with open(profile_path) as proFile:    
        for line in proFile: # read rest of lines
            profile_samples += 1
    
    proFile.close()
            
    print 'Loading test case'
    print '   Trimming'
    #startEnd = findStartEnd(test_path, 2000)
    tokens = findTokens(test_path)
    test_data = numpy.memmap(test_path, dtype='int16')
    start = tokens[0][0]
    end = tokens[len(tokens)-1][1]    
    while test_data.size > end:
        test_data = numpy.delete(test_data, test_data.size - 1)
    while start > 0:
        test_data = numpy.delete(test_data, 0)
        start -= 1
        
#    if (startEnd[0] != -1):
#        test_data = numpy.memmap(test_path, dtype='int16')
#        while test_data.size > startEnd[1]:
#            test_data = numpy.delete(test_data, test_data.size - 1)
#        end = startEnd[0]
#        while end > 0:
#            test_data = numpy.delete(test_data, 0)
#            end = end - 1
#    else: print 'startEnd Error'


    print '   Calculating test\'s shifted sample period'
    sample_period = 1/22050
    test_samples = len(test_data)
    new_period = sample_period * ( profile_samples / test_samples)
    
    
    print '   Interpolating test case data'
    new_data = [] 
    for sample in range(0, profile_samples):
        if sample == 0:
            new_data.append( test_data[sample] )
        else:
            #linear interpolation
            sample_time = sample * sample_period
            shifted_index = sample_time / new_period
            if shifted_index % 1 == 0:
                data = test_data[int(shifted_index)]
            else:
                left_sample = int( math.floor( shifted_index ) )
                if left_sample == test_samples - 1:
                    data = test_data[left_sample]
                else:
                    right_sample = left_sample + 1
                    data = test_data[left_sample] + (test_data[right_sample] - test_data[left_sample]) * ( sample_time - left_sample*new_period ) / ( right_sample*new_period - left_sample*new_period )
            new_data.append(data)
                
    
    print 'Verifying test against sample'
    i = 0
    with open(profile_path) as proFile:    
        for line in proFile: # read rest of lines
            profile_data = ([float(x) for x in line.split()])     
            if (new_data[i] > (profile_data[AMPLITUDE] - DEVS*profile_data[STDDEV]) and
                new_data[i] < (profile_data[AMPLITUDE] + DEVS*profile_data[STDDEV])):
                passedPoints += 1
            i += 1
                
    print 'Required accuracy is %.2f %% of samples within a range of %.2f standard deviations from the profile' % (PASS_LIMIT*100, DEVS)
    print 'Test is %.2f %% accurate' % ((passedPoints / profile_samples) * 100)
    return ((passedPoints / profile_samples) * 100)
    #if passedPoints > (profile_samples * PASS_LIMIT):
        #print 'PASSED'
        #return True
    #print 'FAILED'
    #return False

# Creates a PUF profile from the given d, consisting n datapoints
# n is the average number of samples across all data in path
# datapoints are a tuple - (mean value of sample i, std deviation of sample i)
# path: path of a directory containing .pcm data - same challenge/user/device
def pcmProfile(path):
    for root, dirs, files in os.walk(path):
        if(len(files) != 0):
            files.sort()
            dataSets = []
            for fileName in files:
                print 'Trimming file %s' % fileName
                filePath = join(root, fileName)
                tokens = findTokens(filePath)
                data = numpy.memmap(filePath, dtype='int16')
                start = tokens[0][0]
                end = tokens[len(tokens)-1][1]    
                while data.size > end:
                    data = numpy.delete(data, data.size - 1)
                while start > 0:
                    data = numpy.delete(data, 0)
                    start -= 1
                
                dataSets.append(data)
                    
                    
#                startEnd = findStartEnd(join(root, fileName), 2000)
#                if (startEnd[0] != -1):
#                    data = numpy.memmap(filePath, dtype='int16')
#                    while data.size > startEnd[1]:
#                        data = numpy.delete(data, data.size - 1)
#                    start = startEnd[0]
#                    while start > 0:
#                        data = numpy.delete(data, 0)
#                        start -= 1
#                    dataSets.append(data)

    print 'Done Trimming'
    
    print 'Building Profile'
    profile = buildProfile(dataSets)
    

    out_path = os.path.join(path, "profile.p")
    proFile = io.open(out_path, 'wb')
    
    print 'Writing Profile to %s' % out_path
    numpy.savetxt(proFile, profile, fmt='%f')              


def testProfile(profile_path, data_path):
     
    i = 1
    result = []
    num_tests = len(os.listdir(data_path))
    for file in os.listdir(data_path):
        if file.endswith(".pcm"):
            print 'Testing %d / %d' % (i, num_tests)
            result.append(verifyUser(profile_path, data_path + "\\" + file))
            print ''
            print ''
            i += 1
        
    mean = numpy.mean(result)
    print 'On Average, %d%% of the data was within acceptable range' % mean
    
    
# def graphTest(profile):
    # #Create figure to graph with
    # fig = figure(figsize=(16,12))
    # fig.suptitle("Jacob's Test")
    # #Setup plot of path traced
    # #subplot(1,2,1)
    # #xlim(0,800)
    # #ylim(1280,0)
    # #title("Challenge/Response Path")
    # #xlabel("X location (pixels)")
    # #ylabel("Y location (pixels)")
    # #CX = np.array(challengeX)
    # #CY = np.array(challengeY)
    # #plot(CX, CY, color='green', linewidth=2, linestyle="--", label="Generated Challenge")
    # #RX = np.array(respX)
    # #RY = np.array(respY)
    # #plot(RX, RY, color='blue', linewidth=2, label="User Response")
    # #annotate("Start", xy=(challengeX[0], challengeY[0]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
    # #annotate("End", xy=(challengeX[-1], challengeY[-1]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
    # #legend(loc='upper left')
    # #Setup plot of pressure data
    # subplot(1,2,1)
    # title("Overlay")
    # xlabel("Points")
    # ylabel("Magnitude")
    # maximum = 0
    # averages = [0 for i in range(len(profile))]
    # stddevs = [0 for i in range(len(profile))]
    # for point in profile:
        # if point.avg > maximum:
            # maximum = point.avg
    # i = 0
    # xlim(0, len(profile))
    # ylim(0, 1.0)
    # for point in profile:
        # averages[i] = point.avg
        # stddevs[i] = point.stddev
        # i = i + 1
    # posDevs = [averages[i] + (2 * stddevs[i]) for i in range(len(profile))]
    # negDevs = [averages[i] - (2 * stddevs[i]) for i in range(len(profile))]
    # PTS = np.linspace(1,len(profile), len(profile))
    # plot(PTS, averages, color='blue', linewidth=2, label="Averages")
    # plot(PTS, posDevs, color='red', linewidth=2, label="+2 devs")
    # plot(PTS, negDevs, color='green', linewidth=2, label="-2 devs")
    # legend(loc='lower right')
    # show()
