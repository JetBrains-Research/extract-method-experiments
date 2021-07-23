import os
from shutil import copyfile

import joblib
from sklearn.metrics import precision_score, f1_score, recall_score
from sklearn.model_selection import train_test_split, cross_val_score, KFold

from .model_factory import ModelFactory
from .utils import set_train_path


class Model:
    def __init__(self, config):
        self.random_state = config.get('random_state')
        self.model_type = config.get('model_type')
        self.model_config_path = config.get('model_config_path')
        self.model_train_path = set_train_path(config.get('model_train_dir'))
        self._model = ModelFactory(self.model_config_path, self.model_type).make_model()

    def set_model(self, model_config_path, model_type):
        self._model = ModelFactory(model_config_path, model_type).make_model()

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

    def cross_validate(self, x, y, folds=5):
        cv = KFold(n_splits=folds, random_state=self.random_state, shuffle=True)
        cv_scores = cross_val_score(self._model, X=x, y=y, cv=cv, scoring='f1')
        with open(os.path.join(self.model_train_path, "cross_validation_results.txt"), 'w') as f:
            to_print = f'Cross val f1 scores: {cv_scores}\n'
            f.write(to_print)

    def train(self, x, y):
        x_train, x_test, y_train, y_test = train_test_split(x, y, )
        self.fit(x_train, y_train)
        f1 = f1_score(y_test, self.predict(x_test))
        precision = precision_score(y_test, self.predict(x_test))
        recall = recall_score(y_test, self.predict(x_test))

        with open(os.path.join(self.model_train_path, "training_results.txt"), 'w') as f:
            to_print = f'Test precision score: {precision}\n' \
                       f'Test f1 score: {f1}\n' \
                       f'Test recall score: {recall}\n'

            f.write(to_print)
        self.save_model()


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
