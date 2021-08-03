import os
from shutil import copyfile

import joblib
import numpy as np
from sklearn.metrics import classification_report
from sklearn.model_selection import train_test_split

from .model_factory import ModelFactory
from .utils import set_train_path


class Model:
    def __init__(self, config):
        self.random_state = config.get('random_state')
        np.random.seed(self.random_state)

        self.cv_folds = config.get('cv_folds')
        self.model_type = config.get('model_type')
        self.model_config_path = config.get('model_config_path')
        self.model_train_path = set_train_path(config.get('model_train_dir'))
        self._model = ModelFactory(self.model_config_path, self.model_type).make_model()

    def set_model(self, model_config_path, model_type):
        self._model = ModelFactory(model_config_path, model_type, self.cv_folds).make_model()

    def predict(self, features):
        return self._model.predict(features)

    def save_model(self):
        copyfile(src=self.model_config_path, dst=os.path.join(self.model_train_path, 'model_settings.ini'))
        return joblib.dump(self._model, os.path.join(self.model_train_path, 'trained.sav'))

    def save_training_config(self, training_config_path):
        copyfile(src=training_config_path, dst=os.path.join(self.model_train_path, 'training_settings.ini'))


class OCCModel(Model):
    def fit(self, neutrals):
        return self._model.fit(neutrals)

    def train(self, neutral, positive):
        self.fit(neutral)
        pos_accuracy = sum(self.predict(positive) == -1) / len(positive)
        with open(os.path.join(self.model_train_path, "training_results.txt"), 'a') as f:
            to_print = f'Training error fraction: {self._model.nu,}\n' \
                       f'Accuracy on positives: {pos_accuracy}\n'
            f.write(to_print)
        return self.save_model()


class BinaryModel(Model):
    def fit(self, features, targets):
        return self._model.fit(features, targets)

    def train(self, x, y):
        x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.2)
        self.fit(x_train, y_train)
        with open(os.path.join(self.model_train_path, "cv_results.txt"), 'w') as f:
            to_write = f'mean: {self._model.cv_results_.get("mean_test_score")}\n' \
                       f'std: {self._model.cv_results_.get("std_test_score")}'
            f.write(to_write)

        with open(os.path.join(self.model_train_path, "test_report.txt"), 'w') as f:
            f.write(classification_report(y_test, self.predict(x_test)))

        self.save_best()

    def save_best(self):
        copyfile(src=self.model_config_path, dst=os.path.join(self.model_train_path, 'model_settings.ini'))
        with open(os.path.join(self.model_train_path, "best_params.txt"), 'w') as f:
            f.write(str(self._model.best_params_))
        joblib.dump(self._model.best_estimator_, 'trained.sav')


class TestModel:
    def __init__(self, model_path):
        self.model = self.load_my_model(model_path)

    @staticmethod
    def load_my_model(model_path):
        """
        Loads and returns the neural network from the file
        """
        model_file_path = os.path.join(model_path, 'trained.sav')

        if os.path.isfile(model_file_path):
            loaded_model = joblib.load(model_file_path)
            return loaded_model
        else:
            print("No such model-folder")  # TODO: make into logger

    def predict(self, features):
        return self.model.predict(features)
