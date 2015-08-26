import os
import binascii
from util import hammingDistance
from os.path import join, dirname, basename

GENERATED_OUTPUT_DIR = "/home/nmont/Documents/PUFphh/git/PUFProject/OutputGenerated/"

def main():
    testHamming("Strat1")
    testHamming("Strat2")
    testHamming("Strat3")
    testHamming("Strat4")
    #testHamming("ConsistencyStrat2")

def testHamming(strategy="Strat1"):
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
    # routes but different devices/users
    print "\n##### Average hamming distance between runs of same routes, different devices/users #####"
    for i in range(0, len(byteArrList)):
        for j in range(i, len(byteArrList)):
            shortLen = len(byteArrList[i]) if len(byteArrList[i]) < len(byteArrList[j]) else len(byteArrList[j])
            avgHamDist = 0
            #byte length of 16 creates 128-bit sequences
            for k in range(0, shortLen, 16):
                if((len(byteArrList[i][k:k+16]) == 16) and (len(byteArrList[j][k:k+16]) == 16)):
                    avgHamDist += hammingDistance(byteArrList[i][k:k+16], byteArrList[j][k:k+16])
            avgHamDist /= (shortLen/16)
            print "%i -> %i = %i" % (i, j, avgHamDist)

    # Calculate average hamming distance between runs of same device/users 
    # but different routes
    print "\n##### Average hamming distance between runs of same devices/user, different routes #####"
    count = 0
    for byteArr in byteArrList:
        avgHamDist = 0
        comparisons = 0
        for i in range(0, len(byteArr)/16):
            for j in range(i, len(byteArr)/16):
                if( i != j ):
                    avgHamDist += hammingDistance(byteArr[i:i+16], byteArr[j:j+16])
                    comparisons += 1
        avgHamDist /= comparisons
        print "%i = %i" % (count, avgHamDist)
        count += 1
    print "\n"

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
