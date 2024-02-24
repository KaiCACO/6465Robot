import PySimpleGUI as sg
import time
 
# Initial values for x and y
x = 50
y = 50
 
# Function to be activated when a dropdown option is selected
def dropdown_function():
    print("Dropdown option selected!")
 
# Create the layout
layout = [
    [sg.Graph(canvas_size=(400, 400), graph_bottom_left=(0, 0), graph_top_right=(400, 400), key="-GRAPH-")],
    [sg.Text("Dropdown:"), sg.Combo(["Option 1", "Option 2"], key="-DROPDOWN-")]
]

if __name__ == "__main__":
    # Create the window
    window = sg.Window("Dynamic UI", layout)
    
    # Get a reference to the graph element
    graph = window["-GRAPH-"]
    
    # Draw the initial dot
    graph.draw_circle((x, y), 10, fill_color="red")
    
    # Event loop
    while True:
        event, values = window.read()
    
        if event == sg.WIN_CLOSED:
            break
    
        # Update the position of the dot based on new values (simulated here)
        x += 5
        y += 10
    
        # Clear the graph and redraw the dot
        graph.erase()
        graph.draw_circle((x, y), 10, fill_color="red")
    
        # Check for dropdown selection and activate function
        if event == "-DROPDOWN-":
            dropdown_function()

        # Update the window
        window.refresh()

    window.close()