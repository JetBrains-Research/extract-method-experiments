import os

from src.trainer import train_by_config

directory = 'settings/'
for filename in os.listdir(directory):
    config_path = os.path.join(directory, filename)
    print(f'Training model in accordance with {config_path} config')
    try:
        train_by_config(config_path)
    except:
        print("failed at "+config_path)
