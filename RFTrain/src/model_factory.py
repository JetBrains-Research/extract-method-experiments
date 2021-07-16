from sklearn.ensemble import RandomForestClassifier
from sklearn.svm import SVC, LinearSVC, OneClassSVM
from sklearn.linear_model import SGDClassifier
from sklearn.naive_bayes import ComplementNB, GaussianNB

from src.utils import import_model_args


class ModelFactory:
    def __init__(self, model_config_path, model_type):
        self.model_type = model_type
        self.model_args = import_model_args(model_config_path, self.model_type)

    def make_model(self):
        type_to_maker = {
            'RF': self.make_rf,
            'SVM': self.make_svc,
            'LSVM': self.make_lsvc,
            'SGD': self.make_sgd,
            'DNN': self.make_dnn,
            'OCC': self.make_occ,
            'GNB': self.make_gnb,
            'CNB': self.make_cnb,
        }

        return type_to_maker[self.model_type]()

    def make_rf(self):
        return RandomForestClassifier(**self.model_args)

    def make_svc(self):
        return SVC(**self.model_args)

    def make_lsvc(self):
        return LinearSVC(**self.model_args)

    def make_sgd(self):
        return SGDClassifier(**self.model_args)

    def make_dnn(self):
        pass  # TODO: Add implementation of custom keras-based neural network

    def make_occ(self):
        return OneClassSVM(**self.model_args)

    def make_gnb(self):
        return GaussianNB(**self.model_args)

    def make_cnb(self):
        return ComplementNB(**self.model_args)
