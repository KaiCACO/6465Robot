from networktables import NetworkTables
import random

import logging
logging.basicConfig(level=logging.DEBUG)

def connectionListener(connected, info):
    print(info, "; Connected=%s" % connected)

class Table():
    def __init__(self, onChange = None, ip : str='roborio-6465-frc.local'):
        NetworkTables.initialize(server=ip)

        if onChange is not None:
            self.onChange = onChange
        NetworkTables.addConnectionListener(connectionListener)

        self.sd = NetworkTables.getTable("SmartDashboard")
        self.sd.addEntryListener(onChange)
    
    def get(self, key : str):
        return self.sd.getValue(key, "0")
    
    def put(self, key : str, value : str):
        self.sd.putString(key, value)

class TableTest():
    def __init__(self, onChange = None, ip : str='roborio-6465-frc.local'):
        NetworkTables.initialize(server=ip)

        if onChange is not None:
            self.onChange = onChange
        NetworkTables.addConnectionListener(connectionListener)

        self.sd = NetworkTables.getTable("SmartDashboard")
        self.sd.addEntryListener(onChange)

    def get(self, key : str):
        return random.randrange(-27, 27)
    
    def put(self, key : str, value : str):
        self.sd.putString(key, value)