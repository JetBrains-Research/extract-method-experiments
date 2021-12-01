import logging
import os
from pathlib import Path
from shutil import copyfile

import joblib
import numpy as np
from sklearn.metrics import classification_report
from sklearn2pmml import sklearn2pmml
from sklearn2pmml.pipeline import PMMLPipeline

from .model_factory import ModelFactory
from .utils import set_model_path


class TrainModel:
    def __init__(self, config):
        self.random_state = config.get('random_state')
        np.random.seed(self.random_state)

        self._model_config_path = config.get('model_config_path')
        self._model_path = set_model_path(config.get('model_train_dir'))

        copyfile(src=self._model_config_path, dst=os.path.join(self._model_path, 'model_settings.ini'))

        self.train_dir = os.path.join(self._model_path, 'train')

        self._model = ModelFactory(self._model_config_path,
                                   config.get('classifier_type'),
                                   config.get('cv_folds'),
                                   config.get('scoring_func'),
                                   self.random_state).make_model()

    def predict(self, features):
        return self._model.predict(features)

    def predict_with_threshold(self, features, threshold):
        return self._model.predict_proba(features)[:, 1] >= threshold

    def fit(self, features, targets):
        return self._model.fit(features, targets)

    def train(self, x_train, y_train):
        self.fit(x_train, y_train)
        with open(os.path.join(self._model_path, "cv_results.txt"), 'w') as f:
            to_write = f'mean score: {self._model.cv_results_.get("mean_test_score")[self._model.best_index_]}\n' \
                       f'std score: {self._model.cv_results_.get("std_test_score")[self._model.best_index_]}\n' \
                       f'mean fit time: {self._model.cv_results_.get("mean_fit_time")[self._model.best_index_]}\n' \
                       f'std fit time: {self._model.cv_results_.get("std_fit_time")[self._model.best_index_]}\n'
            f.write(to_write)

        self.save_best()
        self.save_grid()

    def save_best(self):
        with open(os.path.join(self._model_path, "best_params.txt"), 'w') as f:
            f.write(str(self._model.best_params_))
        try:
            joblib.dump(self._model.best_estimator_, os.path.join(self._model_path, "best_trained.sav"))
        except:
            return 1
        logging.info(f'Saved the model at {self._model_path}')
        return 0

    def save_grid(self):
        try:
            joblib.dump(self._model, os.path.join(self._model_path, 'trained.sav'))
        except:
            logging.error(f'Failed to save the grid at {self._model_path}')
            return 1
        return 0

    def save_pmml(self):
        pmml_pipeline = PMMLPipeline([
            ("pipeline", self._model.best_estimator_)
        ])
        pmml_pipeline.configure(compact=False)

        sklearn2pmml(pmml_pipeline,
                     os.path.join(self._model_path, "model_pipeline.pmml"),
                     with_repr=True)

    def save_training_config(self, training_config_path):
        copyfile(src=training_config_path, dst=os.path.join(self._model_path, 'training_settings.ini'))


class TestModel:
    def __init__(self, model_path):
        self._model = self.load_model(model_path)
        self._model_path = model_path

    @staticmethod
    def load_model(model_path):
        """
        Loads and returns the model from the file
        """
        model_file_path = os.path.join(model_path, 'trained.sav')

        if os.path.isfile(model_file_path):
            loaded_model = joblib.load(model_file_path)
            return loaded_model
        else:
            return None

    def save_pmml(self):
        pmml_pipeline = PMMLPipeline([
            ("pipeline", self._model.best_estimator_)
        ])
        pmml_pipeline.configure(compact=False)

        sklearn2pmml(pmml_pipeline,
                     os.path.join(self._model_path, "model_pipeline.pmml"),
                     with_repr=True)

    def predict(self, features):
        return self._model.predict(features)

    def predict_with_threshold(self, features, threshold):
        return self._model.predict_proba(features)[:, 1] >= threshold

    def test(self, x, y):
        val_dir = os.path.join(self._model_path, 'test_results')
        Path(val_dir).mkdir(parents=True, exist_ok=True)

        try:
            with open(os.path.join(val_dir, "report_0.5.txt"), 'w') as f:
                f.write(classification_report(y, self.predict_with_threshold(x, 0.5)))
            with open(os.path.join(val_dir, "report_0.4.txt"), 'w') as f:
                f.write(classification_report(y, self.predict_with_threshold(x, 0.4)))
            with open(os.path.join(val_dir, "report_0.3.txt"), 'w') as f:
                f.write(classification_report(y, self.predict_with_threshold(x, 0.3)))
            with open(os.path.join(val_dir, "report_0.2.txt"), 'w') as f:
                f.write(classification_report(y, self.predict_with_threshold(x, 0.2)))
            with open(os.path.join(val_dir, "report_0.1.txt"), 'w') as f:
                f.write(classification_report(y, self.predict_with_threshold(x, 0.1)))

        except AttributeError:
            return

    def save_testing_config(self, testing_config_path):
        copyfile(src=testing_config_path, dst=os.path.join(self._model_path, 'testing_settings.ini'))

