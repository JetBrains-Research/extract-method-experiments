from src.model import OCCModel, BinaryModel
from src.preprocessor import Preprocessor
from src.utils import import_train_configuration

if __name__ == "__main__":
    config_path = "settings/training_settings_1.ini"
    config = import_train_configuration(config_file=config_path)
    dataset_preprocessor = Preprocessor(config)

    if config.get('model_type') == 'OCC':
        neutral, positive = dataset_preprocessor.make_neutral_positive(consider_quantile=False)
        model = OCCModel(config)
        model.train(neutral, positive)
        model.save_training_config(config_path)

    else:
        features, targets = dataset_preprocessor.make_binary()
        model = BinaryModel(config)
        model.train(features, targets)
        model.save_training_config(config_path)
