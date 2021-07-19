import os
from shutil import copyfile

import joblib
from sklearn.metrics import f1_score
from sklearn.model_selection import train_test_split, cross_val_score

from model_factory import ModelFactory
from utils import set_train_path


class Model:
    def __init__(self, config):
        self.model_type = config.get('model_type')
        self.model_config_path = config.get('model_config_path')
        self.model_train_path = set_train_path(config.get('model_train_dir'))
        self._model = ModelFactory(self.model_config_path, self.model_type).make_model()
        print("Made model!")  # TODO: Introduce logger here

    def set_model(self, model_config_path, model_type):
        self._model = ModelFactory(model_config_path, model_type).make_model()

    def predict(self, features):
        return self._model.predict(features)

    def save_model(self):
        copyfile(src=self.model_config_path, dst=os.path.join(self.model_train_path, 'training_settings.ini'))
        return joblib.dump(self._model, os.path.join(self.model_train_path, 'trained.sav'))


class OCCModel(Model):
    def fit(self, neutrals):
        return self._model.fit(neutrals)

    def train(self, neutral, positive):
        self.fit(neutral)
        pos_accuracy = sum(self.predict(positive) == -1) / len(positive)
        with open(os.path.join(self.model_train_path, "training_results.txt"), 'a') as f:
            to_print = "Training error fraction: {}\n" \
                       "Accuracy on positives: {}\n".format(self._model.nu, pos_accuracy)
            f.write(to_print)


class BinaryModel(Model):
    def fit(self, features, targets):
        return self._model.fit(features, targets)

    def train(self, x, y):
        x_train, x_test, y_train, y_test = train_test_split(x, y)
        cv_scores = cross_val_score(self._model, x=x_train, y=y_train, cv=10, scoring='f1')
        f1 = f1_score(y_test, self.predict(y_train))
        with open(os.path.join(self.model_train_path, "training_results.txt"), 'a') as f:
            to_print = "Cross val f1 scores: {}\n" \
                       "Test f1 score: {}\n".format(*cv_scores, f1)
            f.write(to_print)


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
