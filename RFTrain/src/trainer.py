# from .kmodel import KModel
from .model import Model
from .dataset_importer import DatasetImporter
from .utils import import_train_configuration


def train_by_config(config_path):
    config = import_train_configuration(config_file=config_path)
    dataset_importer = DatasetImporter(config)

    if config.get('classifier_type').lower() == 'k-dnn':
        pass

    else:
        features, targets = dataset_importer.make_binary()
        model = Model(config)
        model.train(features, targets)
        model.save_training_config(config_path)
        # model.save_pmml(model.get_best_estimator())
