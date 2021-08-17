import os

from src.trainer import train_by_config

directory = 'settings/'
for filename in os.listdir(directory):
    config_path = os.path.join(directory, filename)
    print(f'Training model in accordance with {config_path} config')

    train_by_config(config_path)

