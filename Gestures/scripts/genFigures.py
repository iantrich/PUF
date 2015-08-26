import os
from os.path import join, dirname, basename

import csv
from util import simpleMovingAverage, cumulativeMovingAverage, distBetweenPts, calcNextPt

from math import sqrt, atan, sin, cos, fabs
from pylab import *

CSV_OUTPUT_DIR = "/home/nmont/Documents/GitRepos/PUFProject/OutputCSVs/"
COMBINED_FIGURE_OUTPUT_DIR = "/home/nmont/Documents/GitRepos/PUFProject/Figures/Combined/"
INDIVIDUAL_FIGURE_OUTPUT_DIR = "/home/nmont/Documents/GitRepos/PUFProject/Figures/Individual/"
OVERLAID_FIGURE_OUTPUT_DIR = "/home/nmont/Documents/GitRepos/PUFProject/Figures/Overlaid/"
NORMALIZED_STRAT2_OVERLAID_FIGURE_OUTPUT_DIR = "/home/nmont/Documents/GitRepos/PUFProject/Figures/Normalized_Strat2_Overlaid"
NORMALIZED_STRAT3_OVERLAID_FIGURE_OUTPUT_DIR = "/home/nmont/Documents/GitResos/PUFProject/Figures/Normalized_Strat3_Overlaid"
NORMALIZED_STRAT4_OVERLAID_FIGURE_OUTPUT_DIR = "/home/nmont/Documents/GitRepos/PUFProject/Figures/Normalized_Strat4_Overlaid"

def main():
    #genIndividualFigures()
    #genCombinedFigures()
    #genOverlaidFigure2Path()
    #genNormalizedStrat2OverlaidFigure2Path()
    #genNormalizedStrat3OverlaidFigure2Path()
    genNormalizedStrat4OverlaidFigure2Path()

