// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.Constants.OIConstants;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.Joystick;

/** Add your docs here. */
public class robotHighLevel {
  // The driver's controller
  public static double transX = 0.0;
  public static double transY = 0.0;
  public static double rot = 0.0;

  private static XboxController m_Xbox = new XboxController(OIConstants.xboxPort);
  private static Joystick m_Joystick = new Joystick(OIConstants.joystickPort);

  public static void updateValues() {
    // joystickVector returns list of [controllerAmplitude, controllerAngle]
    var joystickVector = vectorize(m_Joystick.getX(), -m_Joystick.getY());
    // robotRot contains robot rotation from gyro
    var robotRot = 0.0;
    joystickVector[1] = joystickVector[1] - robotRot;
    var amp = joystickVector[0];
    var ang = joystickVector[1];

    // now amp and ang contain robot movement speed and angle
    transX = Math.sqrt((amp*amp) + (Math.sin(ang)*Math.sin(ang)));
    transY = Math.sqrt((amp*amp) + (Math.cos(ang)*Math.cos(ang)));
    rot = m_Xbox.getRightX();
  }

  private static double[] vectorize(double x, double y) {
    var amp = Math.sqrt((x*x)+(y*y));
    var theta = Math.atan(y/x);
    double[] ret = new double[2];
    ret[0] = amp;
    ret[1] = theta;
    return ret;
  }

}
