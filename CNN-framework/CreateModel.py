#import libraries 
import tensorflow as tf
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import Flatten
from keras.layers import Dropout, LSTM
from tensorflow.keras import layers, models
from keras.layers.convolutional import Conv1D
from keras.layers.convolutional import MaxPooling1D

def newModel(layers, drop, dense):
    #Create new sequential model
    model = Sequential()

    #Add layers
    model.add((Conv1D(filters=32, kernel_size=3, activation='relu')))
    model.add((Conv1D(filters=i, kernel_size=3, activation='relu')))
    model.add((MaxPooling1D(pool_size=2)))
    model.add(Dropout(drop))
    model.add(Flatten())
    model.add(Dense(dense, activation='sigmoid'))
    model.add(Dense(1, activation='sigmoid'))
    
    #Define optimizer
    adam = tf.optimizers.Adam(learning_rate=0.001, beta_1=0.9, beta_2=0.999, epsilon=None, decay=0.0)
    #Complie model
    model.compile(optimizer=adam, loss='binary_crossentropy', metrics=[tf.keras.metrics.AUC(curve='PR'),                                                                                         tf.keras.metrics.PrecisionAtRecall(0.8)])
    return model