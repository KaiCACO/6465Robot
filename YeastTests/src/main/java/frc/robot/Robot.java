// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This is a demo program showing the use of the DifferentialDrive class, specifically it contains
 * the code necessary to operate a robot with tank drive.
 */
public class Robot extends TimedRobot {
  private DifferentialDrive m_robotDrive;
  private Joystick m_Joystick;

  private final CANSparkMax m_frontLeft = new CANSparkMax(1, MotorType.kBrushless);
  private final CANSparkMax m_frontRight = new CANSparkMax(3, MotorType.kBrushless);
  private final CANSparkMax m_backLeft = new CANSparkMax(2, MotorType.kBrushless);
  private final CANSparkMax m_backRight = new CANSparkMax(4, MotorType.kBrushless);

  private final MotorControllerGroup m_leftMotor = new MotorControllerGroup(m_frontLeft, m_backLeft);
  private final MotorControllerGroup m_rightMotor = new MotorControllerGroup(m_frontRight, m_backRight);

  private NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
  private NetworkTableEntry tx;
  private NetworkTableEntry ty;
  private NetworkTableEntry ta;

  private double x;
  private double y;
  private double area;

  private double turnSpeed = 0;
  

  @Override
  public void robotInit() {
    m_rightMotor.setInverted(true);
    m_robotDrive = new DifferentialDrive(m_leftMotor::set, m_rightMotor::set);
    
  }

  @Override
  public void robotPeriodic() {
    table = NetworkTableInstance.getDefault().getTable("limelight");
    tx = table.getEntry("tx");
    ty = table.getEntry("ty");
    ta = table.getEntry("ta");

    //read values periodically
    x = tx.getDouble(0.0);
    y = ty.getDouble(0.0);
    area = ta.getDouble(0.0);

    //post to smart dashboard periodically
    SmartDashboard.putNumber("LimelightX", x);
    SmartDashboard.putNumber("LimelightY", y);
    SmartDashboard.putNumber("LimelightArea", area);
  }

  @Override
  public void testPeriodic() {
    double power = Math.abs(x/120);
    power = Math.pow(power, 0.4);
    var slowdown = 1.7;
    
    if (x < 0) {
      power *= -1;
    }

    if (area > 0.3) {
      System.out.println("turning at" + turnSpeed);
      turnSpeed += (power - turnSpeed)/48;
    }
    else {
      System.out.println("No ring found!");
      turnSpeed = turnSpeed/1.1;
    }

    m_robotDrive.tankDrive(turnSpeed/slowdown, -turnSpeed/slowdown);

  }
}
