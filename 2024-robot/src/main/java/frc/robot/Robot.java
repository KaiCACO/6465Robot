// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DigitalInput;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private Command m_autonomousCommand;
  private RobotContainer m_robotContainer;
  private final XboxController xbox = new XboxController(Constants.OIConstants.kDriverControllerPort);

  // CANS 1-8: Drive train
  private final Pigeon2 gyro = new Pigeon2(42);
  private final DigitalInput m_ScrewLimitBack = new DigitalInput(0);
  private final DigitalInput m_ScrewLimitFront = new DigitalInput(1);
  private final CANSparkMax m_LeadScrew = new CANSparkMax(9, MotorType.kBrushless);
  private final CANSparkMax m_Intake = new CANSparkMax(10, MotorType.kBrushless);
  private final CANSparkMax m_ArmLeft = new CANSparkMax(11, MotorType.kBrushless);
  private final CANSparkMax m_ArmRight = new CANSparkMax(12, MotorType.kBrushed);
  private final CANSparkMax m_ShooterLeft = new CANSparkMax(13, MotorType.kBrushless);
  private final CANSparkMax m_ShooterRight = new CANSparkMax(14, MotorType.kBrushless);

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
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
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
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

    /*
     * String autoSelected = SmartDashboard.getString("Auto Selector",
     * "Default"); switch(autoSelected) { case "My Auto": autonomousCommand
     * = new MyAutoCommand(); break; case "Default Auto": default:
     * autonomousCommand = new ExampleCommand(); break; }
     */

    // schedule the autonomous command (example)
    if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
    }
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
    gyro.reset();
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    // Controls the drivetrain
    RobotContainer.EasyDrive(xbox.getLeftY(), xbox.getLeftX(), xbox.getRightX());

    // Lead screw control
    if (xbox.getPOV() == 0 && m_ScrewLimitFront.get()) {
      m_LeadScrew.set(1);
    }
    else if (xbox.getPOV() == 180 && m_ScrewLimitBack.get()) {
      m_LeadScrew.set(-1);
    }
    else {
      m_LeadScrew.set(0);
    }

    // Intake control
    if (xbox.getAButton()) {
      m_Intake.set(1);
    }
    else {
      m_Intake.set(0);
    }

    // Arm control
    if (xbox.getLeftTriggerAxis() > 0.1) {
      m_ArmLeft.set(1);
      m_ArmRight.set(-1);
    }
    else if (xbox.getRightTriggerAxis() > 0.1) {
      m_ArmLeft.set(-1);
      m_ArmRight.set(1);
    }
    else {
      m_ArmLeft.set(0);
      m_ArmRight.set(0);
    }

    // Shooter control
    if (xbox.getLeftBumper()) {
      m_ShooterLeft.set(.8);
      m_ShooterRight.set(-.8);
    }
    else if (xbox.getRightBumper()) {
      m_ShooterLeft.set(-.8);
      m_ShooterRight.set(.8);
    }
    else {
      m_ShooterLeft.set(0);
      m_ShooterRight.set(0);
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
}
