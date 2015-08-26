import binascii
import os
from os.path import join, dirname, basename
from math import sqrt, atan, sin, cos, fabs
from pylab import *
import csv

def hammingDistance(byteArr1, byteArr2):
    if(len(byteArr1) != len(byteArr2)):
        raise ValueError("Undefined for sequences of unequal length")

    hammDist = 0
    for i in range(0, len(byteArr1)):
        for j in range(0, 8):
            bit1 = byteArr1[i] & (1 << j)
            bit2 = byteArr2[i] & (1 << j)
            if( bit1 != bit2 ):
                hammDist += 1

    return hammDist

def simpleMovingAverage(inputList, n=5):
    returnList = []
    for i in range(0, len(inputList)):
        floor = i-n/2
        if floor < 0:
            floor = 0
        if n%2 == 1:
            spliceList = inputList[floor:(i+n/2+1)]
            returnList.append(sum(spliceList, dtype="float")/float(len(spliceList)))
        else:
            spliceList = inputList[floor:(i+n/2)]
            returnList.append(sum(spliceList, dtype="float")/float(len(spliceList)))
    return returnList

def cumulativeMovingAverage(inputList):
    cumulativeMovAvg = []
    for i in range(0, len(inputList)):
        if i == 0:
            cumulativeMovAvg.append(float(inputList[0]))
        else:
            cumulAvg = (cumulativeMovAvg[i-1]*float(i-1) + float(inputList[i]))/float(i)
            cumulativeMovAvg.append(cumulAvg)
    return cumulativeMovAvg

def distBetweenPts(pt1=(0,0), pt2=(0,0)):
    return sqrt(pow(pt1[0]-pt2[0], 2) + pow(pt1[1]-pt2[1], 2))

def calcNextPt(lastPt=(0,0), targetPt=(0,0), increment=0):
    if(lastPt[1] > targetPt[1]):
        increment = -increment

    deltaX = 0
    deltaY = 0
    if(lastPt[1] == targetPt[1]): #tan(X/0) is NaN
        if(lastPt[0] > targetPt[0]):
            deltaX = -increment
        else:
            deltaX = increment #TODO fixed this from deltax to deltaX. Could be a bad bug
    else:
        angle = atan((lastPt[0]-targetPt[0])/(lastPt[1]-targetPt[1]))
        deltaX = increment * sin(angle)
        deltaY = increment * cos(angle)

    nextPt = (lastPt[0]+deltaX, lastPt[1]+deltaY)
    return nextPt

class DataList:
    def __init__(self, fileName, testerName, deviceName, seed, challengeList, respList, pressureList):
        self.fileName = fileName
        self.testerName = testerName
        self.deviceName = deviceName
        self.seed = seed
        self.challengeList = challengeList
        self.respList = respList
        self.pressureList = pressureList

    def __repr__(self):
        return repr((self.fileName, self.testerName, self.deviceName, self.seed, self.challengeList, self.respList, self.pressureList))


def getRawDataLists(directory="/home/rascheel/git/PUFProject/OutputCSVs/"):
    dataLists = [] #list of pressures using DataList class

    # Walk through all the lower directories
    for root, dirs, files in os.walk(directory):
        # We only care about directories that have files
        if(len(files) != 0):
            files.sort()

            # The directories with files have the .csv files
            # Get the pressure data from them and store in list
            for fileName in files:
                challengeList = []
                pressureList = []
                respList = []
                testerName = ""
                deviceName = ""
                seed = ""
                seed = int(fileName.split(":")[0])
                with open(join(root, fileName), "rb") as csvfile:
                    respReader = csv.reader(csvfile)
                    responseStarted = False
                    for row in respReader:
                        if(responseStarted):
                            respX = float(row[0])
                            respY = float(row[1])
                            respList.append((respX, respY)) #Append the points as x,y tuples
                            pressureList.append(float(row[2]))
                        elif(row[0] != "ChallengeX" and row[0] != "X"):
                            challX = float(row[0])
                            challY = float(row[1])
                            challengeList.append((challX, challY))
                            testerName = row[2]
                            deviceName = row[3]

                        if(row[0] == "X" and row[1] == "Y" and row[2] == "PRESSURE"):
                            responseStarted = True
                dataLists.append(DataList(fileName, testerName, deviceName, seed, challengeList, respList, pressureList))
    return dataLists

