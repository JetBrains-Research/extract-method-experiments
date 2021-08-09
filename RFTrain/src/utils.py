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
        'model_type': content['model'].get('model_type'),
        'model_config_path': os.path.join(content['model'].get('model_config_dir'),
                                          content['model'].get('model_config_name')),
        'model_train_dir': content['model'].get('model_train_dir'),
        'cv_folds': content['training params'].getint('cross_val_folds'),
    }

    return config


def import_test_configuration(config_file):
    """
    Read the config file regarding the testing and import its content
    """
    pass


def set_train_path(models_path_name):
    """
    Create a new model path with an incremental integer, also considering previously created model paths
    """
    models_path = os.path.join(os.getcwd(), models_path_name, '')
    os.makedirs(os.path.dirname(models_path), exist_ok=True)

    dir_content = os.listdir(models_path)
    if dir_content:
        previous_versions = [int(name.split("_")[1]) for name in dir_content]
        new_version = str(max(previous_versions) + 1)
    else:
        new_version = '1'

    data_path = os.path.join(models_path, 'model_' + new_version, '')
    os.makedirs(os.path.dirname(data_path), exist_ok=True)
    return data_path


def import_model_args(model_config_path, model_type):
    type_to_config_parser = {
        'rf': import_rf_args,
        'svc': import_svc_args,
        'lsvc': import_linear_svc_args,
        'sgd': import_sgd_args,
        'mlp': import_mlp_args,
        'occ': import_occ_args,
        'gnb': import_gnb_args,
        'cnb': import_cnb_args,
        'lrc': import_lrc_args,
    }

    content = configparser.ConfigParser()
    content.read(model_config_path)

    return type_to_config_parser[model_type.lower()](content['settings'])


def import_rf_args(settings):
    """
    Returns parsed config for RandomForest model from provided settings
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
        'n_estimators': settings.get('n_estimators', fallback='10'),
        'criterion': settings.get('criterion', fallback='gini'),
        'max_depth': settings.get('max_depth', fallback='5'),
        'min_samples_split': settings.get('min_samples_split', fallback='2'),
        'min_samples_leaf': settings.get('min_samples_leaf', fallback='1'),
        'bootstrap': settings.get('bootstrap', fallback='False'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_svc_args(settings):
    types = {
        'C': float,
        'kernel': str,
        'degree': int,
        'gamma': str,
        'tol': float,
    }

    args = {
        'C': settings.get('C', fallback='1'),
        'kernel': settings.get('kernel', fallback='rbf'),
        'degree': settings.get('degree', fallback='3'),
        'gamma': settings.get('gamma', fallback='scale'),
        'tol': settings.get('tol', fallback='1e-3'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_linear_svc_args(settings):
    types = {
        'C': float,
        'tol': float,
        'loss': str,
        'dual': make_bool,
        'penalty': str,
    }

    args = {
        'C': settings.get('C', fallback='1'),
        'tol': settings.get('tol', fallback='1e-4'),
        'loss': settings.get('loss', fallback='squared_hinge'),
        'dual': settings.get('dual', fallback='False'),
        'penalty': settings.get('penalty', fallback='l2'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_sgd_args(settings):
    types = {
        'alpha': float,
        'tol': float,
        'loss': str,
        'max_iter': int,
        'penalty': str,
        'fit_intercept': make_bool
    }

    args = {
        'alpha': settings.get('alpha', fallback='1e-4'),
        'tol': settings.get('tol', fallback='1e-3'),
        'loss': settings.get('loss', fallback='hinge'),
        'max_iter': settings.get('max_iter', fallback='1000'),
        'penalty': settings.get('penalty', fallback='l2'),
        'fit_intercept': settings.get('fit_intercept', fallback='False'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_mlp_args(settings):
    """
    Returns parsed config for MultiLayerPerceptron model from provided settings
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
        'hidden_layer_sizes': settings.get('hidden_layer_sizes', fallback='(100,)'),  # Formatting matters!
        'activation': settings.get('activation', fallback='relu'),
        'solver': settings.get('solver', fallback='adam'),
        'alpha': settings.get('alpha', fallback='0.0001'),
        'batch_size': settings.get('batch_size', fallback='200'),
        'learning_rate': settings.get('learning_rate', fallback='constant'),
        'learning_rate_init': settings.get('learning_rate_init', fallback='0.001'),
        'max_iter': settings.get('max_iter', fallback='200'),
        'tol': settings.get('tol', fallback='1e-4'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_occ_args(settings):
    """Returns parsed config for OneClassClassification with SVM model from provided settings"""

    args = {
        'kernel': settings.get('kernel', fallback='rbf'),
        'degree': settings.getint('degree', fallback='3'),
        'gamma': settings.get('gamma', fallback='scale'),
        'tol': settings.getfloat('tol', fallback='1e-3'),
        'nu': settings.getfloat('nu', fallback='0.5'),
    }

    return args


def import_gnb_args(settings):
    types = {
        'var_smoothing': float
    }

    args = {
        'var_smoothing': settings.get('var_smoothing', fallback='1e-9')
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_cnb_args(settings):
    types = {
        'alpha': float,
        'norm': make_bool,
    }

    args = {
        'alpha': settings.get('alpha', fallback='1.0'),
        'norm': settings.get('norm', fallback='False'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def import_lrc_args(settings):
    """
    Returns parsed config for OneClassClassification with SVM model from provided settings
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
        'penalty': settings.get('penalty', fallback='l2'),
        'tol': settings.get('tol', fallback='1e-4'),
        'C': settings.get('C', fallback='1'),
        'fit_intercept': settings.get('fit_intercept', fallback='True'),
        'solver': settings.get('solver', fallback='lbfgs'),
        'max_iter': settings.get('max_iter', fallback='200'),
    }

    for key in args.keys():
        args[key] = cast_to_typed_list(args[key], types[key])

    return args


def make_bool(_string):
    return _string == 'True'


def cast_to_typed_list(_string, _type):
    return list(map(_type, re.split('; |;|\s|\s',_string)))
