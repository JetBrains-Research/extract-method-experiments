import sys, getopt

def main(argc, argv):
    try:
        opts, args = getopt.getopt(argv, "hi:o:", ["ifile=", "ofile="])
    except getopt.GetoptError:
        print('deepanalysis.py -p <pos_inputfile> -n <pos_inputfile> --arg=[""]')
        sys.exit(2)
    pass
