import tensorflow as tf
from CreateModel import newModel
import joblib
from joblib import load
from GetData import getData, getPreprocessedData
import pandas as pd
import numpy as np

#TODO: figure out how to load in data for evaluation

#Load in scaler
scaler = load("standardScaler.bin")
#Load in model
model = newModel()
model.load_weights("TrainedModel")

testX = scaler.transform(testX)

testX = testX[..., None]

probPrediction = model.predict(testX)

prediction = []
for i in range(0,len(probPrediction)):
    if probPrediction[i] >= .5:
        prediction.append(1)
    else:
        prediction.append(0)
print(prediction)






