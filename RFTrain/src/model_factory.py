from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import SGDClassifier, LogisticRegression
from sklearn.model_selection import GridSearchCV
from sklearn.naive_bayes import ComplementNB, GaussianNB
from sklearn.neural_network import MLPClassifier
from sklearn.svm import SVC, LinearSVC, OneClassSVM

from .utils import import_model_args


class ModelFactory:
    def __init__(self, model_config_path, model_type, cv_folds=5):
        self.model_type = model_type.lower()
        self.model_args = import_model_args(model_config_path, self.model_type)
        self.cv_folds = cv_folds

    def make_model(self):
        type_to_maker = {
            'rf': self.make_rf,
            'svc': self.make_svc,
            'lsvc': self.make_lsvc,
            'sgd': self.make_sgd,
            'mlp': self.make_mlp,
            'gnb': self.make_gnb,
            'cnb': self.make_cnb,
            'lrc': self.make_lrc,
        }

        if self.model_type != 'occ':
            return GridSearchCV(type_to_maker[self.model_type](), self.model_args,
                                cv=self.cv_folds, scoring='f1', verbose=3)
        else:
            return self.make_occ()

    def make_rf(self):
        return RandomForestClassifier()

    def make_svc(self):
        return SVC()

    def make_lsvc(self):
        return LinearSVC()

    def make_sgd(self):
        return SGDClassifier()

    def make_mlp(self):
        return MLPClassifier()

    def make_lrc(self):
        return LogisticRegression()

    def make_occ(self):  # TODO: GridSearch with one-class?
        return OneClassSVM(**self.model_args)

    def make_gnb(self):
        return GaussianNB()

    def make_cnb(self):
        return ComplementNB()
