import logging
import os

import numpy as np
from sklearn.model_selection import train_test_split
from tensorflow import keras

from src.utils import set_model_path


class KModel:
    def __init__(self, config):
        self.random_state = config.get('random_state')
        np.random.seed(self.random_state)
        self.cv_folds = config.get('cv_folds')
        self.model_train_path = set_model_path(config.get('model_train_dir'))
        checkpoint_filepath = os.path.join(self.model_train_path, 'tmp', 'checkpoint')
        self.callbacks = [keras.callbacks.ModelCheckpoint(filepath=checkpoint_filepath,
                                                          monitor='precision',
                                                          mode='max',
                                                          save_best_only=True
                                                          )]

        self._model = self.compile_model()

    def compile_model(self):
        model = keras.Sequential(
            [
                keras.layers.Dense(
                    128, activation="relu", input_shape=(82,)
                ),
                keras.layers.Dropout(0.3),

                keras.layers.Dense(128, activation="relu"),
                keras.layers.Dense(1, activation="sigmoid"),
            ]
        )

        metrics = [
            keras.metrics.Precision(name="precision"),
            keras.metrics.Recall(name="recall"),
        ]

        model.compile(
            optimizer=keras.optimizers.Adam(1e-1), loss="binary_crossentropy", metrics=metrics
        )
        return model

    def train(self, features, targets):
        x_train, x_test, y_train, y_test = train_test_split(features, targets, test_size=0.2)
        self.fit(x_train, y_train)
        print(self.evaluate(x_test, y_test))

    def predict(self, features):
        return self._model.predict(features)

    def evaluate(self, features, targets):
        return self._model.evaluate(
            x=features,
            y=targets,
            return_dict=True
        )

    def fit(self, train_features, train_targets):
        self._model.fit(
            train_features,
            train_targets,
            batch_size=128,
            epochs=50,
            verbose=1,
            callbacks=self.callbacks,
            class_weight={0: 1, 1: 100},
        )
