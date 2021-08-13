import configparser
import os
import re
from ast import literal_eval as make_tuple


def import_train_configuration(config_file):
    """
    Read the config file regarding the training and import its content
    """
    content = configparser.ConfigParser()
    content.read(config_file)
    config = {
        'datasets_dir_path': content['datasets'].get('datasets_dir_path'),
        'positive_dataset_name': content['datasets'].get('positive_dataset_name'),
        'negative_dataset_name': content['datasets'].get('negative_dataset_name'),
        'quantile_to_negative': content['datasets'].getfloat('quantile_to_negative'),
        'random_state': content['datasets'].getint('random_state'),
        'classifier_type': content['model'].get('classifier_type'),
        'model_config_path': os.path.join(content['model'].get('model_config_dir'),
                                          content['model'].get('model_config_name')),
        'model_train_dir': content['model'].get('model_train_dir'),
        'cv_folds': content['training params'].getint('cross_val_folds'),
        'scoring_func': content['training params'].get('scoring_func'),
    }

    return config


def import_test_configuration(config_file):
    """
    Read the config file regarding the testing and import its content
    """
    pass


def set_train_path(model_path_name):
    """
    Create a new classifier path with an incremental integer, also considering previously created classifier paths
    """
    classifiers_path = os.path.join(os.getcwd(), model_path_name, '')
    os.makedirs(os.path.dirname(classifiers_path), exist_ok=True)

    dir_content = os.listdir(classifiers_path)
    if dir_content:
        previous_versions = [int(name.split("_")[1]) for name in dir_content]
        new_version = str(max(previous_versions) + 1)
    else:
        new_version = '1'

    data_path = os.path.join(classifiers_path, 'model_' + new_version, '')
    os.makedirs(os.path.dirname(data_path), exist_ok=True)
    return data_path


def import_sampler(model_config_path):
    content = configparser.ConfigParser()
    content.read(model_config_path)
    return content['preprocessing'].get('sampler', fallback='None')


def import_preprocessor(model_config_path):
    content = configparser.ConfigParser()
    content.read(model_config_path)
    return content['preprocessing'].get('preprocessor', fallback='None')


def import_gridsearch_args(model_config_path, classifier_type):
    """
    Returns config dict based on classifier type and .ini file at the path
    """
    
    content = configparser.ConfigParser()
    content.read(model_config_path)

    classifier_args = import_classifier_args(content['hyperparameters'], classifier_type.lower())

    return to_pipeline(classifier_args, attribute_name='classifier')
    

def import_classifier_args(hyperparameters, classifier_type):
    classifier_arg_parsers = {
        'rf': import_rf_args,
        'svc': import_svc_args,
        'lsvc': import_linear_svc_args,
        'sgd': import_sgd_args,
        'mlp': import_mlp_args,
        'gnb': import_gnb_args,
        'cnb': import_cnb_args,
        'lrc': import_lrc_args,
    }
    
    return classifier_arg_parsers[classifier_type](
        hyperparameters)

