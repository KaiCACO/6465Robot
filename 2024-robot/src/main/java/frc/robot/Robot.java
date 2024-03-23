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
  private final XboxController xbox = new XboxController(Constants.OIConstants.kDriverControllerPort);

  private String m_autoSelected = "m";
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  // Motors/sensors
  private final Pigeon2 gyro = new Pigeon2(DriveConstants.kGyroCanId);
  private final DigitalInput m_ScrewLimitBack = new DigitalInput(DriveConstants.kBackScrewLimitChannel);
  private final DigitalInput m_ScrewLimitFront = new DigitalInput(DriveConstants.kFrontScrewLimitChannel);
  private final CANSparkMax m_LeadScrew = new CANSparkMax(DriveConstants.kLeadScrewCanId, MotorType.kBrushless);
  private final CANSparkMax m_Intake = new CANSparkMax(DriveConstants.kIntakeCanId, MotorType.kBrushless);
  private final CANSparkMax m_SecondIntake = new CANSparkMax(DriveConstants.kSecondIntakeCanId, MotorType.kBrushless);
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
    m_LeadScrew.setInverted(false);
    m_ArmLeft.setInverted(true);
    m_ArmRight.setInverted(true);

    m_chooser.setDefaultOption("middle", "m");
    m_chooser.addOption("left", "l");
    m_chooser.addOption("right", "r");
    SmartDashboard.putData("Auto choices", m_chooser);
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
    m_autoSelected = m_chooser.getSelected();

    if (m_autoSelected.equals("m")) {
      m_autonomousCommand = m_robotContainer.getMiddleAutoCommand(m_Intake, m_SecondIntake, m_ShooterLeft, m_ShooterRight);
    }
    else if (m_autoSelected.equals("l")) {
      m_autonomousCommand = m_robotContainer.getLeftAutoCommand(m_Intake, m_SecondIntake, m_ShooterLeft, m_ShooterRight);
    }
    else if (m_autoSelected.equals("r")) {
      m_autonomousCommand = m_robotContainer.getRightAutoCommand(m_Intake, m_SecondIntake, m_ShooterLeft, m_ShooterRight);
    }

    if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
    }
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {

    m_ShooterLeft.setInverted(true);
    m_ShooterRight.setInverted(true);
    m_Intake.setInverted(true);
    m_LeadScrew.setInverted(false);
    m_ArmLeft.setInverted(true);
    m_ArmRight.setInverted(true);
    
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
    gyro.reset();
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

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

    if(!ampTog && !speakerTog){
      xbox.setRumble(RumbleType.kBothRumble, 0);
      m_autonomousCommand = null;
      manualControls();
    }

    else if (ampTog) {
      xbox.setRumble(RumbleType.kLeftRumble, 0.2);
      // m_autonomousCommand = m_robotContainer.getAmpCommand(m_Intake, m_SecondIntake, m_ShooterLeft, m_ShooterRight);
    }

    else if (speakerTog) {
      xbox.setRumble(RumbleType.kLeftRumble, 0.2);
      // m_autonomousCommand = m_robotContainer.getSpeakerCommand(m_Intake, m_SecondIntake, m_ShooterLeft, m_ShooterRight);
    }

    if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
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
    m_ShooterLeft.set(0.5);
    m_ShooterRight.set(0.5);
  }

  private void manualControls() {


    // Lead screw control
    if (xbox.getPOV() == 0 && !m_ScrewLimitFront.get()) {
      m_LeadScrew.set(.5);
    }
    else if (xbox.getPOV() == 180 && !m_ScrewLimitBack.get()) {
      m_LeadScrew.set(-.5);
    }
    else {
      m_LeadScrew.set(0);
    }
    if (m_ScrewLimitBack.get()) {
      m_LeadScrew.getEncoder().setPosition(0);
    }
    if (m_ScrewLimitFront.get()) {
      // var convFac = m_LeadScrew.getEncoder().getPositionConversionFactor();
      // var encPos = m_LeadScrew.getEncoder().getPosition();
      // var newFac = (1/(encPos/convFac))*convFac;
      // m_LeadScrew.getEncoder().setPositionConversionFactor(newFac);
    }

    // Intake control
    if (xbox.getAButton()) {
      m_Intake.set(0.8);
      m_SecondIntake.set(0.8);
    }
    else if (xbox.getYButton()) {
      m_Intake.set(-0.8);
      m_SecondIntake.set(-0.8);
    }
    else {
      m_Intake.set(0);
      m_SecondIntake.set(0);
    }

    // Arm control
    if (xbox.getLeftBumper()) {
      if (xbox.getXButton()) {
        m_ArmLeft.set(-0.5);
      }
      else {
        m_ArmLeft.set(0.5);
      }
    }
    else {
      m_ArmLeft.set(0);
    }
    
    if (xbox.getRightBumper()) {
      if (xbox.getXButton()) {
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
    double shooterDamp = 1;
    if (xbox.getLeftTriggerAxis() > 0.02) {
      m_ShooterLeft.set(xbox.getLeftTriggerAxis()/shooterDamp);
      m_ShooterRight.set(xbox.getLeftTriggerAxis()/shooterDamp);
    }
    else if (xbox.getRightTriggerAxis() > 0.02) {
      m_ShooterLeft.set(-xbox.getRightTriggerAxis()/shooterDamp);
      m_ShooterRight.set(-xbox.getRightTriggerAxis()/shooterDamp);
    }
    else {
      m_ShooterLeft.set(0);
      m_ShooterRight.set(0);
    }

    // Toggle breaks
    if (xbox.getBButton() && !bTogCon) {
      bTogCon = true;
      toggleLock = !toggleLock;
    }
    else if (!xbox.getBButton()) {
      bTogCon = false;
    }
    if (xbox.getBButton() && toggleLock) {
      xbox.setRumble(RumbleType.kBothRumble, 0.6);
    }
    else {
      xbox.setRumble(RumbleType.kBothRumble, 0);
    }
  }
}

