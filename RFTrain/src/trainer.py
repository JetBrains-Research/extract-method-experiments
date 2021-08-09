from .kmodel import KModel
from .model import OCCModel, BinaryModel
from .preprocessor import Preprocessor
from .utils import import_train_configuration
from sklearn.preprocessing import Normalizer


def train_by_config(config_path):
    config = import_train_configuration(config_file=config_path)
    dataset_preprocessor = Preprocessor(config)

    if config.get('model_type').lower() == 'occ':
        neutral, positive = dataset_preprocessor.make_neutral_positive(consider_quantile=False)
        model = OCCModel(config)
        model.train(neutral, positive)
        model.save_training_config(config_path)

    elif config.get('model_type').lower() == 'k-dnn':
        features, targets = dataset_preprocessor.make_binary()
        normalizer = Normalizer().fit(features)
        features = normalizer.transform(features)
        model = KModel(config)
        model.fit(features, targets)

    else:
        features, targets = dataset_preprocessor.make_binary()
        normalizer = Normalizer().fit(features)
        features = normalizer.transform(features)
        model = BinaryModel(config)
        model.train(features, targets)
        model.save_training_config(config_path)
