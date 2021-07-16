import os

from src.model_factory import ModelFactory
from src.utils import import_model_args


class Model:
    def __init__(self, config):
        self.model_type = config.get('model_type')
        self.model_config_path = config.get('model_config_path')

        self.model = ModelFactory(self.model_config_path, self.model_type).make_model()
        print("Made model!")  # TODO: Introduce logger here

    def set_model(self, model_config_path, model_type):
        self.model = ModelFactory(model_config_path, model_type).make_model()

    def fit(self, features, targets=None):
        if self.model_type == "OCC":
            self.model.fit(features)
        else:
            self.model.fit(features, targets)

    def predict(self, features):
        return self.model.predict(features)
