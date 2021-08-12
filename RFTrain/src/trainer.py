from .model import Model, TestModel
from .dataset_importer import TrainImporter, TestImporter
from .utils import import_train_configuration, import_test_configuration
from sklearn.metrics import classification_report


def train_by_config(config_path):
    config = import_train_configuration(config_file=config_path)
    dataset_importer = TrainImporter(config)

    if config.get('classifier_type').lower() == 'k-dnn':
        pass

    else:
        x_train, x_test, y_train, y_test = dataset_importer.make_binary()
        model = Model(config)
        model.train(x_train, y_train)
        model.save_training_config(config_path)
        model.validate(x_test, y_test)
        # model.save_pmml(model.get_best_estimator())


def test_by_config(config_path):
    config = import_test_configuration(config_path)
    dataset_importer = TestImporter(config)

    x, y = dataset_importer.make_test()

    model = TestModel(config.get('model_path'))
    print(classification_report(y, model.predict(x)))
