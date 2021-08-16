from sklearn.metrics import classification_report

from .dataset_importer import TrainImporter, TestImporter
from .model import Model, TestModel
from .utils import import_train_configuration, import_test_configuration


def train_by_config(config_path):
    config = import_train_configuration(config_file=config_path)
    dataset_importer = TrainImporter(config)

    x, y = dataset_importer.make_binary()
    model = Model(config)
    model.train(x, y)
    model.save_training_config(config_path)

    # saving model in pmml can cause errors, use with care
    # model.save_pmml()


def test_by_config(config_path):
    config = import_test_configuration(config_path)
    dataset_importer = TestImporter(config)

    x, y = dataset_importer.make_test()

    model = TestModel(config.get('model_path'))
    print(classification_report(y, model.predict(x)))
