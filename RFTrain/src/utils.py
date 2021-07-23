import configparser
import os


def import_train_configuration(config_file):
    """
    Read the config file regarding the training and import its content
    """
    content = configparser.ConfigParser()
    content.read(config_file)
    config = {'datasets_dir_path': content['datasets'].get('datasets_dir_path'),
              'positive_dataset_name': content['datasets'].get('positive_dataset_name'),
              'negative_dataset_name': content['datasets'].get('negative_dataset_name'),
              'quantile_to_negative': content['datasets'].getfloat('quantile_to_negative'),
              'random_state': content['datasets'].getint('random_seed'),
              'model_type': content['model'].get('model_type'),
              'model_config_path': os.path.join(content['model'].get('model_config_dir'),
                                                content['model'].get('model_config_name')),
              'model_train_dir': content['model'].get('model_train_dir'),
              'cv_folds': content['training params'].getint('cross_val_folds')
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
        'RF': import_rf_args,
        'SVC': import_svc_args,
        'LSVC': import_linear_svc_args,
        'SGD': import_sgd_args,
        'DNN': import_dnn_args,
        'OCC': import_occ_args,
        'GNB': import_gnb_args,
        'CNB': import_cnb_args,
    }

    content = configparser.ConfigParser()
    content.read(model_config_path)

    return type_to_config_parser[model_type](content['settings'])


def import_rf_args(settings):
    """Returns parsed config for RandomForest model from provided settings"""
    args = {
        'n_estimators': settings.getint('n_estimators', fallback=10),
        'criterion': settings.get('criterion', fallback='gini'),
        'max_depth': settings.getint('max_depth', fallback=None),
        'min_samples_split': settings.getint('min_samples_split', fallback=2),
        'min_samples_leaf': settings.getint('min_samples_leaf', fallback=1),
        'bootstrap': settings.getboolean('bootstrap', fallback=False),
    }
    return args


def import_svc_args(settings):
    args = {
        'C': settings.getfloat('regularization_coefficient', fallback=1.0),
        'kernel': settings.get('kernel', fallback='rbf'),
        'degree': settings.getint('poly_kernel_degree', fallback=3),
        'gamma': settings.get('gamma', fallback='scale'),
        'tol': settings.getfloat('tolerance_for_stopping', fallback=1e-3),
    }
    return args


def import_linear_svc_args(settings):
    args = {
        'C': settings.getfloat('regularization_coefficient', fallback=1.0),
        'tol': settings.getfloat('tolerance_for_stopping', fallback=1e-4),
        'loss': settings.get('loss', fallback='squared_hinge'),
        'dual': settings.getboolean('dual', fallback=False),
        'penalty': settings.get('penalty', fallback='l2'),
    }
    return args


def import_sgd_args(settings):
    args = {
        'alpha': settings.getfloat('alpha', fallback=1e-4),
        'tol': settings.getfloat('tolerance_for_stopping', fallback=1e-3),
        'loss': settings.get('loss', fallback='hinge'),
        'max_iter': settings.getint('max_iter', fallback=1000),
        'penalty': settings.get('penalty', fallback='l2'),
    }
    return args


def import_dnn_args(settings):
    """Returns parsed config for DeepNeuralNetwork model from provided settings"""
    args = {
        'optimizer': settings.get('optimizer', fallback='Adadelta'),
        'loss': settings.get('loss', fallback='binary_crossentropy'),
        'hidden_layers_width': list(map(int, (settings.get('hidden_layers_width', fallback='512, 128')).split(','))),
        'has_dropout': settings.getboolean('has_dropout', fallback=True),
        'dropout_rate': settings.getfloat('dropout_rate', fallback=0.5),
        'epoch_count': settings.getint('epoch_count', fallback=100)
    }
    return args


def import_occ_args(settings):
    """Returns parsed config for OneClassClassification with SVM model from provided settings"""
    args = {
        'kernel': settings.get('kernel', fallback='rbf'),
        'degree': settings.getint('poly_kernel_degree', fallback=3),
        'gamma': settings.get('gamma', fallback='scale'),
        'tol': settings.getfloat('tolerance_for_stopping', fallback=1e-3),
        'nu': settings.getfloat('nu', fallback=0.5),
    }
    return args


def import_gnb_args(settings):
    args = {
        'var_smoothing': settings.getfloat('var_smoothing', fallback=1e-9)
    }
    return args


def import_cnb_args(settings):
    args = {
        'alpha': settings.getfloat('alpha', fallback=1.0),
        'norm': settings.getboolean('norm', fallback=False),
    }
    return args
