import numpy as np
import math

DEGREES_TO_RAD = math.pi / 180

def smooth_motion(poses, current_pos):
    try:
        smoothed_pos = np.mean(poses)
    except:
        smoothed_pos = 0

    alpha = 0.4 # Smoothing strength
    smoothed_pos = alpha * smoothed_pos + (1 - alpha) * current_pos

    return smoothed_pos

def y_to_z(y, LIMELIGHT_HEIGHT):
    return float(500 * LIMELIGHT_HEIGHT * math.tan(float(y) * DEGREES_TO_RAD))

def shorten(list: list, max_len : int):
    if(len(list) > max_len):
        return list[-max_len:]
    else:
        return list