def interpolatedPressure(n=32, dataList=DataList("", "", "", 0, [], [], [])):
    testername = dataList.testerName
    deviceName = dataList.deviceName
    seed = dataList.seed
    challengeList = dataList.challengeList
    respList = dataList.respList
    pressureList = dataList.pressureList

    challengeLength = 0
    isX = False
    for i in range(0, len(challengeList)-1):
        yLength = fabs(challengeList[i][1] - challengeList[i+1][1])
        xLength = fabs(challengeList[i][0] - challengeList[i+1][0])
        if(xLength > yLength):
            challengeLength = xLength
            isX = True
        else:
            challengeLength = yLength
            isX = False
    normalizedDist = challengeLength/float(n)

    normalizedList = []
    currPt = challengeList[0]
    for i in range(0, n):
        normalizedList.append(currPt)
        if(isX):
            currPt = (currPt[0]+normalizedDist, currPt[1])
        else:
            currPt = (currPt[0], currPt[1]+normalizedDist)

    normalizedPressList = []
    for normPt in normalizedList:
        if(isX):
            closestLeftPt = respList[0]
            closestRightPt = respList[-1]
            closestLeftPtInd = 0
            closestRightPtInd = -1
            if(respList[0][0] > normPt[0]):
                closestLeftPt = respList[-1]
                closestRightPt = respList[0]
                closestLeftPtInd = -1
                closestRightPtInd = 0

            for i in range(0, len(respList)):
                if(respList[i][0] <= normPt[0]): #if it's to the left
                    if((normPt[0] - respList[i][0]) < (normPt[0] - closestLeftPt[0])):
                        closestLeftPt = respList[i]
                        closestLeftPtInd = i
                if(respList[i][0] > normPt[0]): #if it's to the right
                    if((respList[i][0]-normPt[0]) < (closestRightPt[0]-normPt[0])):
                        closestRightPt = respList[i]
                        closestRightPtInd = i

            if(closestRightPt == closestLeftPt):
                closestLeftPtInd = closestLeftPtInd - 1
                closestLeftPt = respList[closestLeftPtInd]

            deltaX = closestRightPt[0]-closestLeftPt[0]
            deltaY = closestRightPt[1]-closestLeftPt[1]
            angle = fabs(atan(deltaY/deltaX))

            deltaX1 = normPt[0]-closestLeftPt[0]
            h1 = deltaX1/cos(angle)

            h = deltaX/cos(angle)
            h2 = h-h1

            interpPress = (h1/h)*pressureList[closestLeftPtInd]+(h2/h)*pressureList[closestRightPtInd]
            normalizedPressList.append(interpPress)
        else:
            closestLowerPt = respList[0]
            closestUpperPt = respList[-1]
            closestLowerPtInd = 0
            closestUpperPtInd = -1
            if(respList[0][1] > normPt[1]):
                closestLowerPt = respList[-1]
                closestUpperPt = respList[0]
                closestLowerPtInd = -1
                closestUpperPtInd = 0

            for i in range(0, len(respList)):
                if(respList[i][1] <= normPt[1]): #if it's below
                    if((normPt[1] - respList[i][1]) < (normPt[1] - closestLowerPt[1])):
                        closestLowerPt = respList[i]
                        closestLowerPtInd = i
                if(respList[i][1] > normPt[1]): #if it's above
                    if((respList[i][1]-normPt[1]) < (closestUpperPt[1]-normPt[1])):
                        closestUpperPt = respList[i]
                        closestUpperPtInd = i

            if(closestUpperPt == closestLowerPt):
                closestLowerPtInd = closestLowerPtInd - 1
                closestLowerPt = respList[closestLowerPtInd]

            deltaX = closestUpperPt[0]-closestLowerPt[0]
            deltaY = closestUpperPt[1]-closestLowerPt[1]
            angle = fabs(atan(deltaX/deltaY))

            deltaY1 = normPt[1]-closestLowerPt[1]
            h1 = deltaY1/cos(angle)

            h = deltaY/cos(angle)
            h2 = h-h1

            interpPress = (h1/h)*pressureList[closestLowerPtInd]+(h2/h)*pressureList[closestUpperPtInd]
            normalizedPressList.append(interpPress)

    return normalizedPressList

def meanAndStdDev(inputList=[]):
    #Calculate sample mean
    mean = 0.0
    for num in inputList:
        mean = mean + float(num)
    mean = mean / float(len(inputList))

    #Calculate sample standard deviation
    variance = 0.0
    for num in inputList:
        variance = variance + (float(num)-mean)**2
    variance = variance / float(len(inputList))
    stdDev = sqrt(variance)

    return (mean, stdDev)

# Returns the number of points which did not match the distribution
#
# allowedDevation is the number of standard deviations a point can vary from 
# the mean and still pass the test.
def testPressListVsDistrib(pressureList=[], pressDistrib=[], allowedDeviation=1.0):
    shortLen = len(pressureList)
    if(len(pressDistrib) < shortLen):
        shortLen = len(pressDistrib)

    failedPoints = 0

    for i in range(0, shortLen):
        mean = pressDistrib[i][0]
        stdDev = pressDistrib[i][1]
        
        if(fabs(pressureList[i]-mean) > stdDev*allowedDeviation):
            failedPoints = failedPoints + 1

    return failedPoints

def genMeanList(pressDistrib=[], deviations=0.0):
    retList = []
    for distrib in pressDistrib:
        mean = distrib[0]
        stdDev = distrib[1]
        retList.append(mean+(deviations*stdDev))
    return retList

def genGraph(xArray=[], yArrays=[], yLabels=[], xLabel="", yLabel="", graphTitle="", savePath="."):

    fig = figure()
    title(graphTitle)
    xlabel(xLabel)
    ylabel(yLabel)
    ylim(0, 1.0)
    
    for i in range(0, len(yArrays)):
        yArray = yArrays[i]
        X = np.array(xArray)
        Y = np.array(yArray)
        plot(X,Y, linewidth=4, label=yLabels[i])

    legend(loc='upper right')

    att_path = os.path.join(savePath)
    if not os.path.exists(att_path):
        os.makedirs(att_path)
    att_path = os.path.join(att_path, graphTitle + ".png")
    savefig(att_path)
    #show()