def import_rf_args(hyperparameters):
    """
    Returns parsed config for RandomForest classifier from provided settings
    *Grid-search friendly
    """

    types = {
        'n_estimators': int,
        'criterion': str,
        'max_depth': int,
        'min_samples_split': int,
        'min_samples_leaf': int,
        'bootstrap': make_bool,
    }

    args = {
        'n_estimators': hyperparameters.get('n_estimators', fallback='10'),
        'criterion': hyperparameters.get('criterion', fallback='gini'),
        'max_depth': hyperparameters.get('max_depth', fallback='5'),
        'min_samples_split': hyperparameters.get('min_samples_split', fallback='2'),
        'min_samples_leaf': hyperparameters.get('min_samples_leaf', fallback='1'),
        'bootstrap': hyperparameters.get('bootstrap', fallback='False'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_svc_args(hyperparameters):
    types = {
        'C': float,
        'kernel': str,
        'degree': int,
        'gamma': str,
        'tol': float,
    }

    args = {
        'C': hyperparameters.get('C', fallback='1'),
        'kernel': hyperparameters.get('kernel', fallback='rbf'),
        'degree': hyperparameters.get('degree', fallback='3'),
        'gamma': hyperparameters.get('gamma', fallback='scale'),
        'tol': hyperparameters.get('tol', fallback='1e-3'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_linear_svc_args(hyperparameters):
    types = {
        'C': float,
        'tol': float,
        'loss': str,
        'dual': make_bool,
        'penalty': str,
    }

    args = {
        'C': hyperparameters.get('C', fallback='1'),
        'tol': hyperparameters.get('tol', fallback='1e-4'),
        'loss': hyperparameters.get('loss', fallback='squared_hinge'),
        'dual': hyperparameters.get('dual', fallback='False'),
        'penalty': hyperparameters.get('penalty', fallback='l2'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_sgd_args(hyperparameters):
    types = {
        'alpha': float,
        'tol': float,
        'loss': str,
        'max_iter': int,
        'penalty': str,
        'fit_intercept': make_bool
    }

    args = {
        'alpha': hyperparameters.get('alpha', fallback='1e-4'),
        'tol': hyperparameters.get('tol', fallback='1e-3'),
        'loss': hyperparameters.get('loss', fallback='hinge'),
        'max_iter': hyperparameters.get('max_iter', fallback='1000'),
        'penalty': hyperparameters.get('penalty', fallback='l2'),
        'fit_intercept': hyperparameters.get('fit_intercept', fallback='False'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_mlp_args(hyperparameters):
    """
    Returns parsed config for MultiLayerPerceptron classifier from provided settings
    *Grid-search friendly
    """
    types = {
        'hidden_layer_sizes': make_tuple,  # Not a type but casting func
        'activation': str,
        'solver': str,
        'alpha': float,
        'batch_size': int,
        'learning_rate': str,
        'learning_rate_init': float,
        'max_iter': int,
        'tol': float,
    }

    args = {
        'hidden_layer_sizes': hyperparameters.get('hidden_layer_sizes', fallback='(100,)'),  # Formatting matters!
        'activation': hyperparameters.get('activation', fallback='relu'),
        'solver': hyperparameters.get('solver', fallback='adam'),
        'alpha': hyperparameters.get('alpha', fallback='0.0001'),
        'batch_size': hyperparameters.get('batch_size', fallback='200'),
        'learning_rate': hyperparameters.get('learning_rate', fallback='constant'),
        'learning_rate_init': hyperparameters.get('learning_rate_init', fallback='0.001'),
        'max_iter': hyperparameters.get('max_iter', fallback='200'),
        'tol': hyperparameters.get('tol', fallback='1e-4'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_gnb_args(hyperparameters):
    types = {
        'var_smoothing': float
    }

    args = {
        'var_smoothing': hyperparameters.get('var_smoothing', fallback='1e-9')
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_cnb_args(hyperparameters):
    types = {
        'alpha': float,
        'norm': make_bool,
    }

    args = {
        'alpha': hyperparameters.get('alpha', fallback='1.0'),
        'norm': hyperparameters.get('norm', fallback='False'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_lrc_args(hyperparameters):
    """
    Returns parsed config for OneClassClassification with SVM classifier from provided settings
    *Grid-search friendly
    """
    types = {
        'penalty': str,
        'tol': float,
        'C': float,
        'fit_intercept': make_bool,
        'solver': str,
        'max_iter': int,
    }

    args = {
        'penalty': hyperparameters.get('penalty', fallback='l2'),
        'tol': hyperparameters.get('tol', fallback='1e-4'),
        'C': hyperparameters.get('C', fallback='1'),
        'fit_intercept': hyperparameters.get('fit_intercept', fallback='True'),
        'solver': hyperparameters.get('solver', fallback='lbfgs'),
        'max_iter': hyperparameters.get('max_iter', fallback='200'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def make_bool(_string):
    return _string == 'True'


def cast_to_typed_list(_string, _type):
    return list(map(_type, re.split('; |;|\s|\s', _string)))


def to_pipeline(config, attribute_name='classifier'):
    """
    Returns reformatted config dict to pipeline suitable format
    """
    old_to_new = {}
    for key in config.keys():
        old_to_new[key] = f'{attribute_name}__{key}'

    return dict((old_to_new[key], value) for (key, value) in config.items())
