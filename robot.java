// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  Double getY = 0.0;
  Double getX = 0.0;
  Double speedY = 0.0;
  Double speedX = 0.0;

  private final Joystick m_Joystick_Drive = new Joystick(0);
  private final XboxController m_Xbox_Co_Drive = new XboxController(1);

  //private final Compressor pcm_Compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);

  private final CANSparkMax m_Front_Left = new CANSparkMax(10, MotorType.kBrushless);
  private final CANSparkMax m_Front_Right = new CANSparkMax(1, MotorType.kBrushless);
  private final CANSparkMax m_Back_Left = new CANSparkMax(9, MotorType.kBrushless);
  private final CANSparkMax m_Back_Right = new CANSparkMax(2, MotorType.kBrushless);

  private final MotorControllerGroup m_Left = new MotorControllerGroup(m_Front_Left, m_Back_Left);
  private final MotorControllerGroup m_Right = new MotorControllerGroup(m_Front_Right, m_Back_Right);

  private final DifferentialDrive m_Drive = new DifferentialDrive(m_Left, m_Right);

  private final DoubleSolenoid p_Intake_Left = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 2, 3);
  private final DoubleSolenoid p_Intake_Right = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 1, 0);

  //private final CANSparkMax m_Aim = new CANSparkMax(8, MotorType.kBrushless);
  private final CANSparkMax m_Intake = new CANSparkMax(3, MotorType.kBrushed);
  private final CANSparkMax m_IntakeInner = new CANSparkMax(8, MotorType.kBrushless);
  private final CANSparkMax m_Winch = new CANSparkMax(5, MotorType.kBrushless);
  private final CANSparkMax m_Shoot1 = new CANSparkMax(4, MotorType.kBrushed);
  private final CANSparkMax m_Shoot2 = new CANSparkMax(7, MotorType.kBrushed);

  private final Timer m_timer = new Timer();


  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {

    m_Left.setInverted(false);
    m_Right.setInverted(true);
    m_Shoot2.setInverted(true);



  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}
  
  @Override
  public void autonomousInit() {}

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    p_Intake_Left.set(Value.kForward);
    p_Intake_Right.set(Value.kForward);
    //pcm_Compressor.enableDigital();
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    getY = -m_Joystick_Drive.getY();
    getX = -m_Joystick_Drive.getX();
    
    //This speed line makes the robot accelerate to the Joystick's position
    //Which improves handling of the vehicle greatly.
    speedY = speedY + (getY - speedY)/18;
    //The farther forward the joystick is, the less sensitive the rotation will be.
    //This also improves handling.
    speedX = speedX + (getX - speedX)/(18+(Math.abs(getY)*10));
    
    m_Drive.arcadeDrive(speedY/1.2, speedX/(1.2+(Math.abs(getY)*0.8)));

    if(m_Xbox_Co_Drive.getLeftTriggerAxis() > .10){
      m_Intake.set(-m_Xbox_Co_Drive.getLeftTriggerAxis());
    }
    else if(m_Xbox_Co_Drive.getRightTriggerAxis() > .10){
      m_Intake.set(m_Xbox_Co_Drive.getRightTriggerAxis());
    }
    else{
      m_Intake.set(0.0);
    }

    if(m_Xbox_Co_Drive.getXButton()){
      m_IntakeInner.set(0.7);
    }
    else if(m_Xbox_Co_Drive.getYButton()){
      m_IntakeInner.set(-0.7);
    }
    else{
      m_IntakeInner.set(0.0);
    }

    if(m_Xbox_Co_Drive.getBButton()) {
      m_Winch.set(-0.3);
    }
    else if(m_Xbox_Co_Drive.getAButton()) {
      m_Winch.set(0.3);
    }
    else {
      m_Winch.set(0.0);
    }

    if(m_Xbox_Co_Drive.getRightBumper()) {
      m_Shoot1.set(0.5);
      m_Shoot2.set(0.5);
    }
    else if(m_Xbox_Co_Drive.getLeftBumper()) {
      m_Shoot1.set(-0.5);
      m_Shoot2.set(-0.5);
    }
    else {
      m_Shoot1.set(0.0);
      m_Shoot2.set(0.0);
    }

    if(m_Xbox_Co_Drive.getPOV() == 0) {
      p_Intake_Left.set(Value.kReverse);
      p_Intake_Right.set(Value.kReverse);
    }
    else if(m_Xbox_Co_Drive.getPOV() == 180) {
      p_Intake_Left.set(Value.kForward);
      p_Intake_Right.set(Value.kForward);
    }
    else {
      p_Intake_Left.set(Value.kOff);
      p_Intake_Right.set(Value.kOff);
    }

  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    m_timer.reset();
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
    m_Intake.set(0.5);
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
