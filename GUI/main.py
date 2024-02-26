import time
from nwtez import Table
from gui import runFile
import subprocess as sp

def valueChanged(table, key, value, isNew):
    print("valueChanged: key: '%s'; value: '%s'; isNew: '%s'" % (key, value, isNew))

def runFile(filepath):
    import subprocess, os, platform
    if platform.system() == 'Darwin': 
        subprocess.call(('open', filepath))
    elif platform.system() == 'Windows':
        os.startfile(filepath)
    else:
        subprocess.call(('xdg-open', filepath))

robotTable = Table(valueChanged)
runFile('index.html')

if __name__ == "__main__":
    runFile("index.html")

while True:
    time.sleep(0.1)
