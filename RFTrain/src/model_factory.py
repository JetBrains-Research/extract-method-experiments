from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import SGDClassifier
from sklearn.naive_bayes import ComplementNB, GaussianNB
from sklearn.svm import SVC, LinearSVC, OneClassSVM
from keras.models import Sequential
from keras.layers import Dense, Dropout
from keras.wrappers.scikit_learn import KerasClassifier
from .utils import import_model_args


def compile_keras_model(optimizer, loss, hidden_layers_width,
                        has_dropout, dropout_rate, epoch_count, batch_size):
    mdl = Sequential()
    for width in hidden_layers_width:
        mdl.add(Dense(width, activation='relu'))
        if has_dropout:
            mdl.add(Dropout(dropout_rate))
    mdl.add(Dense(1, activation='sigmoid'))
    mdl.compile(loss=loss, optimizer=optimizer)
    return KerasClassifier(build_fn=lambda: mdl, nb_epoch=epoch_count, batch_size=batch_size)


class ModelFactory:
    def __init__(self, model_config_path, model_type):
        self.model_type = model_type.lower()
        self.model_args = import_model_args(model_config_path, self.model_type)

    def make_model(self):
        type_to_maker = {
            'rf': self.make_rf,
            'svm': self.make_svc,
            'lsvm': self.make_lsvc,
            'sgd': self.make_sgd,
            'dnn': self.make_dnn,
            'occ': self.make_occ,
            'gnb': self.make_gnb,
            'cnb': self.make_cnb,
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
        return compile_keras_model(**self.model_args)

    def make_occ(self):
        return OneClassSVM(**self.model_args)

    def make_gnb(self):
        return GaussianNB(**self.model_args)

    def make_cnb(self):
        return ComplementNB(**self.model_args)
