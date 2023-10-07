// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj.XboxController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.DigitalInput;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private CANSparkMax arm = new CANSparkMax(9, MotorType.kBrushless);
  private CANSparkMax intakeL = new CANSparkMax(10, MotorType.kBrushless);
  private CANSparkMax intakeR = new CANSparkMax(11, MotorType.kBrushless);

  private XboxController xbox = new XboxController(1);
  private Joystick joystick = new Joystick(0);

  private DigitalInput limitSwitch = new DigitalInput(1);

  private Command m_autonomousCommand;

  private RobotContainer m_robotContainer;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_robotContainer = new RobotContainer();
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }

  /** This function is called once each time the robot enters Disabled mode. */
  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  /** This autonomous runs the autonomous command selected by your {@link RobotContainer} class. */
  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();
    if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
    }
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    RobotContainer.rot = xbox.getRightX();

    var newValues = offsetJoystick(xbox.getLeftX(), xbox.getLeftY(), RobotContainer.rotOffset);
    RobotContainer.transX = newValues[0];
    RobotContainer.transY = newValues[1];
    

    if (xbox.getLeftBumper() && limitSwitch.get()) {
      arm.set(.4);
    }
    else if (xbox.getRightBumper()) {
      arm.set(-.4);
    }
    else {
      arm.set(0);
    }

    if (xbox.getAButton()) {
      intakeL.set(0.5);
      intakeR.set(-0.5);
    }
    else if (xbox.getBButton()) {
      intakeL.set(-1);
      intakeR.set(1);
    }
    else {
      intakeL.set(0);
      intakeR.set(0);
    }
  }

  @Override
  public void testInit() {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  private static double[] offsetJoystick(double x, double y, double degrees) {
    double radians = Math.toRadians(degrees);
    double newX = x * Math.cos(radians) - y * Math.sin(radians);
    double newY = x * Math.sin(radians) + y * Math.cos(radians);

    newX = Math.round(newX * 1000000.0) / 1000000.0;
    newY = Math.round(newY * 1000000.0) / 1000000.0;

    double[] newValues = {newX, newY};
    return newValues;
  }
}
