// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.Constants.OIConstants;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.Joystick;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class robotHighLevel {
  public static double transX = 0.0;
  public static double transY = 0.0;
  public static double rot = 0.0;

  private static XboxController m_Xbox = new XboxController(OIConstants.xboxPort);
  private static Joystick m_Joystick = new Joystick(OIConstants.joystickPort);

  private static CANSparkMax arm = new CANSparkMax(9, MotorType.kBrushless);
  private static CANSparkMax intake_L = new CANSparkMax(10, MotorType.kBrushless);
  private static CANSparkMax intake_R = new CANSparkMax(11, MotorType.kBrushless);

  public static void updateValues() {
    updateTranslation();
    updateDooHickeys();
  }

  private static void updateTranslation() {
    // joystickVector returns list of [controllerAmplitude, controllerAngle]
    var joystickVector = vectorize(m_Joystick.getX(), -m_Joystick.getY());
    // robotRot contains robot rotation from gyro
    var robotRot = 0.0;
    joystickVector[1] = joystickVector[1] - robotRot;
    var amp = joystickVector[0];
    var ang = joystickVector[1];

    // now amp and ang contain robot movement speed and angle
    transX = Math.sqrt(amp*(Math.sin(ang)*Math.sin(ang)));
    transY = Math.sqrt(amp*(Math.cos(ang)*Math.cos(ang)));
    rot = m_Xbox.getRightX();
  }

  private static double[] vectorize(double x, double y) {
    var amp = Math.sqrt((x*x)+(y*y));
    var theta = 0.0;
    if (Double.isFinite(y/x)) {
      theta = Math.atan(y/x);
    }
    if (amp < 0.01) {
      amp = 0.0;
    }
    double[] ret = new double[2];
    ret[0] = amp;
    ret[1] = theta;
    return ret;
  }

  private static void updateDooHickeys() {
    // arm angle controller
    if (m_Xbox.getRightBumper()) {
      arm.set(0.6);
    }
    else if (m_Xbox.getLeftBumper()) {
      arm.set(-0.6);
    }
    else {
      arm.set(0);
    }

    // intake controller
    if(m_Xbox.getAButton()) {
      intake_L.set(.8);
      intake_R.set(-.8);
    }
    else if(m_Xbox.getBButton()) {
      intake_L.set(-.8);
      intake_R.set(.8);
    }
    else {
      intake_L.set(0);
      intake_R.set(0);
    }
  }
}