from src.preprocessor import Preprocessor
from src.model import Model, OCCModel, BinaryModel
from src.utils import import_train_configuration

if __name__ == "__main__":
    config = import_train_configuration(config_file="settings/training_settings.ini")

    dataset_preprocessor = Preprocessor(config)
    model = Model(config)

    if config.get('model_type') == 'OCC':
        neutral, positive = dataset_preprocessor.make_neutral_positive(False)
        model = OCCModel(config)
        model.train(neutral, positive)

    else:
        features, targets = dataset_preprocessor.make_binary()
        model = BinaryModel(config)
        model.train(features, targets)
