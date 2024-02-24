import time
import sys
from networktables import NetworkTables

import logging
logging.basicConfig(level=logging.DEBUG)

ip = 'roborio-6465-frc.local'
#ip = sys.argv[1]

NetworkTables.initialize(server=ip)

def valueChanged(table, key, value, isNew):
    print("valueChanged: key: '%s'; value: '%s'; isNew: '%s'" % (key, value, isNew))

def connectionListener(connected, info):
    print(info, "; Connected=%s" % connected)

NetworkTables.addConnectionListener(connectionListener)

sd = NetworkTables.getTable("SmartDashboard")
sd.addEntryListener(valueChanged)

while True:
    time.sleep(1)
