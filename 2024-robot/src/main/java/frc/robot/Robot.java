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
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private Command m_autonomousCommand = null;
  private RobotContainer m_robotContainer;
  private final XboxController first_controller = new XboxController(Constants.OIConstants.kDriverControllerPort);
  private final XboxController second_controller = new XboxController(Constants.OIConstants.kSecondDriverControllerPort);

  // Motors/sensors
  private final Pigeon2 gyro = new Pigeon2(DriveConstants.kGyroCanId);
  private final DigitalInput m_ScrewLimitBack = new DigitalInput(DriveConstants.kBackScrewLimitChannel);
  // private final DigitalInput m_BackupScrewLimitBack = new DigitalInput(DriveConstants.kBackupBackScrewLimitChannel);
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

  private int autoAmp = 0;
  private Timer ampAutoTimer = new Timer();

  private int autoSpeak = 0;
  private Timer speakAutoTimer = new Timer();
  private Timer autoTimer = new Timer();
  
  private String autoLocation;

  private boolean toggleLock = false;
  private boolean bTogCon = false;
  private boolean sevenTog = false;
  private boolean ampTog = false;
  private boolean eightTog = false;
  private boolean speakerTog = false;

  private SendableChooser<String> m_autoChooser = new SendableChooser<>();

  

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

    m_autoChooser.setDefaultOption("LEFT", "l");
    m_autoChooser.addOption("MIDDLE", "m");
    m_autoChooser.addOption("RIGHT", "r");
    SmartDashboard.clearPersistent("Auto choices");
    SmartDashboard.putData("Auto choices", m_autoChooser);
    SmartDashboard.updateValues();
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
  public void disabledInit() {
    ampTog = false;
    speakerTog = false;
    sevenTog = false;
    eightTog = false;
    autoAmp = 0;
    autoSpeak = 0;
  }

  @Override
  public void disabledPeriodic() {}

  /** This autonomous runs the autonomous command selected by your {@link RobotContainer} class. */
  @Override
  public void autonomousInit() {
    SmartDashboard.updateValues();
    autoLocation = m_autoChooser.getSelected();
    System.out.println("SELECTED AUTO: " + autoLocation);
    autoTimer.reset();
    autoTimer.start();
    gyro.reset();
    
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
    m_autonomousCommand = null;

  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    double t = autoTimer.get();
    if (t < 1.5){
      m_Intake.set(0);
      m_ShooterLeft.set(-0.55);
      m_ShooterRight.set(-0.55);
    }
    else if (t < 4) {
      m_Intake.set(1);
      if (t > 2 && t < 2.5) {
        m_ShooterLeft.set(0.2);
        m_ShooterRight.set(0.2);
      }
      else if (t < 3) {
        m_ShooterLeft.set(0);
        m_ShooterRight.set(0);
        m_Intake.set(1);
      }
      if (t < 3.5 && t > 3 && m_autonomousCommand == null && autoLocation == "m") {
        m_autonomousCommand = m_robotContainer.getAutoCommand(m_Intake, m_ShooterLeft, m_ShooterRight);
        m_autonomousCommand.schedule();
      }
    }
  }

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
    // System.out.println("Backup back screw limit: " + m_BackupScrewLimitBack.get());
    // System.out.println("Front screw limit: " + m_ScrewLimitFront.get());
    // System.out.println("AutoAmp: " + autoAmp);
    // System.out.println("AmpTimer: " + ampAutoTimer.get());
    // System.out.println(m_LeadScrew.getEncoder().getPosition());
    // System.out.println(ampTog);

    if (second_controller.getRawButton(7) && !sevenTog && !speakerTog) {
      sevenTog = true;
      ampTog = !ampTog;
    }
    else if (!second_controller.getRawButton(7)) {
      sevenTog = false;
    }

    if (second_controller.getRawButton(8) && !eightTog && !ampTog) {
      eightTog = true;
      speakerTog = !speakerTog;
    }
    else if (!second_controller.getRawButton(8)) {
      eightTog = false;
    }

    if(first_controller.getRawButton(9)) {
      gyro.reset();
      System.out.println("-- ROBOT ROTATION RESET --");
    }

    // Manual controls
    if(!ampTog && !speakerTog){
      second_controller.setRumble(RumbleType.kBothRumble, 0);
      autoAmp = 0;
      autoSpeak = 0;
      ampAutoTimer.stop();
      manualControls();
    }

    // Auto functions
    else if (ampTog) {
      second_controller.setRumble(RumbleType.kLeftRumble, 0.3);
      autoSpeak = 0;

      if (autoAmp == 0) {
        ampAutoTimer.reset();
        ampAutoTimer.start();
        autoAmp = 1;
      }
      var t = ampAutoTimer.get();

      m_ShooterLeft.set(-0.18);
      m_ShooterRight.set(-0.18);

      if (!m_ScrewLimitFront.get()) {
        m_LeadScrew.set(0.5);
      }
      else {
        m_LeadScrew.set(0);
      }

      if (autoAmp == 1) {
        if (t <= 1) {
          
        }
        else if (m_ScrewLimitFront.get()) {
          ampAutoTimer.reset();
          ampAutoTimer.start();
          autoAmp = 2;
        }
      }

      else if (autoAmp == 2) {
        if (t <= 0.2) {
          m_Intake.set(-.5);
        }
        else {
          m_Intake.set(1);
        }
      }
    }

    else if (speakerTog) {
      second_controller.setRumble(RumbleType.kRightRumble, 0.3);
      autoAmp = 0;

      if (autoSpeak == 0) {
        speakAutoTimer.reset();
        speakAutoTimer.start();
        autoSpeak = 1;
      }
      var t = speakAutoTimer.get();

      m_ShooterLeft.set(-0.6);
      m_ShooterRight.set(-0.6);

      if (!m_ScrewLimitBack.get()) {
        m_LeadScrew.set(-0.5);
      }
      else {
        m_LeadScrew.set(0);
      }

      if (autoSpeak == 1) {
        if (m_ScrewLimitBack.get()) {
          speakAutoTimer.reset();
          speakAutoTimer.start();
          autoSpeak = 2;
        }
      }

      else if (autoSpeak == 2) {
        if (t <= 0.2) {
          m_Intake.set(-.5);
        }
        else {
          m_Intake.set(1);
        }
      }
    }
  }

  @Override
  public void testInit() {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
    System.out.println("Back:" + m_ScrewLimitBack.get());
  }

  private void manualControls() {
    // Controls the drivetrain
    m_robotContainer.EasyDrive(first_controller.getLeftY(), first_controller.getLeftX(), first_controller.getRightX(), true);

    // Lead screw control
    if (second_controller.getPOV() == 0 && !m_ScrewLimitFront.get()) {
      m_LeadScrew.set(.5);
    }
    else if (second_controller.getPOV() == 180 && !m_ScrewLimitBack.get()) {
      m_LeadScrew.set(-.5);
    }
    else {
      m_LeadScrew.set(0);
    }
    if (m_ScrewLimitBack.get()) {
      m_LeadScrew.getEncoder().setPosition(0);
    }
    if (m_ScrewLimitFront.get()) {
      var convFac = m_LeadScrew.getEncoder().getPositionConversionFactor();
      var encPos = m_LeadScrew.getEncoder().getPosition();
      var newFac = (1/(encPos/convFac))*convFac;
      // m_LeadScrew.getEncoder().setPositionConversionFactor(newFac);
    }

    // Intake control
    if (first_controller.getAButton()) {
      if (m_LeadScrew.getEncoder().getPosition() > 0.35) {
        m_LeadScrew.set(-0.5);
      }
      else {
        m_Intake.set(0.8);
      }
    }
    else if (first_controller.getYButton()) {
      m_Intake.set(-0.8);
    }
    else {
      m_Intake.set(0);
    }

    // Arm control
    if (second_controller.getLeftBumper()) {
      if (second_controller.getXButton()) {
        m_ArmLeft.set(-0.5);
      }
      else {
        m_ArmLeft.set(0.5);
      }
    }
    else {
      m_ArmLeft.set(0);
    }
    
    if (second_controller.getRightBumper()) {
      if (second_controller.getXButton()) {
        m_ArmRight.set(-0.5);
      }
      else {
        m_ArmRight.set(0.5);
      }
    }
    else {
      m_ArmRight.set(0);
    }

    // Arm lock control
    if (toggleLock) {
      m_armLeftBreak.set(Value.kForward);
      m_armRightBreak.set(Value.kForward);
    }
    else {
      m_armLeftBreak.set(Value.kReverse);
      m_armRightBreak.set(Value.kReverse);
    }

    // Shooter control
    double shooterDamp = 1.8;
    if (second_controller.getLeftTriggerAxis() > 0.02) {
      m_ShooterLeft.set(second_controller.getLeftTriggerAxis()/shooterDamp);
      m_ShooterRight.set(second_controller.getLeftTriggerAxis()/shooterDamp);
    }
    else if (second_controller.getRightTriggerAxis() > 0.02) {
      m_ShooterLeft.set(-second_controller.getRightTriggerAxis()/shooterDamp);
      m_ShooterRight.set(-second_controller.getRightTriggerAxis()/shooterDamp);
    }
    else {
      m_ShooterLeft.set(0);
      m_ShooterRight.set(0);
    }

    // Toggle breaks
    if (second_controller.getBButton() && !bTogCon) {
      bTogCon = true;
      toggleLock = !toggleLock;
    }
    else if (!second_controller.getBButton()) {
      bTogCon = false;
    }
    if (second_controller.getBButton() && toggleLock) {
      second_controller.setRumble(RumbleType.kBothRumble, 0.6);
    }
    else {
      second_controller.setRumble(RumbleType.kBothRumble, 0);
    }
  }
}

