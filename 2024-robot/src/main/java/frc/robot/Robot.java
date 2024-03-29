// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants.DriveConstants;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private Command m_autonomousCommand = null;
  private RobotContainer m_robotContainer;
  private final XboxController xbox = new XboxController(Constants.OIConstants.kDriverControllerPort);

  // Motors/sensors
  private final Pigeon2 gyro = new Pigeon2(DriveConstants.kGyroCanId);
  private final DigitalInput m_ScrewLimitBack = new DigitalInput(DriveConstants.kBackScrewLimitChannel);
  private final DigitalInput m_ScrewLimitFront = new DigitalInput(DriveConstants.kFrontScrewLimitChannel);
  private final CANSparkMax m_LeadScrew = new CANSparkMax(DriveConstants.kLeadScrewCanId, MotorType.kBrushless);
  private final CANSparkMax m_Intake = new CANSparkMax(DriveConstants.kIntakeCanId, MotorType.kBrushless);
  private final CANSparkMax m_ArmLeft = new CANSparkMax(DriveConstants.kArmLeftCanId, MotorType.kBrushless);
  private final CANSparkMax m_ArmRight = new CANSparkMax(DriveConstants.kArmRightCanId, MotorType.kBrushless);
  private final CANSparkMax m_ShooterLeft = new CANSparkMax(DriveConstants.kShooterLeftCanId, MotorType.kBrushless);
  private final CANSparkMax m_ShooterRight = new CANSparkMax(DriveConstants.kShooterRightCanId, MotorType.kBrushless);

  // Other
  private final DoubleSolenoid m_armLeftBreak = new DoubleSolenoid(PneumaticsModuleType.REVPH, DriveConstants.kArmLeftBreakPort, DriveConstants.kArmLeftBreakPort2);
  private final DoubleSolenoid m_armRightBreak = new DoubleSolenoid(PneumaticsModuleType.REVPH, DriveConstants.kArmRightBreakPort, DriveConstants.kArmRightBreakPort2);
  private final Compressor m_compressor = new Compressor(PneumaticsModuleType.REVPH);

  private boolean toggleLock = false;
  private boolean bTogCon = false;
  private boolean sevenTog = false;
  private boolean ampTog = false;
  private boolean eightTog = false;
  private boolean speakerTog = false;

  

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_LeadScrew.getEncoder().setPositionConversionFactor(1/117.44);
    m_robotContainer = new RobotContainer();
    m_compressor.enableDigital();

    m_ShooterLeft.setInverted(true);
    m_ShooterRight.setInverted(true);
    m_Intake.setInverted(true);
    m_LeadScrew.setInverted(true);
    m_ArmLeft.setInverted(true);
    m_ArmRight.setInverted(true);
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
    gyro.reset();
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    // System.out.println("Back screw limit: " + m_ScrewLimitBack.get());
    // System.out.println("Frpnt screw limit: " + m_ScrewLimitFront.get());
    // System.out.println("AutoAmp: " + autoAmp);
    // System.out.println("AmpTimer: " + ampAutoTimer.get());
    // System.out.println(m_LeadScrew.getEncoder().getPosition());
    // System.out.println(ampTog);

    if (xbox.getRawButton(7) && !sevenTog && !speakerTog) {
      sevenTog = true;
      ampTog = !ampTog;
    }
    else if (!xbox.getRawButton(7)) {
      sevenTog = false;
    }

    if (xbox.getRawButton(8) && !eightTog && !ampTog) {
      eightTog = true;
      speakerTog = !speakerTog;
    }
    else if (!xbox.getRawButton(8)) {
      eightTog = false;
    }

    if(xbox.getRawButton(9)) {
      gyro.reset();
      System.out.println("-- RESET ROBOT ROTATION --");
    }

    // Manual controls
    if(!ampTog && !speakerTog){
      xbox.setRumble(RumbleType.kBothRumble, 0);
    }

    // Auto functions
    else if (ampTog) {
      xbox.setRumble(RumbleType.kLeftRumble, 0.3);
    }

    else if (speakerTog) {
      xbox.setRumble(RumbleType.kRightRumble, 0.3);
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

