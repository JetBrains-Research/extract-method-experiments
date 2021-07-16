from src.model_wrapper import ModelWrapper
from src.utils import import_train_configuration

if __name__ == "__main__":
    config = import_train_configuration(config_file="settings/training_settings.ini")
    model_wrapper = ModelWrapper(config)


