#import libraries
from GetData import getData #TODO: greate function that returns processed data to train with
from CreateModel import newModel
import sys
import traceback

#path to export trained model
outfile = "TrainedModel.h5"

print("Creating data set...")
try:
    trainX, trainY = getData()
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
    
print("Begin training (Default 20 epochs)...")
try:
    trainedModel = model.fit(trainX, trainY, epochs = 20, batch_size=20, verbose = 1)
    print("Training complete, exporting model...\n")
except:
    print("Training failed, please see tensorflow documentation, system exiting")
    print(traceback.format_exc())
    sys.exit()

try:
    trainedModel.save(outfile,save_format="h5")
    print("Model successfully export: " + outfile)
    print("Exiting...")
    sys.exit()
except:
    print("Model failed to export, system exiting")
    print(traceback.format_exc())
    sys.exit()
    