def genIndividualFigures():
    # Walk through all the lower directories
    for root, dirs, files in os.walk(CSV_OUTPUT_DIR):
        # We only care about directories that have files
        if(len(files) != 0):
            files.sort()
            deviceName = basename(dirname(root))
            testerName = basename(root)
            print "Creating individual figures for device: %s, user: %s" % (deviceName, testerName)

            # The directories with files have the .csv files
            # Get the pressure data from them and store in list
            for fileName in files:
                with open(join(root, fileName), "rb") as csvfile:
                    respReader = csv.reader(csvfile)
                    dataStarted = False
                    challengeX = []
                    challengeY = []
                    testerName = ""
                    deviceName = ""
                    pathSeed = fileName.split(":")[0]
                    respX= []
                    respY= []
                    pressureList = []
                    for row in respReader:
                        if(dataStarted):
                            respX.append(float(row[0]))
                            respY.append(float(row[1]))
                            pressureList.append(float(row[2]))
                        elif(row[0] != "ChallengeX" and row[0] != "X"):
                            challengeX.append(float(row[0]))
                            challengeY.append(float(row[1]))
                            testerName = row[2]
                            deviceName = row[3]

                        if(row[0] == "X" and row[1] == "Y" and row[2] == "PRESSURE"):
                            dataStarted = True

                    #Calculate average pressure
                    avgPres = 0
                    for pres in pressureList:
                        avgPres = avgPres + pres
                    avgPres = avgPres / len(pressureList)

                    #Calculate simple moving average n = 5
                    movingAvg5 = simpleMovingAverage(pressureList, 5)

                    #Calculate simple moving average n = 10
                    movingAvg10 = simpleMovingAverage(pressureList, 10)

                    #Calculate cumulative moving average
                    cumulativeMovAvg = cumulativeMovingAverage(pressureList)

                    #Create figure to graph with
                    fig = figure(figsize=(16,12))
                    fig.suptitle("Path: %s\n Device: %s\n User: %s" % (pathSeed, deviceName, testerName))
                    #Setup plot of path traced
                    subplot(1,2,1)
                    xlim(0,800)
                    ylim(1280,0)
                    title("Challenge/Response Path")
                    xlabel("X location (pixels)")
                    ylabel("Y location (pixels)")
                    CX = np.array(challengeX)
                    CY = np.array(challengeY)
                    plot(CX, CY, color='green', linewidth=2, linestyle="--", label="Generated Challenge")
                    RX = np.array(respX)
                    RY = np.array(respY)
                    plot(RX, RY, color='blue', linewidth=2, label="User Response")
                    annotate("Start", xy=(challengeX[0], challengeY[0]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                    annotate("End", xy=(challengeX[-1], challengeY[-1]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                    legend(loc='upper left')
                    #Setup plot of pressure data
                    subplot(1,2,2)
                    title("Recorded Pressure")
                    xlabel("Points")
                    ylabel("Pressure")
                    ylim(0, 1.0)
                    PTS = np.linspace(1,len(pressureList), len(pressureList))
                    PRS = np.array(pressureList)
                    AP = np.linspace(avgPres, avgPres, len(pressureList))
                    MA5 = np.array(movingAvg5)
                    MA10 = np.array(movingAvg10)
                    CMA = np.array(cumulativeMovAvg)
                    plot(PTS, PRS, color='blue', linewidth=2, label="Pressure")
                    plot(PTS, AP, color='black', linewidth=2, linestyle="--", label="Average pressure")
                    plot(PTS, MA5, color='red', linewidth=2, linestyle="-", label="Moving average n = 5")
                    plot(PTS, MA10, color='cyan', linewidth=2, linestyle="-", label="Moving average n = 10")
                    plot(PTS, CMA, color='magenta', linewidth=2, linestyle="-", label="Cumulative moving average")
                    legend(loc='lower right')
                    #Copy the directory structure of the OutputCSVs folder
                    deviceName = basename(dirname(root))
                    testerName = basename(root)
                    #If the directory doesn't exist make it
                    att_path = os.path.join(INDIVIDUAL_FIGURE_OUTPUT_DIR, deviceName, testerName)
                    if not os.path.exists(att_path):
                        os.makedirs(att_path)
                    att_path = os.path.join(att_path, fileName.split(".")[0] + ".png")
                    #NOTE: Cannot show and save figure at same time
                    #show()
                    savefig(att_path)
                    close()

def genOverlaidFigure2Path():
    # Walk through all the lower directories
    for root, dirs, files in os.walk(CSV_OUTPUT_DIR):
        # We only care about directories that have files
        if(len(files) != 0):
            files.sort()
            deviceName = basename(dirname(root))
            testerName = basename(root)
            print "Creating overlaid figures for device: %s, user: %s" % (deviceName, testerName)

            # The directories with files have the .csv files
            # Get the pressure data from them and store in list
            challengeX_1 = []
            challengeY_1 = []
            respX_1 = []
            respY_1 = []
            pressureList_1 = []
            challengeX_2 = []
            challengeY_2 = []
            respX_2 = []
            respY_2 = []
            pressureList_2 = []
            pathSeed = []
            fileNames = []
            count = 0
            for fileName in files:
                count += 1
                pathSeed.append(fileName.split(":")[0])
                fileNames.append(fileName)
                with open(join(root, fileName), "rb") as csvfile:
                    respReader = csv.reader(csvfile)
                    dataStarted = False
                    for row in respReader:
                        if(dataStarted):
                            if count == 1:
                                respX_1.append(float(row[0]))
                                respY_1.append(float(row[1]))
                                pressureList_1.append(float(row[2]))
                            elif count == 2:
                                respX_2.append(float(row[0]))
                                respY_2.append(float(row[1]))
                                pressureList_2.append(float(row[2]))
                        elif(row[0] != "ChallengeX" and row[0] != "X"):
                            if count == 1:
                                challengeX_1.append(float(row[0]))
                                challengeY_1.append(float(row[1]))
                            elif count == 2:
                                challengeX_2.append(float(row[0]))
                                challengeY_2.append(float(row[1]))

                        if(row[0] == "X" and row[1] == "Y" and row[2] == "PRESSURE"):
                            dataStarted = True

                    if count == 2:
                        # -- Calculations for path 1
                        #Calculate average pressure
                        avgPres_1 = 0
                        for pres in pressureList_1:
                            avgPres_1 = avgPres_1 + pres
                        avgPres_1 = avgPres_1 / len(pressureList_1)

                        #Calculate simple moving average n = 5
                        movingAvg5_1 = simpleMovingAverage(pressureList_1, 5)

                        #Calculate simple moving average n = 10
                        movingAvg10_1 = simpleMovingAverage(pressureList_1, 10)

                        #Calculate cumulative moving average
                        cumulativeMovAvg_1 = cumulativeMovingAverage(pressureList_1)

                        # -- Calculations for path 2
                        #Calculate average pressure
                        avgPres_2 = 0
                        for pres in pressureList_2:
                            avgPres_2 = avgPres_2 + pres
                        avgPres_2 = avgPres_2 / len(pressureList_2)

                        #Calculate simple moving average n = 5
                        movingAvg5_2 = simpleMovingAverage(pressureList_2, 5)

                        #Calculate simple moving average n = 10
                        movingAvg10_2 = simpleMovingAverage(pressureList_2, 10)

                        #Calculate cumulative moving average
                        cumulativeMovAvg_2 = cumulativeMovingAverage(pressureList_2)
                        # -- done with calculations

                        #Create figure to graph with
                        fig = figure(figsize=(16,12))
                        fig.suptitle("Overlaid Paths: %s\n Device: %s\n User: %s" % (",".join(pathSeed),deviceName, testerName))

                        #Setup plot of path traced
                        subplot(1,2,1)
                        xlim(0,800)
                        ylim(1280,0)
                        title("Challenge/Response Path")
                        xlabel("X location (pixels)")
                        ylabel("Y location (pixels)")
                        #plot path 1
                        CX_1 = np.array(challengeX_1)
                        CY_1 = np.array(challengeY_1)
                        plot(CX_1, CY_1, color='blue', linewidth=2, linestyle="--", label="Path %s: Generated Challenge" % pathSeed[0])
                        RX_1 = np.array(respX_1)
                        RY_1 = np.array(respY_1)
                        plot(RX_1, RY_1, color='red', linewidth=2, label="Path %s: User Response" % pathSeed[0])
                        annotate("Start%s" % pathSeed[0], xy=(challengeX_1[0], challengeY_1[0]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        annotate("End%s" % pathSeed[0], xy=(challengeX_1[-1], challengeY_1[-1]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        #plot path 2
                        CX_2 = np.array(challengeX_2)
                        CY_2 = np.array(challengeY_2)
                        plot(CX_2, CY_2, color='green', linewidth=2, linestyle="--", label="Path %s: Generated Challenge" % pathSeed[1])
                        RX_2 = np.array(respX_2)
                        RY_2 = np.array(respY_2)
                        plot(RX_2, RY_2, color='magenta', linewidth=2, label="Path %s: User Response" % pathSeed[1])
                        annotate("Start%s" % pathSeed[1], xy=(challengeX_2[0], challengeY_2[0]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        annotate("End%s" % pathSeed[1], xy=(challengeX_2[-1], challengeY_2[-1]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        legend(loc='lower left')

                        #Setup plot of pressure data
                        subplot(1,2,2)
                        title("Recorded Pressure")
                        xlabel("Points")
                        ylabel("Pressure")
                        ylim(0, 1.0)
                        #plot pressure data for path 1
                        PTS_1 = np.linspace(1,len(pressureList_1), len(pressureList_1))
                        PRS_1 = np.array(pressureList_1)
                        AP_1 = np.linspace(avgPres_1, avgPres_1, len(pressureList_1))
                        MA5_1 = np.array(movingAvg5_1)
                        MA10_1 = np.array(movingAvg10_1)
                        CMA_1 = np.array(cumulativeMovAvg_1)
                        plot(PTS_1, PRS_1, color='red', linewidth=2, label="Path %s: Pressure" % pathSeed[0])
                        plot(PTS_1, MA5_1, color='blue', linewidth=2, linestyle="--", label="Path %s: Moving average n = 5" % pathSeed[0])
                        #plot pressure data for path 2
                        PTS_2 = np.linspace(1,len(pressureList_2), len(pressureList_2))
                        PRS_2 = np.array(pressureList_2)
                        AP_2 = np.linspace(avgPres_2, avgPres_2, len(pressureList_2))
                        MA5_2 = np.array(movingAvg5_2)
                        MA10_2 = np.array(movingAvg10_2)
                        CMA_2 = np.array(cumulativeMovAvg_2)
                        plot(PTS_2, PRS_2, color='magenta', linewidth=2, label="Path %s: Pressure" % pathSeed[1])
                        plot(PTS_2, MA5_2, color='green', linewidth=2, linestyle="--", label="Path %s: Moving average n = 5" % pathSeed[1])
                        #Done plotting pressure data
                        legend(loc='lower right')
                        #Copy the directory structure of the OutputCSVs folder
                        deviceName = basename(dirname(root))
                        testerName = basename(root)
                        #If the directory doesn't exist make it
                        att_path = os.path.join(OVERLAID_FIGURE_OUTPUT_DIR, deviceName, testerName)
                        if not os.path.exists(att_path):
                            os.makedirs(att_path)
                        att_path = os.path.join(att_path, "OverlaidFiles = ('%s', '%s').png" % (fileNames[0], fileNames[1]))
                        #NOTE: Cannot show and save figure at same time
                        #show()
                        savefig(att_path)
                        close()
                        #clear lists
                        challengeX_1 = []
                        challengeY_1 = []
                        respX_1 = []
                        respY_1 = []
                        pressureList_1 = []
                        challengeX_2 = []
                        challengeY_2 = []
                        respX_2 = []
                        respY_2 = []
                        pressureList_2 = []
                        pathSeed = []
                        fileNames = []
                        count = 0


def genCombinedFigures():
    # Walk through all the lower directories
    for root, dirs, files in os.walk(CSV_OUTPUT_DIR):
        # We only care about directories that have files
        if(len(files) != 0):
            files.sort()
            deviceName = basename(dirname(root))
            testerName = basename(root)
            print "Creating combined figures for device: %s, user: %s" % (deviceName, testerName)

            # The directories with files have the .csv files
            # Get the pressure data from them and store in list
            challengeX = []
            challengeY = []
            respX= []
            respY= []
            pressureList = []
            pathSeed = []
            count = 0
            for fileName in files:
                count += 1
                pathSeed.append(fileName.split(":")[0])
                with open(join(root, fileName), "rb") as csvfile:
                    respReader = csv.reader(csvfile)
                    dataStarted = False
                    for row in respReader:
                        if(dataStarted):
                            respX.append(float(row[0]))
                            respY.append(float(row[1]))
                            pressureList.append(float(row[2]))
                        elif(row[0] != "ChallengeX" and row[0] != "X"):
                            challengeX.append(float(row[0]))
                            challengeY.append(float(row[1]))
                            testerName = row[2]
                            deviceName = row[3]

                        if(row[0] == "X" and row[1] == "Y" and row[2] == "PRESSURE"):
                            dataStarted = True

                    if count == 10:#len(files):
                        #Calculate average pressure
                        avgPres = 0
                        for pres in pressureList:
                            avgPres = avgPres + pres
                        avgPres = avgPres / len(pressureList)

                        #Calculate simple moving average n = 5
                        movingAvg5 = simpleMovingAverage(pressureList, 5)

                        #Calculate simple moving average n = 10
                        movingAvg10 = simpleMovingAverage(pressureList, 10)

                        #Calculate cumulative moving average
                        cumulativeMovAvg = cumulativeMovingAverage(pressureList)

                        #Create figure to graph with
                        fig = figure(figsize=(16,12))
                        fig.suptitle("Combined Paths: %s\n Device: %s\n User: %s" % (",".join(pathSeed),deviceName, testerName))
                        #Setup plot of pressure data with averages
                        title("Recorded Pressure")
                        xlabel("Points")
                        ylabel("Pressure")
                        ylim(0, 1.0)
                        PTS = np.linspace(1,len(pressureList), len(pressureList))
                        PRS = np.array(pressureList)
                        AP = np.linspace(avgPres, avgPres, len(pressureList))
                        MA5 = np.array(movingAvg5)
                        MA10 = np.array(movingAvg10)
                        CMA = np.array(cumulativeMovAvg)
                        plot(PTS, PRS, color='blue', linewidth=2, label="Pressure")
                        plot(PTS, AP, color='black', linewidth=2, linestyle="--", label="Average pressure")
                        plot(PTS, MA5, color='red', linewidth=2, linestyle="-", label="Moving average n = 5")
                        plot(PTS, MA10, color='cyan', linewidth=2, linestyle="-", label="Moving average n = 10")
                        plot(PTS, CMA, color='magenta', linewidth=2, linestyle="-", label="Cumulative moving average")
                        legend(loc='lower right')
                        #Copy the directory structure of the OutputCSVs folder
                        deviceName = basename(dirname(root))
                        testerName = basename(root)
                        #If the directory doesn't exist make it
                        att_path = os.path.join(COMBINED_FIGURE_OUTPUT_DIR, deviceName, testerName)
                        if not os.path.exists(att_path):
                            os.makedirs(att_path)
                        att_path = os.path.join(att_path, "Combined Paths: %s; Device: %s; User: %s" % (",".join(pathSeed),deviceName, testerName))
                        #NOTE: Cannot show() and savefig() at same time
                        #show()
                        savefig(att_path)
                        #Close figure to cleanup memory
                        close()
                        #clear lists
                        challengeX = []
                        challengeY = []
                        respX= []
                        respY= []
                        pathSeed = []
                        pressureList = []
                        count = 0

def genNormalizedStrat2OverlaidFigure2Path():
    # Walk through all the lower directories
    for root, dirs, files in os.walk(CSV_OUTPUT_DIR):
        # We only care about directories that have files
        if(len(files) != 0):
            files.sort()
            deviceName = basename(dirname(root))
            testerName = basename(root)
            print "Creating normalized overlaid figures for device: %s, user: %s" % (deviceName, testerName)

            # The directories with files have the .csv files
            # Get the pressure data from them and store in list
            challengeX_1 = []
            challengeY_1 = []
            respX_1 = []
            respY_1 = []
            pressureList_1 = []
            challengeX_2 = []
            challengeY_2 = []
            respX_2 = []
            respY_2 = []
            pressureList_2 = []
            pathSeed = []
            fileNames = []
            count = 0
            for fileName in files:
                seed = int(fileName.split(":")[0])
                #Only seeds above 1000 are extra long paths likely to create
                #bit strings of atleast 128 bits
                if( seed > 1000 ):
                    count += 1
                    pathSeed.append(fileName.split(":")[0])
                    fileNames.append(fileName)
                    challengeList = []
                    pressureList = []
                    respList = []
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
                    challengeLength = 0
                    for i in range(0, len(challengeList)-1):
                        challengeLength += distBetweenPts(challengeList[i], challengeList[i+1])
                    normalizedDist = challengeLength/32.0

                    normalizedList = []
                    currPt = challengeList[0]
                    for i in range(0, len(challengeList)-1):
                        currPt = challengeList[i]
                        nextPt = challengeList[i+1]

                        while(distBetweenPts(currPt, nextPt) > normalizedDist):
                            normalizedList.append(currPt)
                            currPt = calcNextPt(currPt, nextPt, normalizedDist)
                    normalizedList.append(challengeList[-1])

                    normalizedPressList = []
                    print len(respList)
                    lastPtIndex = 0
                    for normPt in normalizedList:
                        lastIndexChanged = False
                        closestPt = respList[lastPtIndex]
                        closestPtDist = distBetweenPts(normPt, closestPt)
                        if(len(respList[lastPtIndex:lastPtIndex+100]) < 100):
                            lastPtIndex = len(respList) - 100 #I always want to ensure we atleast search through 100 points
                        for i in range(0, len(respList)):#range(lastPtIndex, lastPtIndex+100): #The assumption is the next closest point won't be 100 points away from the last one. This reduces search time and prevents the situation of two intersecting pressure lines mismatching
                            if(distBetweenPts(normPt, respList[i]) <= closestPtDist):
                                closestPtDist = distBetweenPts(normPt, respList[i])
                                closestPt = respList[i]
                                if(i != lastPtIndex):
                                    lastPtIndex = i
                                    lastIndexChanged = True
                        print "%s, lastPtInd = %d, normPt = %s, closestPt = %s, dist = %f" % (lastIndexChanged, lastPtIndex, normPt, closestPt, closestPtDist)
                        normalizedPressList.append(pressureList[lastPtIndex])
                        if not lastIndexChanged:
                            lastPtIndex = lastPtIndex+1

                    if(count == 1):
                        for respPt in respList:
                            respX_1.append(respPt[0])
                            respY_1.append(respPt[1])
                        for normPressPt in normalizedPressList:
                            pressureList_1.append(normPressPt)
                        for challPt in challengeList:
                            challengeX_1.append(challPt[0])
                            challengeY_1.append(challPt[1])
                    elif(count == 2):
                        for respPt in respList:
                            respX_2.append(respPt[0])
                            respY_2.append(respPt[1])
                        for normPressPt in normalizedPressList:
                            pressureList_2.append(normPressPt)
                        for challPt in challengeList:
                            challengeX_2.append(challPt[0])
                            challengeY_2.append(challPt[1])

                    if count == 2:
                        # -- Calculations for path 1
                        #Calculate average pressure
                        avgPres_1 = 0
                        for pres in pressureList_1:
                            avgPres_1 = avgPres_1 + pres
                        avgPres_1 = avgPres_1 / len(pressureList_1)

                        #Calculate simple moving average n = 5
                        movingAvg5_1 = simpleMovingAverage(pressureList_1, 5)

                        #Calculate simple moving average n = 10
                        movingAvg10_1 = simpleMovingAverage(pressureList_1, 10)

                        #Calculate cumulative moving average
                        cumulativeMovAvg_1 = cumulativeMovingAverage(pressureList_1)

                        # -- Calculations for path 2
                        #Calculate average pressure
                        avgPres_2 = 0
                        for pres in pressureList_2:
                            avgPres_2 = avgPres_2 + pres
                        avgPres_2 = avgPres_2 / len(pressureList_2)

                        #Calculate simple moving average n = 5
                        movingAvg5_2 = simpleMovingAverage(pressureList_2, 5)

                        #Calculate simple moving average n = 10
                        movingAvg10_2 = simpleMovingAverage(pressureList_2, 10)

                        #Calculate cumulative moving average
                        cumulativeMovAvg_2 = cumulativeMovingAverage(pressureList_2)
                        # -- done with calculations

                        #Create figure to graph with
                        fig = figure(figsize=(16,12))
                        fig.suptitle("Normalized Overlaid Paths: %s\n Device: %s\n User: %s" % (",".join(pathSeed),deviceName, testerName))

                        #Setup plot of path traced
                        subplot(1,2,1)
                        xlim(0,800)
                        ylim(1280,0)
                        title("Challenge/Response Path")
                        xlabel("X location (pixels)")
                        ylabel("Y location (pixels)")
                        #plot path 1
                        CX_1 = np.array(challengeX_1)
                        CY_1 = np.array(challengeY_1)
                        plot(CX_1, CY_1, color='blue', linewidth=2, linestyle="--", label="Path %s: Generated Challenge" % pathSeed[0])
                        RX_1 = np.array(respX_1)
                        RY_1 = np.array(respY_1)
                        plot(RX_1, RY_1, color='red', linewidth=2, label="Path %s: User Response" % pathSeed[0])
                        annotate("Start%s" % pathSeed[0], xy=(challengeX_1[0], challengeY_1[0]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        annotate("End%s" % pathSeed[0], xy=(challengeX_1[-1], challengeY_1[-1]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        #plot path 2
                        CX_2 = np.array(challengeX_2)
                        CY_2 = np.array(challengeY_2)
                        plot(CX_2, CY_2, color='green', linewidth=2, linestyle="--", label="Path %s: Generated Challenge" % pathSeed[1])
                        RX_2 = np.array(respX_2)
                        RY_2 = np.array(respY_2)
                        plot(RX_2, RY_2, color='magenta', linewidth=2, label="Path %s: User Response" % pathSeed[1])
                        annotate("Start%s" % pathSeed[1], xy=(challengeX_2[0], challengeY_2[0]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        annotate("End%s" % pathSeed[1], xy=(challengeX_2[-1], challengeY_2[-1]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        legend(loc='lower left')

                        #Setup plot of pressure data
                        subplot(1,2,2)
                        title("Recorded Pressure")
                        xlabel("Points")
                        ylabel("Pressure")
                        ylim(0, 1.0)
                        #plot pressure data for path 1
                        PTS_1 = np.linspace(1,len(pressureList_1), len(pressureList_1))
                        PRS_1 = np.array(pressureList_1)
                        AP_1 = np.linspace(avgPres_1, avgPres_1, len(pressureList_1))
                        MA5_1 = np.array(movingAvg5_1)
                        MA10_1 = np.array(movingAvg10_1)
                        CMA_1 = np.array(cumulativeMovAvg_1)
                        plot(PTS_1, PRS_1, color='red', linewidth=2, label="Path %s: Pressure" % pathSeed[0])
                        plot(PTS_1, MA5_1, color='blue', linewidth=2, linestyle="--", label="Path %s: Moving average n = 5" % pathSeed[0])
                        #plot pressure data for path 2
                        PTS_2 = np.linspace(1,len(pressureList_2), len(pressureList_2))
                        PRS_2 = np.array(pressureList_2)
                        AP_2 = np.linspace(avgPres_2, avgPres_2, len(pressureList_2))
                        MA5_2 = np.array(movingAvg5_2)
                        MA10_2 = np.array(movingAvg10_2)
                        CMA_2 = np.array(cumulativeMovAvg_2)
                        plot(PTS_2, PRS_2, color='magenta', linewidth=2, label="Path %s: Pressure" % pathSeed[1])
                        plot(PTS_2, MA5_2, color='green', linewidth=2, linestyle="--", label="Path %s: Moving average n = 5" % pathSeed[1])
                        #Done plotting pressure data
                        legend(loc='lower right')
                        #Copy the directory structure of the OutputCSVs folder
                        deviceName = basename(dirname(root))
                        testerName = basename(root)
                        #If the directory doesn't exist make it
                        att_path = os.path.join(NORMALIZED_STRAT2_OVERLAID_FIGURE_OUTPUT_DIR, deviceName, testerName)
                        if not os.path.exists(att_path):
                            os.makedirs(att_path)
                        att_path = os.path.join(att_path, "OverlaidFiles = ('%s', '%s').png" % (fileNames[0], fileNames[1]))
                        #NOTE: Cannot show and save figure at same time
                        #show()
                        savefig(att_path)
                        close()
                        #clear lists
                        challengeX_1 = []
                        challengeY_1 = []
                        respX_1 = []
                        respY_1 = []
                        pressureList_1 = []
                        challengeX_2 = []
                        challengeY_2 = []
                        respX_2 = []
                        respY_2 = []
                        pressureList_2 = []
                        pathSeed = []
                        fileNames = []
                        count = 0

def genNormalizedStrat3OverlaidFigure2Path():
    # Walk through all the lower directories
    for root, dirs, files in os.walk(CSV_OUTPUT_DIR):
        # We only care about directories that have files
        if(len(files) != 0):
            files.sort()
            deviceName = basename(dirname(root))
            testerName = basename(root)
            print "Creating normalized overlaid figures for device: %s, user: %s" % (deviceName, testerName)

            # The directories with files have the .csv files
            # Get the pressure data from them and store in list
            challengeX_1 = []
            challengeY_1 = []
            respX_1 = []
            respY_1 = []
            pressureList_1 = []
            challengeX_2 = []
            challengeY_2 = []
            respX_2 = []
            respY_2 = []
            pressureList_2 = []
            pathSeed = []
            fileNames = []
            count = 0
            for fileName in files:
                seed = int(fileName.split(":")[0])
                #Only seeds above 1000 are extra long paths likely to create
                #bit strings of atleast 128 bits
                if( seed > 1000 ):
                    count += 1
                    pathSeed.append(fileName.split(":")[0])
                    fileNames.append(fileName)
                    challengeList = []
                    pressureList = []
                    respList = []
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
                    challengeLength = 0
                    for i in range(0, len(challengeList)-1):
                        challengeLength += distBetweenPts(challengeList[i], challengeList[i+1])
                    normalizedDist = challengeLength/32.0

                    normalizedList = []
                    currPt = challengeList[0]
                    for i in range(0, len(challengeList)-1):
                        currPt = challengeList[i]
                        nextPt = challengeList[i+1]

                        while(distBetweenPts(currPt, nextPt) > normalizedDist):
                            normalizedList.append(currPt)
                            currPt = calcNextPt(currPt, nextPt, normalizedDist)
                    normalizedList.append(challengeList[-1])

                    normalizedPressList = []
                    print len(respList)
                    print len(pressureList)
                    for normPt in normalizedList:
                        closestPtIndex = 0
                        secondClosestPtIndex = 0
                        closestPt = respList[0]
                        secondClosestPt = respList[0]
                        closestPtDist = distBetweenPts(normPt, closestPt)
                        for i in range(0, len(respList)):
                            if(distBetweenPts(normPt, respList[i]) <= closestPtDist):
                                closestPtDist = distBetweenPts(normPt, respList[i])
                                secondClosestPt = closestPt
                                secondClosestPtIndex = closestPtIndex
                                closestPt = respList[i]
                                closestPtIndex = i
                        secondClosestPtDist = distBetweenPts(normPt, secondClosestPt)
                        distBetweenClosestPts = secondClosestPtDist + closestPtDist
                        normPressure = (closestPtDist/distBetweenClosestPts)*pressureList[closestPtIndex] + (secondClosestPtDist/distBetweenClosestPts)*pressureList[secondClosestPtIndex] #weighted average weighted towards the closer point
                        normalizedPressList.append(normPressure)

                    if(count == 1):
                        for respPt in respList:
                            respX_1.append(respPt[0])
                            respY_1.append(respPt[1])
                        for normPressPt in normalizedPressList:
                            pressureList_1.append(normPressPt)
                        for challPt in challengeList:
                            challengeX_1.append(challPt[0])
                            challengeY_1.append(challPt[1])
                    elif(count == 2):
                        for respPt in respList:
                            respX_2.append(respPt[0])
                            respY_2.append(respPt[1])
                        for normPressPt in normalizedPressList:
                            pressureList_2.append(normPressPt)
                        for challPt in challengeList:
                            challengeX_2.append(challPt[0])
                            challengeY_2.append(challPt[1])

                    if count == 2:
                        # -- Calculations for path 1
                        #Calculate average pressure
                        avgPres_1 = 0
                        for pres in pressureList_1:
                            avgPres_1 = avgPres_1 + pres
                        avgPres_1 = avgPres_1 / len(pressureList_1)

                        #Calculate simple moving average n = 5
                        movingAvg5_1 = simpleMovingAverage(pressureList_1, 5)

                        #Calculate simple moving average n = 10
                        movingAvg10_1 = simpleMovingAverage(pressureList_1, 10)

                        #Calculate cumulative moving average
                        cumulativeMovAvg_1 = cumulativeMovingAverage(pressureList_1)

                        # -- Calculations for path 2
                        #Calculate average pressure
                        avgPres_2 = 0
                        for pres in pressureList_2:
                            avgPres_2 = avgPres_2 + pres
                        avgPres_2 = avgPres_2 / len(pressureList_2)

                        #Calculate simple moving average n = 5
                        movingAvg5_2 = simpleMovingAverage(pressureList_2, 5)

                        #Calculate simple moving average n = 10
                        movingAvg10_2 = simpleMovingAverage(pressureList_2, 10)

                        #Calculate cumulative moving average
                        cumulativeMovAvg_2 = cumulativeMovingAverage(pressureList_2)
                        # -- done with calculations

                        #Create figure to graph with
                        fig = figure(figsize=(16,12))
                        fig.suptitle("Normalized Overlaid Paths: %s\n Device: %s\n User: %s" % (",".join(pathSeed),deviceName, testerName))

                        #Setup plot of path traced
                        subplot(1,2,1)
                        xlim(0,800)
                        ylim(1280,0)
                        title("Challenge/Response Path")
                        xlabel("X location (pixels)")
                        ylabel("Y location (pixels)")
                        #plot path 1
                        CX_1 = np.array(challengeX_1)
                        CY_1 = np.array(challengeY_1)
                        plot(CX_1, CY_1, color='blue', linewidth=2, linestyle="--", label="Path %s: Generated Challenge" % pathSeed[0])
                        RX_1 = np.array(respX_1)
                        RY_1 = np.array(respY_1)
                        plot(RX_1, RY_1, color='red', linewidth=2, label="Path %s: User Response" % pathSeed[0])
                        annotate("Start%s" % pathSeed[0], xy=(challengeX_1[0], challengeY_1[0]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        annotate("End%s" % pathSeed[0], xy=(challengeX_1[-1], challengeY_1[-1]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        #plot path 2
                        CX_2 = np.array(challengeX_2)
                        CY_2 = np.array(challengeY_2)
                        plot(CX_2, CY_2, color='green', linewidth=2, linestyle="--", label="Path %s: Generated Challenge" % pathSeed[1])
                        RX_2 = np.array(respX_2)
                        RY_2 = np.array(respY_2)
                        plot(RX_2, RY_2, color='magenta', linewidth=2, label="Path %s: User Response" % pathSeed[1])
                        annotate("Start%s" % pathSeed[1], xy=(challengeX_2[0], challengeY_2[0]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        annotate("End%s" % pathSeed[1], xy=(challengeX_2[-1], challengeY_2[-1]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        legend(loc='lower left')

                        #Setup plot of pressure data
                        subplot(1,2,2)
                        title("Recorded Pressure")
                        xlabel("Points")
                        ylabel("Pressure")
                        ylim(0, 1.0)
                        #plot pressure data for path 1
                        PTS_1 = np.linspace(1,len(pressureList_1), len(pressureList_1))
                        PRS_1 = np.array(pressureList_1)
                        AP_1 = np.linspace(avgPres_1, avgPres_1, len(pressureList_1))
                        MA5_1 = np.array(movingAvg5_1)
                        MA10_1 = np.array(movingAvg10_1)
                        CMA_1 = np.array(cumulativeMovAvg_1)
                        plot(PTS_1, PRS_1, color='red', linewidth=2, label="Path %s: Pressure" % pathSeed[0])
                        plot(PTS_1, MA5_1, color='blue', linewidth=2, linestyle="--", label="Path %s: Moving average n = 5" % pathSeed[0])
                        #plot pressure data for path 2
                        PTS_2 = np.linspace(1,len(pressureList_2), len(pressureList_2))
                        PRS_2 = np.array(pressureList_2)
                        AP_2 = np.linspace(avgPres_2, avgPres_2, len(pressureList_2))
                        MA5_2 = np.array(movingAvg5_2)
                        MA10_2 = np.array(movingAvg10_2)
                        CMA_2 = np.array(cumulativeMovAvg_2)
                        plot(PTS_2, PRS_2, color='magenta', linewidth=2, label="Path %s: Pressure" % pathSeed[1])
                        plot(PTS_2, MA5_2, color='green', linewidth=2, linestyle="--", label="Path %s: Moving average n = 5" % pathSeed[1])
                        #Done plotting pressure data
                        legend(loc='lower right')
                        #Copy the directory structure of the OutputCSVs folder
                        deviceName = basename(dirname(root))
                        testerName = basename(root)
                        #If the directory doesn't exist make it
                        att_path = os.path.join(NORMALIZED_STRAT3_OVERLAID_FIGURE_OUTPUT_DIR, deviceName, testerName)
                        if not os.path.exists(att_path):
                            os.makedirs(att_path)
                        att_path = os.path.join(att_path, "OverlaidFiles = ('%s', '%s').png" % (fileNames[0], fileNames[1]))
                        #NOTE: Cannot show and save figure at same time
                        #show()
                        savefig(att_path)
                        close()
                        #clear lists
                        challengeX_1 = []
                        challengeY_1 = []
                        respX_1 = []
                        respY_1 = []
                        pressureList_1 = []
                        challengeX_2 = []
                        challengeY_2 = []
                        respX_2 = []
                        respY_2 = []
                        pressureList_2 = []
                        pathSeed = []
                        fileNames = []
                        count = 0

def genNormalizedStrat4OverlaidFigure2Path():
    # Walk through all the lower directories
    for root, dirs, files in os.walk(CSV_OUTPUT_DIR):
        # We only care about directories that have files
        if(len(files) != 0):
            files.sort()
            deviceName = basename(dirname(root))
            testerName = basename(root)
            print "Creating normalized overlaid figures for device: %s, user: %s" % (deviceName, testerName)

            # The directories with files have the .csv files
            # Get the pressure data from them and store in list
            challengeX_1 = []
            challengeY_1 = []
            respX_1 = []
            respY_1 = []
            pressureList_1 = []
            challengeX_2 = []
            challengeY_2 = []
            respX_2 = []
            respY_2 = []
            pressureList_2 = []
            pathSeed = []
            fileNames = []
            count = 0
            for fileName in files:
                seed = int(fileName.split(":")[0])
                #Only seeds above 1000 are extra long paths likely to create
                #bit strings of atleast 128 bits
                if( seed >= 2000 ):
                    count += 1
                    pathSeed.append(fileName.split(":")[0])
                    fileNames.append(fileName)
                    challengeList = []
                    pressureList = []
                    respList = []
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
                    normalizedDist = challengeLength/32.0

                    normalizedList = []
                    currPt = challengeList[0]
                    for i in range(0, 32):
                        normalizedList.append(currPt)
                        if(isX):
                            currPt = (currPt[0]+normalizedDist, currPt[1])
                        else:
                            currPt = (currPt[0], currPt[1]+normalizedDist)

                    normalizedPressList = []
                    print len(respList)
                    print len(pressureList)
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
                                if(respList[i][0] > normPt[0]): #if it's to the left
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

                    if(count == 1):
                        for respPt in respList:
                            respX_1.append(respPt[0])
                            respY_1.append(respPt[1])
                        for normPressPt in normalizedPressList:
                            pressureList_1.append(normPressPt)
                        for challPt in challengeList:
                            challengeX_1.append(challPt[0])
                            challengeY_1.append(challPt[1])
                    elif(count == 2):
                        for respPt in respList:
                            respX_2.append(respPt[0])
                            respY_2.append(respPt[1])
                        for normPressPt in normalizedPressList:
                            pressureList_2.append(normPressPt)
                        for challPt in challengeList:
                            challengeX_2.append(challPt[0])
                            challengeY_2.append(challPt[1])

                    if count == 2:
                        # -- Calculations for path 1
                        #Calculate average pressure
                        avgPres_1 = 0
                        for pres in pressureList_1:
                            avgPres_1 = avgPres_1 + pres
                        avgPres_1 = avgPres_1 / len(pressureList_1)

                        #Calculate simple moving average n = 5
                        movingAvg5_1 = simpleMovingAverage(pressureList_1, 5)

                        #Calculate simple moving average n = 10
                        movingAvg10_1 = simpleMovingAverage(pressureList_1, 10)

                        #Calculate cumulative moving average
                        cumulativeMovAvg_1 = cumulativeMovingAverage(pressureList_1)

                        # -- Calculations for path 2
                        #Calculate average pressure
                        avgPres_2 = 0
                        for pres in pressureList_2:
                            avgPres_2 = avgPres_2 + pres
                        avgPres_2 = avgPres_2 / len(pressureList_2)

                        #Calculate simple moving average n = 5
                        movingAvg5_2 = simpleMovingAverage(pressureList_2, 5)

                        #Calculate simple moving average n = 10
                        movingAvg10_2 = simpleMovingAverage(pressureList_2, 10)

                        #Calculate cumulative moving average
                        cumulativeMovAvg_2 = cumulativeMovingAverage(pressureList_2)
                        # -- done with calculations

                        #Create figure to graph with
                        fig = figure(figsize=(16,12))
                        fig.suptitle("Normalized Overlaid Paths: %s\n Device: %s\n User: %s" % (",".join(pathSeed),deviceName, testerName))

                        #Setup plot of path traced
                        subplot(1,2,1)
                        xlim(0,800)
                        ylim(1280,0)
                        title("Challenge/Response Path")
                        xlabel("X location (pixels)")
                        ylabel("Y location (pixels)")
                        #plot path 1
                        CX_1 = np.array(challengeX_1)
                        CY_1 = np.array(challengeY_1)
                        plot(CX_1, CY_1, color='blue', linewidth=2, linestyle="--", label="Path %s: Generated Challenge" % pathSeed[0])
                        RX_1 = np.array(respX_1)
                        RY_1 = np.array(respY_1)
                        plot(RX_1, RY_1, color='red', linewidth=2, label="Path %s: User Response" % pathSeed[0])
                        annotate("Start%s" % pathSeed[0], xy=(challengeX_1[0], challengeY_1[0]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        annotate("End%s" % pathSeed[0], xy=(challengeX_1[-1], challengeY_1[-1]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        #plot path 2
                        CX_2 = np.array(challengeX_2)
                        CY_2 = np.array(challengeY_2)
                        plot(CX_2, CY_2, color='green', linewidth=2, linestyle="--", label="Path %s: Generated Challenge" % pathSeed[1])
                        RX_2 = np.array(respX_2)
                        RY_2 = np.array(respY_2)
                        plot(RX_2, RY_2, color='magenta', linewidth=2, label="Path %s: User Response" % pathSeed[1])
                        annotate("Start%s" % pathSeed[1], xy=(challengeX_2[0], challengeY_2[0]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        annotate("End%s" % pathSeed[1], xy=(challengeX_2[-1], challengeY_2[-1]), bbox=dict(facecolor='white', edgecolor='None', alpha=0.65 ))
                        legend(loc='lower left')

                        #Setup plot of pressure data
                        subplot(1,2,2)
                        title("Recorded Pressure")
                        xlabel("Points")
                        ylabel("Pressure")
                        ylim(0, 1.0)
                        #plot pressure data for path 1
                        PTS_1 = np.linspace(1,len(pressureList_1), len(pressureList_1))
                        PRS_1 = np.array(pressureList_1)
                        AP_1 = np.linspace(avgPres_1, avgPres_1, len(pressureList_1))
                        MA5_1 = np.array(movingAvg5_1)
                        MA10_1 = np.array(movingAvg10_1)
                        CMA_1 = np.array(cumulativeMovAvg_1)
                        plot(PTS_1, PRS_1, color='red', linewidth=2, label="Path %s: Pressure" % pathSeed[0])
                        plot(PTS_1, MA5_1, color='blue', linewidth=2, linestyle="--", label="Path %s: Moving average n = 5" % pathSeed[0])
                        #plot pressure data for path 2
                        PTS_2 = np.linspace(1,len(pressureList_2), len(pressureList_2))
                        PRS_2 = np.array(pressureList_2)
                        AP_2 = np.linspace(avgPres_2, avgPres_2, len(pressureList_2))
                        MA5_2 = np.array(movingAvg5_2)
                        MA10_2 = np.array(movingAvg10_2)
                        CMA_2 = np.array(cumulativeMovAvg_2)
                        plot(PTS_2, PRS_2, color='magenta', linewidth=2, label="Path %s: Pressure" % pathSeed[1])
                        plot(PTS_2, MA5_2, color='green', linewidth=2, linestyle="--", label="Path %s: Moving average n = 5" % pathSeed[1])
                        #Done plotting pressure data
                        legend(loc='lower right')
                        #Copy the directory structure of the OutputCSVs folder
                        deviceName = basename(dirname(root))
                        testerName = basename(root)
                        #If the directory doesn't exist make it
                        att_path = os.path.join(NORMALIZED_STRAT4_OVERLAID_FIGURE_OUTPUT_DIR, deviceName, testerName)
                        if not os.path.exists(att_path):
                            os.makedirs(att_path)
                        att_path = os.path.join(att_path, "OverlaidFiles = ('%s', '%s').png" % (fileNames[0], fileNames[1]))
                        #NOTE: Cannot show and save figure at same time
                        #show()
                        savefig(att_path)
                        close()
                        #clear lists
                        challengeX_1 = []
                        challengeY_1 = []
                        respX_1 = []
                        respY_1 = []
                        pressureList_1 = []
                        challengeX_2 = []
                        challengeY_2 = []
                        respX_2 = []
                        respY_2 = []
                        pressureList_2 = []
                        pathSeed = []
                        fileNames = []
                        count = 0
if __name__ == '__main__':
    main()
