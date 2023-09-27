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
    transX = m_Joystick.getX();
    transY = -m_Joystick.getY();
    rot = m_Xbox.getRightX();
  }

}
