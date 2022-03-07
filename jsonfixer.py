import argparse
import os

parser = argparse.ArgumentParser()
parser.add_argument("infile", help="file with invalid json")
args = parser.parse_args()

with open(args.infile, "r+", encoding="utf-8") as file:
    # Move the pointer (similar to a cursor in a text editor) to the end of the file
    file.seek(0, os.SEEK_END)

    # This code means the following code skips the very last character in the file -
    # i.e. in the case the last line is null we delete the last line
    # and the penultimate one
    pos = file.tell() - 1

    # Read each character in the file one at a time from the penultimate
    # character going backwards, searching for a newline character
    # If we find a new line, exit the search
    while pos > 0 and file.read(1) != "\n":
        pos -= 1
        file.seek(pos, os.SEEK_SET)

    pos -= 1  # move position once more to remove trailing comma

    # So long as we're not at the start of the file, delete all the characters ahead
    # of this position
    if pos > 0:
        file.seek(pos, os.SEEK_SET)
        file.truncate()

with open(args.infile, 'a') as file:
    file.write(']')
