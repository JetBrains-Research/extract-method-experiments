import os

from src.model import BinaryModel
from src.preprocessor import Preprocessor
from src.utils import import_train_configuration

if __name__ == "__main__":
    directory = 'settings/'
    for filename in os.listdir(directory):
        config_path = os.path.join(directory, filename)
        print(f'Training model in accordance with {config_path} config')
        config = import_train_configuration(config_file=config_path)
        dataset_preprocessor = Preprocessor(config)

        if config.get('model_type') == 'OCC':
            print("No one-class classification")

        else:
            try:
                features, targets = dataset_preprocessor.make_binary()
                model = BinaryModel(config)
                model.train(features, targets)
                model.save_training_config(config_path)
            except:
                print("Something went wrong")
