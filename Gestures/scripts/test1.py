import os
import sys

from contextlib import contextmanager
from os.path import join, dirname, basename
from ctypes import *

GENERATED_OUTPUT_DIR = "/home/rascheel/git/PUFProject/OutputGenerated/"
RESULTS_OUTPUT_DIR = "/home/rascheel/git/PUFProject/Results/PRGTests"
LIBRARY_LOCATION = "/usr/local/lib/libtestu01.so"

def main():
    libc = CDLL(LIBRARY_LOCATION)
    #libc.bbattery_RabbitFile(c_char_p(LIBRARY_LOCATION), c_double(3500))
    for root, dirs, files in os.walk(GENERATED_OUTPUT_DIR):
        if(len(files) != 0):
            #Copy the directory structure of the OutputCSVs folder
            stratName = basename(dirname(dirname(root))) 
            deviceName = basename(dirname(root))
            testerName = basename(root)

            #If the directory doesn't exist make it
            outputFile = os.path.join(RESULTS_OUTPUT_DIR, stratName, deviceName, testerName, "statAnalysis.txt")
            if not os.path.exists(outputFile):
                os.makedirs(os.path.dirname(outputFile))

            inputBinaryFile = join(root, files[0])

            print "Starting test for strategy: %s, device: %s, tester: %s" % (stratName, deviceName, testerName)
            #Write binary file, redirecting stdout to the output file
            with stdout_redirected(outputFile):
                libc.bbattery_RabbitFile(c_char_p(inputBinaryFile), c_double(500))


@contextmanager
def stdout_redirected(to=os.devnull):
    '''
    import os

    with stdout_redirected(to=filename):
        print("from Python")
        os.system("echo non-Python applications are also supported")
    '''
    fd = sys.stdout.fileno()

    ##### assert that Python and C stdio write using the same file descriptor
    ####assert libc.fileno(ctypes.c_void_p.in_dll(libc, "stdout")) == fd == 1

    def _redirect_stdout(to):
        sys.stdout.close() # + implicit flush()
        os.dup2(to.fileno(), fd) # fd writes to 'to' file
        sys.stdout = os.fdopen(fd, 'w') # Python writes to fd

    with os.fdopen(os.dup(fd), 'w') as old_stdout:
        with open(to, 'w') as file:
            _redirect_stdout(to=file)
        try:
            yield # allow code to be run with the redirected stdout
        finally:
            _redirect_stdout(to=old_stdout) # restore stdout.
                                            # buffering and flags such as
                                            # CLOEXEC may be different

if __name__ == '__main__':
    main()
