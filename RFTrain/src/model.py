import os
from shutil import copyfile
import logging
import joblib
import numpy as np
from sklearn.metrics import classification_report
from pathlib import Path
from .model_factory import ModelFactory
from .utils import set_model_path

logging.basicConfig(format='%(asctime)s %(message)s', datefmt='%m/%d/%Y %I:%M:%S %p',
                    level=logging.DEBUG, filename='training.log')


class Model:
    def __init__(self, config):
        self.random_state = config.get('random_state')
        np.random.seed(self.random_state)

        self.model_config_path = config.get('model_config_path')
        self.model_path = set_model_path(config.get('model_train_dir'))

        copyfile(src=self.model_config_path, dst=os.path.join(self.model_path, 'model_settings.ini'))

        self.train_dir = os.path.join(self.model_path, 'train')

        self._model = ModelFactory(self.model_config_path,
                                   config.get('classifier_type'),
                                   config.get('cv_folds'),
                                   config.get('scoring_func'),
                                   self.random_state).make_model()

        logging.info(f'Created a model with {self.model_config_path} config')

    def predict(self, features, threshold=0.5):
        return self._model.predict_proba(features)[:, 1] >= threshold

    def fit(self, features, targets):
        return self._model.fit(features, targets)

    def train(self, x_train, y_train):
        logging.info(f'Initiated training sequence for model at {self.model_path}')
        self.fit(x_train, y_train)
        with open(os.path.join(self.model_path, "cv_results.txt"), 'w') as f:
            to_write = f'mean score: {self._model.cv_results_.get("mean_test_score")[self._model.best_index_]}\n' \
                       f'std score: {self._model.cv_results_.get("std_test_score")[self._model.best_index_]}\n' \
                       f'mean fit time: {self._model.cv_results_.get("mean_fit_time")[self._model.best_index_]}\n' \
                       f'std fit time: {self._model.cv_results_.get("std_fit_time")[self._model.best_index_]}\n'
            f.write(to_write)

        self.save_best()
        self.save_grid()

    def validate(self, x_test, y_test):
        val_dir = os.path.join(self.model_path, 'validate_results')
        Path(val_dir).mkdir(parents=True, exist_ok=True)

        with open(os.path.join(val_dir, "report_0.5.txt"), 'w') as f:
            f.write(classification_report(y_test, self.predict(x_test, 0.5)))

        with open(os.path.join(val_dir, "report_0.4.txt"), 'w') as f:
            f.write(classification_report(y_test, self.predict(x_test, 0.4)))

        with open(os.path.join(val_dir, "report_0.3.txt"), 'w') as f:
            f.write(classification_report(y_test, self.predict(x_test, 0.3)))

    def save_best(self):
        with open(os.path.join(self.model_path, "best_params.txt"), 'w') as f:
            f.write(str(self._model.best_params_))
        try:
            joblib.dump(self._model.best_estimator_, os.path.join(self.model_path, "best_trained.sav"))
        except:
            logging.error(f'Failed to save the best model at {self.model_path}')
            return 1
        logging.info(f'Saved the model at {self.model_path}')
        return 0

    def save_grid(self):
        try:
            joblib.dump(self._model, os.path.join(self.model_path, 'trained.sav'))
        except:
            logging.error(f'Failed to save the grid at {self.model_path}')
            return 1
        logging.info(f'Saved the grid at {self.model_path}')
        return 0

    def save_training_config(self, training_config_path):
        copyfile(src=training_config_path, dst=os.path.join(self.model_path, 'training_settings.ini'))

    def get_best_estimator(self):
        return self._model.best_estimator_


class TestModel:
    def __init__(self, model_path):
        self._model = self.load_my_model(model_path)

    @staticmethod
    def load_my_model(model_path):
        """
        Loads and returns the model from the file
        """
        model_file_path = os.path.join(model_path, 'trained.sav')

        if os.path.isfile(model_file_path):
            loaded_model = joblib.load(model_file_path)
            return loaded_model
        else:
            logging.error(f'Failed to load model from {model_path}')
            return None

    def predict(self, features):
        return self._model.predict(features)
