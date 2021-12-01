import os
import logging
from src.trainer import train_by_config

logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)
directory = 'train_settings/'
for filename in os.listdir(directory):
    config_path = os.path.join(directory, filename)
    logging.info(f'Training model in accordance with {config_path} config')

    try:
        train_by_config(config_path)
    except Exception as err:
        print(err)
