import imblearn.combine as combinedsampling
import imblearn.over_sampling as oversampling
import sklearn.preprocessing as preprocessing
from imblearn.pipeline import Pipeline as ImbPipeline
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import SGDClassifier, LogisticRegression
from sklearn.model_selection import GridSearchCV, StratifiedKFold
from sklearn.naive_bayes import ComplementNB, GaussianNB
from sklearn.neural_network import MLPClassifier
from sklearn.svm import SVC, LinearSVC

from .utils import import_gridsearch_args, import_sampler, import_preprocessor


class ModelFactory:
    def __init__(self, model_config_path, classifier_type, cv_folds=5, scoring_func='roc_auc', random_state=0):
        self.classifier_type = classifier_type.lower()
        self.gridsearch_args = import_gridsearch_args(model_config_path, self.classifier_type)
        self.cv_folds = cv_folds
        self.sampler_name = import_sampler(model_config_path)
        self.preprocessor_name = import_preprocessor(model_config_path)
        self.random_state = random_state
        self.scoring_func = scoring_func

    def make_model(self):
        preprocessor = self.__make_preprocessor()
        sampler = self.__make_sampler()
        classifier = self.__make_classifier()
        stages = []

        if preprocessor is not None:
            stages.append(['preprocessor', preprocessor])
        if sampler is not None:
            stages.append(['sampler', sampler])

        stages.append(['classifier', classifier])

        pipe = ImbPipeline(stages)

        stratified_kfold = StratifiedKFold(n_splits=self.cv_folds,
                                           shuffle=True,
                                           random_state=self.random_state)

        return GridSearchCV(estimator=pipe,
                            param_grid=self.gridsearch_args,
                            scoring=self.scoring_func,
                            cv=stratified_kfold,
                            n_jobs=-1,
                            verbose=3)

    def __make_preprocessor(self):
        name_to_implementation = {
            'MaxAbsScaler': preprocessing.MaxAbsScaler,
            'MinMaxScaler': preprocessing.MinMaxScaler,
            'Normalizer': preprocessing.Normalizer,
            'RobustScaler': preprocessing.RobustScaler,
            'StandardScaler': preprocessing.StandardScaler,
        }
        implementation = name_to_implementation.get(self.preprocessor_name, None)
        if implementation is None:
            return None

        return implementation()

    def __make_sampler(self):
        name_to_implementation = {
            'SMOTE': oversampling.SMOTE,
            'ADASYN': oversampling.ADASYN,
            'BorderlineSMOTE': oversampling.BorderlineSMOTE,
            'SMOTETomek': combinedsampling.SMOTETomek,
            'SMOTEENN': combinedsampling.SMOTEENN
        }
        implementation = name_to_implementation.get(self.sampler_name, None)
        if implementation is None:
            return None
        return implementation()

    def __make_classifier(self):
        type_to_implementation = {
            'rf': RandomForestClassifier,
            'svc': SVC,
            'lsvc': LinearSVC,
            'sgd': SGDClassifier,
            'mlp': MLPClassifier,
            'gnb': GaussianNB,
            'cnb': ComplementNB,
            'lrc': LogisticRegression,
        }
        implementation = type_to_implementation.get(self.classifier_type, None)
        if implementation is None:
            return None
        return implementation()
