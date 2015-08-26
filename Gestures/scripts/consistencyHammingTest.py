import os
import binascii
from util import hammingDistance
from os.path import join, dirname, basename

GENERATED_OUTPUT_DIR = "/home/nmont/Documents/PUF\ Research/PUFProject/OutputGenerated/"

def main():
    #testConsistencyHamming("ConsistencyStrat2")
    #testConsistencyHamming("NormalizedStrat2")
    #testConsistencyHamming("NormalizedStrat3")
    testConsistencyHamming("NormalizedStrat4")


def testConsistencyHamming(strategy="ConsistencyStrat1"):
    print "-------------------------------------------------------------------"
    print "Testing Hamming distances for strategy: %s" % strategy
    byteArrList = []
    fileCount = 0
    print "##### Binary Files #####"
    for root, dirs, files in os.walk(GENERATED_OUTPUT_DIR + strategy + "/"):
        if(len(files) != 0):
            files.sort()
            for filename in files:
                byteArrList.append(convBinFileToByteArr(join(root, filename)))
                print "Binary File %i = %s" % (fileCount, join(root,filename))
                fileCount += 1

    # Calculate average hamming distance between runs of the same 
    # routes, same user, same device
    print "\n##### Average hamming distance between runs of same routes, same devices, same users #####"
    for i in range(0, len(byteArrList)):
        for j in range(i, len(byteArrList)):
            shortLen = len(byteArrList[i]) if len(byteArrList[i]) < len(byteArrList[j]) else len(byteArrList[j])
            #byte length of 16 creates 128-bit sequences
            if( shortLen > 3 ):
                hamDist = hammingDistance(byteArrList[i][0:3], byteArrList[j][0:3])
                print "%i -> %i = %i" % (i, j, hamDist)

def convBinFileToByteArr(binFile):
    byteArr = bytearray()
    #print binFile

    with open(binFile, "rb") as f:
        byte = f.read(1)
        while(byte != ""):
            #print binascii.hexlify(byte)
            byteArr.append(byte)
            byte = f.read(1)

    return byteArr

if __name__ == '__main__':
    main()
