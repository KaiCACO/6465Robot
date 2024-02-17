// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants.OIConstants;


public class EasyDrive {
    public static void driver(double leftY, double leftX, double rightX) {
        RobotContainer.m_robotDrive.drive(
            -MathUtil.applyDeadband(leftY, OIConstants.kDriveDeadband),
            -MathUtil.applyDeadband(leftX, OIConstants.kDriveDeadband),
            -MathUtil.applyDeadband(rightX, OIConstants.kDriveDeadband),
            true, false);
    }
}
