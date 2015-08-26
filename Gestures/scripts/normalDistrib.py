import math
from scipy.stats import norm
from util import interpolatedPressure, getRawDataLists, meanAndStdDev, testPressListVsDistrib, genMeanList, genGraph


def main():
    allowedDeviation = 2

    dataLists = getRawDataLists()
    filteredLists = filter(lambda dataList: dataList.seed == 2000 and 
                                            dataList.testerName == "Ryan" and 
                                            dataList.deviceName == "nexus-07", 
                           dataLists)
    
    interpPressLists = []
    for dataList in filteredLists:
        interpPressLists.append(interpolatedPressure(n=32, dataList=dataList))

    tmpLists = []
    for i in range(0,32):
        distrib = []
        for interpPressList in interpPressLists:
            distrib.append(interpPressList[i])
        tmpLists.append(distrib)
    
    interpPressDistribs = []
    for distrib in tmpLists:
        interpPressDistribs.append(meanAndStdDev(distrib))

    for distrib in interpPressDistribs:
        print distrib

    print "----------------------- Ryan nexus-07 -------------------"
    ryanAvgFailedPoints = 0.0
    for i in range(0, len(interpPressLists)):
        failedPoints = testPressListVsDistrib(interpPressLists[i], interpPressDistribs, allowedDeviation)
        print "Failed Points = %d : %s" % (failedPoints, filteredLists[i].fileName)
        ryanAvgFailedPoints = ryanAvgFailedPoints + failedPoints

        xArray = range(0,32)
        yArrays = []
        yLabels = []

        yArrays.append(genMeanList(interpPressDistribs, 0.0))
        yLabels.append("Mean pressure")

        yArrays.append(genMeanList(interpPressDistribs, allowedDeviation))
        yLabels.append("Mean pressure + %.1f deviations" % allowedDeviation)

        yArrays.append(genMeanList(interpPressDistribs, -allowedDeviation))
        yLabels.append("Mean pressure - %.1f deviations" % allowedDeviation)

        yArrays.append(interpPressLists[i])
        yLabels.append("Interpolated Response")

        genGraph(xArray, yArrays, yLabels, xLabel="Points", yLabel="Pressure",
                 graphTitle="Failed points = %d %s" % (failedPoints, filteredLists[i].fileName),
                 savePath="/home/rascheel/git/PUFProject/Figures/Distrib/nexus-07/RyansDistribs")

    ryanAvgFailedPoints = ryanAvgFailedPoints / float(len(interpPressLists))
    print "Ryan Average Failed Points = %i" % ryanAvgFailedPoints

    jakeFilteredLists = filter(lambda dataList: dataList.seed == 2000 and 
                                            dataList.testerName == "Jake" and 
                                            dataList.deviceName == "nexus-07", 
                          dataLists)

    interpPressLists = []
    for dataList in jakeFilteredLists:
        interpPressLists.append(interpolatedPressure(n=32, dataList=dataList))

    print "------------------------- Jake on Ryan's distributions nexus-07 ------------"
    jakeAvgFailedPoints = 0.0
    for i in range(0, len(interpPressLists)):
        failedPoints = testPressListVsDistrib(interpPressLists[i], interpPressDistribs, allowedDeviation)
        print "Failed Points = %d : %s" % (failedPoints, filteredLists[i].fileName)
        jakeAvgFailedPoints = jakeAvgFailedPoints + failedPoints

        xArray = range(0,32)
        yArrays = []
        yLabels = []

        yArrays.append(genMeanList(interpPressDistribs, 0.0))
        yLabels.append("Mean pressure")

        yArrays.append(genMeanList(interpPressDistribs, allowedDeviation))
        yLabels.append("Mean pressure + %.1f deviations" % allowedDeviation)

        yArrays.append(genMeanList(interpPressDistribs, -allowedDeviation))
        yLabels.append("Mean pressure - %.1f deviations" % allowedDeviation)

        yArrays.append(interpPressLists[i])
        yLabels.append("Interpolated Response")

        genGraph(xArray, yArrays, yLabels, xLabel="Points", yLabel="Pressure",
                 graphTitle="Failed points = %d %s" % (failedPoints, jakeFilteredLists[i].fileName),
                 savePath="/home/rascheel/git/PUFProject/Figures/Distrib/nexus-07/JakeonRyansDistribs")

    jakeAvgFailedPoints = jakeAvgFailedPoints / float(len(interpPressLists))
    print "Jake Average Failed Points = %i" % jakeAvgFailedPoints

if __name__ == '__main__':
    main()
