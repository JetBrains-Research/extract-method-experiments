from src.model import Model
from src.utils import import_train_configuration


if __name__ == "__main__":
    config = import_train_configuration(config_file="settings/training_settings.ini")
    model = Model(config)
