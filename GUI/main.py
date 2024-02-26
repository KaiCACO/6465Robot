from nwtez import Table
import server
import webbrowser

def valueChanged(table, key, value, isNew):
    print("valueChanged: key: '%s'; value: '%s'; isNew: '%s'" % (key, value, isNew))

robotTable = Table(valueChanged)
webbrowser.open('http://127.0.0.1:5000/')
server.run()
