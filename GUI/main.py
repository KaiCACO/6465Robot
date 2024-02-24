import time
import sys
from networktables import NetworkTables

import logging

logging.basicConfig(level=logging.DEBUG)
ip = sys.argv[1]

# NetworkTables.initialize(server='roborio-6465-frc.local')
NetworkTables.initialize(server=ip)
sd = NetworkTables.getTable("SmartDashboard")

