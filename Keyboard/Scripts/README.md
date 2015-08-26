#Scripts Usage Information
These scripts are used to analyze information retrieved from the android keyboard. It describes the intent behind all of the scripts, and what they do.

All of the following scripts were written for Python 2.7
___
##main.py
This script provides options that will allow you to use the scripts in an organized way. Many of the scripts do not have main methods and are simply initialized using the main method.

##convertXY.py
This file converts the data collected from the format of the first iteration of the android keyboard to the format of the second iteration of the android keyboard. It works on data collected on the Nexus 7 tablet and is not portable because it relies on the screen size of the Nexus 7.

##debug.py
Contains a debug() method which prints useful information about the authentication model similar to the sensec method of authentication.

The augment\_data() method prints out a set of pressure values for a given symbol based on the x,y coordinates. This was used before the Android Keyboard contained only the x,y coordinates, and not the symbol.

##effectiveness.py
Determines the relative effectiveness of the authentication method similar to the sensec method. It does this by splitting the user data in half. Building a model out of the first half. It then challenges this model with both the second half of the user data, and the challenge data.

The effectiveness calculated at the end is the number of good outcomes by the number of bad outcomes. Good outcomes are defined as the user being authenticated and the challenge data (non-user) not being authenticated. Bad outcomes are defined as the challenge data being authenticated and the user data not being authenticated.


##find\_best\_fit.py (_depricated_)
Find the user profile which best fits the model. This script does not work anymore because a script it uses is not longer the same.

##lookup\_test.py (_depricated_)
This script was used for verifying the completeness of the correctness of the probabilities for the Markov model.

##model_compare.py
This is one authentication scheme. Models are built from both the challenge data and the user data. These models are than compared and a sort of difference is derived between the models. If the user is within an (arbitrary) distance from the model than they pass the authentication.

##myutilities.py
Most of the methods included in this file aide in building the Markov model. There are also some that deal with the creating files and directories.

##user\_auth.py
This is one authentication scheme similar to the sensec model. Each window in the raw\_challenge\_data is compared against a Markov model built from raw\_model\_data.

Probability starts at 1. For each in the challenge data window contained in the model the probability is multiplied by the probability of the next touch in the challenge data coming after that window.

The final probability of the challenge data authenticated against the model is than compared to the value received when the data the model is created with compared to the model.

These probability values are adjusted based on the number of windows that were successfully matched in each set. The idea behind the correction is that we mock having the same number of windows in the challenge data set and the user data set. This is done by taking user\_probability to the power of (user\_windows/challenge\_windows).

##user\_class.py (_depricated_)
Will take in a random set of data and classify the most likely user based on given user profiles (yet to be implemented and probably not immediately useful)
