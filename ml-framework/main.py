from src.trainer import train_by_config, test_by_config
import os

train_by_config(os.path.join('train_settings', 'training_settings_3.ini'))
#
# test_by_config(os.path.join('test_settings', 'test_settings.ini'))
