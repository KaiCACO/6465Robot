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
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.I2C;

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
  double targetPosL = 0.0;
  double targetPosR = 0.0;
  boolean intakeMoving = false;

  private final Joystick m_Joystick_Drive = new Joystick(0);
  private final XboxController m_Xbox_Co_Drive = new XboxController(1);

  //private final Compressor pcm_Compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);

  private final CANSparkMax m_Front_Left = new CANSparkMax(1, MotorType.kBrushless);
  private final CANSparkMax m_Back_Left = new CANSparkMax(2, MotorType.kBrushless);
  private final CANSparkMax m_Front_Right = new CANSparkMax(3, MotorType.kBrushless);
  private final CANSparkMax m_Back_Right = new CANSparkMax(4, MotorType.kBrushless);

  private final MotorControllerGroup m_Left = new MotorControllerGroup(m_Front_Left, m_Back_Left);
  private final MotorControllerGroup m_Right = new MotorControllerGroup(m_Front_Right, m_Back_Right);

  private final DifferentialDrive m_Drive = new DifferentialDrive(m_Left, m_Right);

  private final CANSparkMax m_armBase = new CANSparkMax(6, MotorType.kBrushless);
  private final CANSparkMax m_armWinch = new CANSparkMax(5, MotorType.kBrushless);

  private final CANSparkMax m_Intake_Left = new CANSparkMax(7, MotorType.kBrushless);
  private final CANSparkMax m_Intake_Right = new CANSparkMax(8, MotorType.kBrushless);

  private final Compressor pcm_Compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);
  private final AHRS gyro = new AHRS(I2C.Port.kMXP);
  private final DoubleSolenoid pcm_armBreak = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 0, 1);
  private final DigitalInput bendySwitch = new DigitalInput(0);

  double armTarget = 0.0;
  double armSpeed = 0.0;
  double armPos = 0.0;

  private final Timer m_timer = new Timer();


  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {

    m_Left.setInverted(false);
    m_Right.setInverted(true);
    m_armBase.setInverted(true);
    m_Intake_Left.setInverted(true);
    m_Intake_Right.setInverted(false);
    pcm_Compressor.enableDigital();
    pcm_armBreak.set(Value.kOff);

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
  public void autonomousInit() {
    m_timer.reset();
    m_timer.start();
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    if (m_timer.get() < 3.0) {
      System.out.println("moving");
    }
    else {
      System.out.println("not moving");
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    armTarget = 0;
    armSpeed = 0;
    m_armBase.getEncoder().setPosition(0);
    intakeMoving = false;
    targetPosL = m_Intake_Left.getEncoder().getPosition();
    targetPosR = m_Intake_Right.getEncoder().getPosition();
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    //movement v
    getY = -m_Joystick_Drive.getY();
    getX = -m_Joystick_Drive.getX();
    
    //This speed line makes the robot accelerate to the Joystick's position
    //Which improves handling of the vehicle greatly.
    speedY = speedY + (getY - speedY)/18;
    //The farther forward the joystick is, the less sensitive the rotation will be.
    //This also improves handling.
    speedX = speedX + (getX - speedX)/(18+(Math.abs(getY)*10));
    
    m_Drive.arcadeDrive(speedY/1.2, speedX/(1.2+(Math.abs(getY)*0.8)));
    
    //xbox stuff v
  
    //arm ebow
    armPos = m_armBase.getEncoder().getPosition();

    double lowerLimit = 21;
    if (m_Xbox_Co_Drive.getRightBumper()) {
      armTarget  += 0.1;
      if (armTarget > lowerLimit) {
        armTarget = lowerLimit;
      }
    }
    else if (m_Xbox_Co_Drive.getLeftBumper()) {
      armTarget -= 0.1;
      if (armTarget < -lowerLimit) {
        armTarget = -lowerLimit;
      }
    }

    m_armBase.set((armTarget-armPos)/20);
    //System.out.println((armTarget-armPos)/20);
    //System.out.println(armPos);
    
    if (m_Xbox_Co_Drive.getRawButton(8)) {
      armTarget = 0;
    }

    //arm length
    if (m_Xbox_Co_Drive.getBButton()) {
      m_armWinch.set(0.15);
    }
    else if (m_Xbox_Co_Drive.getAButton()) {
      m_armWinch.set(-0.15);
    }
    else {
      m_armWinch.set(0.0);
    }
  
    //intake
    if (m_Xbox_Co_Drive.getXButton()) {
      m_Intake_Right.set(0.2);
      m_Intake_Left.set(0.2);
      intakeMoving = true;
    }
    else if (m_Xbox_Co_Drive.getYButton()) {
      m_Intake_Right.set(-0.2);
      m_Intake_Left.set(-0.2);
      intakeMoving = true;
    }
    else {
      double LPos = m_Intake_Left.getEncoder().getPosition();
      double RPos = m_Intake_Right.getEncoder().getPosition();
      if (intakeMoving == true) {
        targetPosL = LPos;
        targetPosR = RPos;
      }
      intakeMoving = false;
      m_Intake_Right.set((targetPosR - RPos)/15);
      m_Intake_Left.set((targetPosL - LPos)/15);
    }
    if (m_Xbox_Co_Drive.getRightBumper()) {
      pcm_armBreak.set(Value.kForward);
    }
    else if (m_Xbox_Co_Drive.getLeftBumper()) {
      pcm_armBreak.set(Value.kReverse);
    }
    else {
      pcm_armBreak.set(Value.kOff);
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
    pcm_armBreak.set(Value.kOff);
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
    if (m_Xbox_Co_Drive.getLeftBumper() && bendySwitch.get()) {
      m_armBase.set(-0.1);
    }
    else if (m_Xbox_Co_Drive.getRightBumper() && bendySwitch.get()) {
      m_armBase.set(0.1);
    }
    else {
      m_armBase.set(0);
    }
    if (m_Xbox_Co_Drive.getBButton()) {
      m_armWinch.set(0.15);
    }
    else if (m_Xbox_Co_Drive.getAButton()) {
      m_armWinch.set(-0.15);
    }
    else {
      m_armWinch.set(0.0);
    }
  }

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}