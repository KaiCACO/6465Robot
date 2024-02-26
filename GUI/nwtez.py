from networktables import NetworkTables

import logging
logging.basicConfig(level=logging.DEBUG)

def connectionListener(connected, info):
    print(info, "; Connected=%s" % connected)

class Table():
    def __init__(self, onChange, ip : str='roborio-6465-frc.local'):
        NetworkTables.initialize(server=ip)

        self.onChange = onChange
        NetworkTables.addConnectionListener(connectionListener)

        self.sd = NetworkTables.getTable("SmartDashboard")
        self.sd.addEntryListener(onChange)
    
    def get(self, option : str, getType : str = "number"):
        output = None

        if getType == "number":
            output = self.sd.getNumber(option)

        if not output:
            print("Invalid table entry!")
        
        return output
    
    def put(self, key, value):
        print(f"putting {key, value}")
        try:
            self.sd.putNumber(key, value)
        except:
            print(f"Typeerror: {value} is not a number! Key: {key}")

