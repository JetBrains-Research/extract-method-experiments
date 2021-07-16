import os

from src.model_factory import ModelFactory
from src.utils import import_model_args


class ModelWrapper:
    def __init__(self, config):
        self.path_to_pos = os.path.join(config.get('datasets_dir_path'), config.get('positive_dataset_name'))
        self.path_to_neg = os.path.join(config.get('datasets_dir_path'), config.get('negative_dataset_name'))
        self.model_type = config.get('model_type')
        self.model_config_path = config.get('model_config_path')

        self.model = ModelFactory(self.model_config_path, self.model_type).make_model()
        print("Made model!") # TODO: Introduce logger here

    def set_model(self, model_config_path, model_type):
        self.model = ModelFactory(model_config_path, model_type).make_model()