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
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.wpilibj.DriverStation;

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
  boolean onRamp = false;
  double targetRotationSpeed = 0.0;
  boolean autoStop = false;
  //automode 1: place cube behind robot; automode 2: place cube behind robot and move forward and balance on ramp

  //----------------------------
  //SET AUTOLOCATION HERE vv

  String autolocation = "middle";
  //options: "left", "middle", "right"

  //SET AUTOLOCATION HERE ^^
  //----------------------------


  String alliance = DriverStation.getAlliance().name();

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
  private final AHRS gyro = new AHRS(I2C.Port.kOnboard);
  private final DoubleSolenoid pcm_armBreak = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 1, 0);
  private final DigitalInput bendySwitch = new DigitalInput(0);

  NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
  NetworkTableEntry tx = table.getEntry("tx");
  NetworkTableEntry ty = table.getEntry("ty");
  NetworkTableEntry pipeline = table.getEntry("pipeline");
  double llx = tx.getDouble(0.0);
  double lly = ty.getDouble(0.0);

  double armTarget = 0.0000;
  double armSpeed = 0.0000;
  double armPos = 0.0000;

  private final Timer m_timer = new Timer();
  private final Timer m_timer2 = new Timer();


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
    pcm_armBreak.set(Value.kReverse);

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
    llx = tx.getDouble(0.0);
    lly = ty.getDouble(0.0);
  }
  
  @Override
  public void autonomousInit() {
    m_timer.reset();
    m_timer.start();
    gyro.zeroYaw();
    onRamp = false;
    autoStop = false;
    pcm_armBreak.set(Value.kReverse);
    m_armBase.getEncoder().setPosition(0);

  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    double t = m_timer.get();
    double yaw = gyro.getYaw();

    //MIDDLE, BALANCE
    if (autolocation == "middle") {
      if (t < 0.2) {
        m_Left.set(0);
        m_Right.set(0);
        m_Intake_Left.set(0.1);
        m_Intake_Right.set(0.1);
      }
      else if (t < 1.0) {
        m_armBase.set(-0.1);
        m_Intake_Left.set(0);
        m_Intake_Right.set(0);
      }
      else if (t < 1.8 && m_armBase.getEncoder().getPosition() < 0.0) {
        m_armBase.set(0.2);
        m_Intake_Left.set(-0.8);
        m_Intake_Right.set(-0.8);
      }
      else if (autoStop == false) {
        pcm_armBreak.set(Value.kForward);
        m_Intake_Left.set(0);
        m_Intake_Right.set(0);

        if (onRamp == false) {
          m_Left.set(0.2);
          m_Right.set(0.2);
        }
        else if (m_timer2.get() < 1.9) {
          m_Left.set(yaw/(70));
          m_Right.set(yaw/(70));
        }
        else if (m_timer.get() < 8.0) {
          m_Left.set(yaw/(49+(t*17)));
          m_Right.set(yaw/(49+(t*17)));
        }
        else {
          m_Left.set(0);
          m_Right.set(0);
        }
        if (yaw > 12 && onRamp == false) {
          onRamp = true;
          m_timer2.reset();
          m_timer2.start();
        }
      }
      if(t > 8 && onRamp == false) {
        autoStop = true;
        m_Left.set(0);
        m_Right.set(0);
      }
    }
    //LEFT OF RAMP, RAMP SIDE
    else if(autolocation == "left" && alliance == "Red") {
      if (t < 0.2) {
        m_armBase.getEncoder().setPosition(0);
        m_Left.set(0);
        m_Right.set(0);
        m_Intake_Left.set(0.1);
        m_Intake_Right.set(0.1);
      }
      else if (t < 1.0) {
        m_armBase.set(-0.1);
        m_Intake_Left.set(0);
        m_Intake_Right.set(0);
      }
      else if (t < 1.8 && m_armBase.getEncoder().getPosition() < -3.0) {
        m_armBase.set(0.2);
        m_Intake_Left.set(-0.8);
        m_Intake_Right.set(-0.8);
      }
      else if (t < 2.5) {
        m_Left.set(-0.2);
        m_Right.set(0.2);
      }
      else if (t < 5) {
        m_Left.set(0.2);
        m_Right.set(0.2);
      }
      else if (t < 5.2) {
        m_Right.set(-0.2);
        m_Left.set(0.2);
      }
      else {
        m_armBase.set(-m_armBase.getEncoder().getPosition()/25);
        m_Intake_Left.set(0);
        m_Intake_Right.set(0);

        if (onRamp == false) {
          m_Left.set(0.2);
          m_Right.set(0.2);
        }
        else if (m_timer.get() < 15) {
          m_Left.set(yaw/(100+(t*2.5)));
          m_Right.set(yaw/(100+(t*2.5)));
        }
        else {
          m_Left.set(0);
          m_Right.set(0);
        }
        if (yaw > 10) {
          onRamp = true;
        }
      }
    }
    //RIGHT OF RAMP, SPACE SIDE
    else if(autolocation == "right" && alliance == "Red") {
      if (t < 0.2) {
        m_armBase.getEncoder().setPosition(0);
        m_Left.set(0);
        m_Right.set(0);
        m_Intake_Left.set(0.1);
        m_Intake_Right.set(0.1);
      }
      else if (t < 1.0) {
        m_armBase.set(-0.1);
        m_Intake_Left.set(0);
        m_Intake_Right.set(0);
      }
      else if (t < 1.8 && m_armBase.getEncoder().getPosition() < -3.0) {
        m_armBase.set(0.2);
        m_Intake_Left.set(-0.8);
        m_Intake_Right.set(-0.8);
      }
      else if (t < 2.5) {
        m_Left.set(0.2);
        m_Right.set(-0.2);
      }
      else if (t < 4) {
        m_Left.set(0.2);
        m_Right.set(0.2);
      }
      else if (t < 5.2) {
        m_Right.set(0.2);
        m_Left.set(-0.2);
      }
      else {
        m_Intake_Left.set(0);
        m_Intake_Right.set(0);

        if (onRamp == false) {
          m_Left.set(0.2);
          m_Right.set(0.2);
        }
        else if (m_timer.get() < 15) {
          m_Left.set(yaw/(60+(t*3)));
          m_Right.set(yaw/(60+(t*3)));
        }
        else {
          m_Left.set(0);
          m_Right.set(0);
        }
        if (yaw > 10) {
          onRamp = true;
        }
      }
    }
    //LEFT OF RAMP, SPACE SIDE
    else if(autolocation == "left" && alliance == "Blue") {

    }
    //RIGHT OF RAMP, RAMP SIDE
    else if(autolocation == "right" && alliance  == "Blue") {

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
    pcm_armBreak.set(Value.kReverse);
    autoStop = false;
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    //movement v

    getY = -m_Joystick_Drive.getY();
    getX = -m_Joystick_Drive.getZ();
    
    //This speed line makes the robot accelerate to the Joystick's position
    //Which improves handling of the vehicle greatly.
    speedY = speedY + (getY - speedY)/18;

    //The farther forward the joystick is, the less sensitive the rotation will be.
    //This also improves handling.
    speedX = speedX + (getX - speedX)/((18+(Math.abs(getY)*10))/((m_Joystick_Drive.getRawAxis(4)*-1+2))*7);

    //Targeting system (targets cones and cubes)
    if (!(m_Joystick_Drive.getRawButton(1))) {
      m_Drive.arcadeDrive(speedY*1.1, speedX/(1.3+(Math.abs(getY)*0.8)));
      targetRotationSpeed = 0;
    }

    else {

      if(m_Joystick_Drive.getRawButton(5)) {
        pipeline.setNumber(0);
        targetRotationSpeed = targetRotationSpeed+(((llx/200)-targetRotationSpeed)/20);
        if (targetRotationSpeed > 0.2) {
          targetRotationSpeed = 0.2;
        }
      }
      else if(m_Joystick_Drive.getRawButton(6)) {
        pipeline.setNumber(1);
        targetRotationSpeed = targetRotationSpeed+(((llx/200)-targetRotationSpeed)/20);
        if (targetRotationSpeed > 0.2) {
          targetRotationSpeed = 0.2;
        }
      }
      m_Left.set(targetRotationSpeed);
      m_Right.set(-targetRotationSpeed);

    }
    
    //xbox stuff v
  
    //arm ebow

    armPos = m_armBase.getEncoder().getPosition();

    double lowerLimit = 20;
    if (m_Xbox_Co_Drive.getRightTriggerAxis() > 0.05) {
      armTarget  += m_Xbox_Co_Drive.getRightTriggerAxis()/7;
      if (armTarget > lowerLimit && m_Xbox_Co_Drive.getRightBumper() == false) {
        armTarget = lowerLimit;
      }
    }
    else if (m_Xbox_Co_Drive.getLeftTriggerAxis() > 0.05) {
      armTarget -= m_Xbox_Co_Drive.getLeftTriggerAxis()/7;
      if (armTarget < -lowerLimit && m_Xbox_Co_Drive.getRightBumper() == false) {
        armTarget = -lowerLimit;
      }
    }

    if(Math.abs(armTarget-armPos) < 0.15) {
      pcm_armBreak.set(Value.kForward);
    }
    else {
      pcm_armBreak.set(Value.kReverse);
      m_armBase.set((armTarget-armPos)/18);
    }
    
    if (m_Xbox_Co_Drive.getRawButton(8)) {
      armTarget = 0;
    }

    //arm length
    if (m_Xbox_Co_Drive.getYButton()) {
      m_armWinch.set(0.3);
    }
    else if (m_Xbox_Co_Drive.getXButton()) {
      m_armWinch.set(-0.3);
    }
    else {
      m_armWinch.set(0.0);
    }
  
    //intake
    if (m_Xbox_Co_Drive.getAButton()) {
      m_Intake_Right.set(0.2);
      m_Intake_Left.set(0.2);
      intakeMoving = true;
    }
    else if (m_Xbox_Co_Drive.getBButton()) {
      m_Intake_Right.set(-0.45);
      m_Intake_Left.set(-0.45);
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

    //arm origin
    if (m_Xbox_Co_Drive.getPOV() == 0) {
      m_armBase.getEncoder().setPosition(0);
      armTarget = 0;
      m_Xbox_Co_Drive.setRumble(RumbleType.kBothRumble, 0.1);
    }
    else {
      m_Xbox_Co_Drive.setRumble(RumbleType.kBothRumble, 0);
    }

  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    pcm_armBreak.set(Value.kForward);
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
    m_Left.set(0);
    m_Right.set(0);
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
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

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
