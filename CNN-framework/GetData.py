#import libraries
import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler
import joblib
from joblib import dump

"""
Function takes in raw data from refactoring miner and preprocesses it for model training

Change the paths in trainPositives, trainNegatives, testPositives, testNegatives to the desired files

"""
def getData():
    #Read in raw data and convert to dataframe
    trainPositives = pd.read_csv("acp-dataset/train/positives_80p.csv", delimiter = ";")
    trainNegatives = pd.read_csv("acp-dataset/train/negatives_80p.csv", delimiter = ";")
    testPositives = pd.read_csv("acp-dataset/test/positives_20p.csv", delimiter = ";")
    testNegatives = pd.read_csv("acp-dataset/test/negatives_20p.csv", delimiter = ";")

    #Check for spelling error in first column we found
    try:
        testPositives = testPositives.rename(columns={"otalLinesOfCode": "TotalLinesOfCode"})
    except:
        pass

    #Drop dulicates in code and balance samples
    trainPositives = trainPositives.drop_duplicates().sample(frac = 1 )
    testPositives = testPositives.drop_duplicates().sample(frac = 1 )
    trainNegatives = trainNegatives.drop_duplicates().sample(frac = 1 )[0:len(trainPositives)]
    testNegatives = testNegatives.drop_duplicates().sample(frac = 1 )[0:len(testPositives)]
    
    #Concatenate positive and negative samples
    trainFull = pd.concat([trainPositives, trainNegatives])
    testFull = pd.concat([testPositives, testNegatives])
    
    #Randomize samples
    trainFull = trainFull.sample(len(trainFull))
    testFull = testFull.sample(len(testFull))

    #Reduce features and extract labels
    trainX = trainFull.iloc[:,:-3]
    trainY = trainFull.iloc[:,-2]
    testX = testFull.iloc[:,:-3]
    testY = testFull.iloc[:,-2]
    
    #Reshape data to 3D for CNN
    trainX = trainX[..., None]
    trainY = trainY[..., None]
    testX = testX[..., None]
    testY = testY[..., None]
    
    return trainX, trainY

"""
Function takes in preprocessed data (classes balanced, duplicates removed, NOT normalized)
The standard scaler has to be exported so it is important to normalize the data here

Change the paths in trainFull and TestFull to desired preprocessedData

"""
def getPreprocessedData():
    
    #Concatenate positive and negative samples
    trainFull = pd.read_csv("PreprocessedData/trainFull.csv", index_col=0)
    testFull = pd.read_csv("PreprocessedData/testFull.csv", index_col=0)
    
    #Randomize samples
    trainFull = trainFull.sample(len(trainFull))
    testFull = testFull.sample(len(testFull))

    #Randomize samples
    trainFull = trainFull.sample(len(trainFull))
    testFull = testFull.sample(len(testFull))

    #Reduce features and extract labels
    trainX = trainFull.iloc[:,:-1]
    trainY = trainFull.iloc[:,-1]
    testX = testFull.iloc[:,:-1]
    testY = testFull.iloc[:,-1]

    #Reshape data to 3D for CNN
    trainX = trainX.to_numpy()[..., None]
    trainY = trainY.to_numpy()[..., None]
    testX = testX.to_numpy()[..., None]
    testY = testY.to_numpy()[..., None]

    return trainX, trainY






