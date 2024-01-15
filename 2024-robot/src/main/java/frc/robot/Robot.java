// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.drive.RobotContainer;
import edu.wpi.first.wpilibj.XboxController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.DigitalInput;
import com.ctre.phoenix.sensors.WPI_Pigeon2;
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
  private static WPI_Pigeon2 gyro = new WPI_Pigeon2(12);

  private Timer autoTimer = new Timer();
  private boolean onRamp = false;

  @Override
  public void robotInit() {
    m_robotContainer = new RobotContainer();
  }
  
  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void autonomousInit() {
    gyro.reset();
    gyro.configFactoryDefault();
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
    autoTimer.stop();
    autoTimer.reset();
    autoTimer.start();
    RobotContainer.rot = 0.0;
    RobotContainer.transX = 0.0;
    RobotContainer.transY = 0.0;
  }

  @Override
  public void autonomousPeriodic() {
    var angle = -gyro.getRoll();
    if (onRamp == false && angle > 5) {
      onRamp = true;
      autoTimer.stop();
      autoTimer.reset();
      autoTimer.start();
    }
    if(true) {
      if (autoTimer.get() < 3.3 && onRamp == false) {
        RobotContainer.transX = 0.2;
      }
      else if (onRamp == true) {
        RobotContainer.transX = angle/(20+((autoTimer.get()*1.5)));
      }
      else {
        RobotContainer.transX = 0.0;
      }
    }
  }

  @Override
  public void teleopInit() {
    gyro.reset();
    gyro.configFactoryDefault();
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
  }

  @Override
  public void teleopPeriodic() {
    // var xboxVector = positionToVector(xbox.getLeftX(), xbox.getLeftY());
    // RobotContainer.rot = xboxVector[1] * calculateRotationSpeed(gyro.getYaw(), xboxVector[0]);
    RobotContainer.rot = joystick.getZ()*joystick.getZ()/1.5;
    if (joystick.getZ() < 0) {
      RobotContainer.rot = -RobotContainer.rot;
    }
    RobotContainer.rotOffset = -gyro.getYaw();

    var x = joystick.getX();
    var y = joystick.getY();

    if (Math.abs(x) < 0.03) {
      x = 0;
    }
    if (Math.abs(y) < 0.03) {
      y = 0;
    }

    var newValues = offsetJoystick(x, -y, RobotContainer.rotOffset-90);
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
    CommandScheduler.getInstance().cancelAll();
  }

  @Override
  public void testPeriodic() {}

  private static double[] offsetJoystick(double x, double y, double degrees) {
    double radians = Math.toRadians(degrees);

    double newX = x * Math.cos(radians) - y * Math.sin(radians);
    double newY = x * Math.sin(radians) + y * Math.cos(radians);

    double[] newValues = {newX, newY};
    return newValues;
  }

}
