from sklearn.metrics import classification_report

from .dataset_importer import TrainImporter, TestImporter
from .trainmodel import TrainModel, TestModel
from .utils import import_train_configuration, import_test_configuration


def train_by_config(config_path):
    config = import_train_configuration(config_file=config_path)
    dataset_importer = TrainImporter(config)

    x, y = dataset_importer.make_binary()
    print('Imported the dataset')
    model = TrainModel(config)
    print('Created the model')
    model.train(x, y)
    print('Training finished')
    model.save_training_config(config_path)

    # saving model in pmml can cause errors, use with care
    # model.save_pmml()


def test_by_config(config_path):
    config = import_test_configuration(config_path)
    dataset_importer = TestImporter(config)

    x, y = dataset_importer.make_test()

    model = TestModel(config.get('model_path'))
    model.test(x, y)
    model.save_testing_config(config_path)
    # model.save_pmml()
