import time
import sys
from nwtez import Table

def valueChanged(table, key, value, isNew):
    print("valueChanged: key: '%s'; value: '%s'; isNew: '%s'" % (key, value, isNew))

robotTable = Table(valueChanged)

while True:
    time.sleep(0.1)
