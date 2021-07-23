from src.model import Model, OCCModel, BinaryModel
from src.preprocessor import Preprocessor
from src.utils import import_train_configuration

if __name__ == "__main__":

    config_path = "settings/training_settings.ini"

    config = import_train_configuration(config_file=config_path)

    dataset_preprocessor = Preprocessor(config)
    model = Model(config)

    if config.get('model_type') == 'OCC':
        neutral, positive = dataset_preprocessor.make_neutral_positive(consider_quantile=False)
        model = OCCModel(config)
        model.train(neutral, positive)
        model.save_training_config(config_path)

    else:
        features, targets = dataset_preprocessor.make_binary()
        model = BinaryModel(config)
        model.train(features, targets)
        model.cross_validate(features, targets, config.get('cv_folds'))
        model.save_training_config(config_path)
