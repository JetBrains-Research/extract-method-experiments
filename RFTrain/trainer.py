from sklearn.metrics import confusion_matrix, f1_score
from sklearn.model_selection import train_test_split, cross_val_score

from src.preprocessor import Preprocessor
from src.model import Model, OCCModel, BinaryModel
from src.utils import import_train_configuration


# def occ_score(y_true, y_pred):
#     conf_matrix = confusion_matrix(y_true, y_pred, labels=[-1, 1])
#     tn, fp, fn, tp = conf_matrix.ravel()
#     precision = tp / (tp + fp + 1e-10)
#     recall = tp / (tp + fn + 1e-10)
#
#     return (precision ** 3) * recall


def binary_score(y_true, y_pred):
    conf_matrix = confusion_matrix(y_true, y_pred, labels=[1, 0])
    tn, fp, fn, tp = conf_matrix.ravel()
    precision = tp / (tp + fp + 1e-10)
    recall = tp / (tp + fn + 1e-10)

    return (precision ** 3) * recall


def one_class_train(model, dataset_preprocessor):
    neutral, positive = dataset_preprocessor.make_neutral_positive(False)
    model.fit(neutral)
    pos_accuracy = sum(model.predict(positive) == -1) / len(positive)


def two_class_train(model, dataset_preprocessor):
    features, targets = dataset_preprocessor.make_binary()
    X_train, X_test, y_train, y_test = train_test_split(features, targets)
    cv_scores = cross_val_score(model._model, X=X_train, y=y_train, cv=10, scoring='f1')
    f1 = f1_score(y_test, model.predict(y_train))



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
