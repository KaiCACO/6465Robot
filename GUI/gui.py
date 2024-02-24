import PySimpleGUI as sg

sg.change_look_and_feel("Dark Red")

layout = [
    [sg.Text("Select alliance color:"), sg.Combo(["Red", "Blue"], key="-ALLIANCE-", enable_events=False)],
    [sg.Text("Select auto:"), sg.Combo(["Right", "Middle", "Left"], key="-AUTO-", enable_events=False)],
    [sg.Button("Return", key="-RETURN_AUTO-", enable_events=True)]
]
window = sg.Window("Dynamic UI", layout)

def return_auto_settings(event, values):
    alliance = values["-ALLIANCE-"].lower()
    auto = values["-AUTO-"].lower()
    print(f"Alliance: {alliance}")
    print(f"Auto: {auto}")

while True:
    event, values = window.read(timeout=1000)

    if event == sg.WIN_CLOSED:
        break
    
    if event == "-RETURN_AUTO-":
        return_auto_settings(event, values)

window.close()
