#import libraries
from GetData import getData, getPreprocessedData
from CreateModel import newModel 
import sys 
import traceback
import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler
import joblib
from joblib import dump

#path to export trained model
outfile = "TrainedModel.h5"

print("Creating data set...")
try:
    trainX, trainY = getPreprocessedData()
    print("Dataset creation successful\n")
except:
    print("Failed to create data set, system exiting")
    print(traceback.format_exc())
    sys.exit()
    
print("Creating Convolutional Neural Network...")
try:
    model = newModel()
    print("Network creation successful\n")
except:
    print("Network creation failed, check CreateModel.py, system exiting")
    print(traceback.format_exc())
    sys.exit()
    
print("Begin training (Default 30 epochs)...")
try:
    model.fit(trainX, trainY, epochs = 30, batch_size=20, verbose = 1)
    print("Training complete, exporting model...\n")
except:
    print("Training failed, please see tensorflow documentation, system exiting")
    print(traceback.format_exc())
    sys.exit()

try:
    model.save(outfile,save_format="h5")
    print("Model successfully exported: " + outfile)
    print("Exiting...")
except:
    print("Model failed to export, system exiting")
    print(traceback.format_exc())
    
    
