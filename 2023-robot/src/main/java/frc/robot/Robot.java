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
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.wpilibj.DriverStation;

import java.sql.Array;

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
  private String autolocation;

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
  private final DoubleSolenoid pcm_llcontrol = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 3, 2);

  NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
  NetworkTableEntry tx = table.getEntry("tx");
  NetworkTableEntry ty = table.getEntry("ty");
  NetworkTableEntry tagPos = table.getEntry("camerapose_targetspace");
  NetworkTableEntry pipeline = table.getEntry("pipeline");
  double llx = tx.getDouble(0.0);
  double lly = ty.getDouble(0.0);
  double[] lltagPos = tagPos.getDoubleArray(new double[6]);

  double armTarget = 0.0000;
  double armSpeed = 0.0000;
  double armPos = 0.0000;
  Boolean llpos = true;
  Boolean llbtn = false;
  Boolean offRamp = false;
  Boolean backOnRamp = false;
  Boolean levelArm = false;

  private final Timer m_timer = new Timer();
  private final Timer m_rampTimer = new Timer();
  private final Timer m_rampOffTimer = new Timer();
  private final Timer m_onRampAgainTimer = new Timer();
  private final Timer m_sideTimer = new Timer();


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
    pcm_llcontrol.set(Value.kForward);

    var autoOptions = new String[] {"balance", "justPlaceCube", "goGetConeNoRamp", "noMovement"};
    SmartDashboard.putStringArray("Auto List", autoOptions);

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
    m_rampOffTimer.reset();
    m_rampOffTimer.stop();
    m_rampTimer.reset();
    m_rampTimer.stop();
    gyro.zeroYaw();
    onRamp = false;
    autoStop = false;
    pcm_armBreak.set(Value.kReverse);
    pcm_llcontrol.set(Value.kForward);
    m_armBase.getEncoder().setPosition(0);
    offRamp = false;
    backOnRamp = false;

    autolocation = SmartDashboard.getString("Auto Selector", "justPlaceCube");
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    double t = m_timer.get();
    double yaw = gyro.getYaw();

    //MIDDLE, BALANCE
    if (autolocation.equals("balance") && !autoStop) {
      
      if (!offRamp) {
      //intake cube a little
        if (t < 0.28) {
          m_Left.set(0);
          m_Right.set(0);
          m_Intake_Left.set(0.28);
          m_Intake_Right.set(0.28);
        }
        //bring arm back
        else if (t < 1.0) {
          m_armBase.set(-0.1);
          m_Intake_Left.set(0);
          m_Intake_Right.set(0);
        }
        //outtake and move arm back up
        else if (t < 4 && m_armBase.getEncoder().getPosition() < -1 ) {
          m_armBase.set(0.2);
          m_Intake_Left.set(-0.9);
          m_Intake_Right.set(-0.9);
          m_Left.set(0);
          m_Right.set(0);
        }
        else if (!autoStop) {
          //break arm
          m_armBase.set(0);
          pcm_armBreak.set(Value.kForward);
          m_Intake_Left.set(0);
          m_Intake_Right.set(0);
          //set onRamp to true when on the ramp for the first time
          if (yaw > 10 && !onRamp) {
            onRamp = true;
          }
          //if not on the ramp yet, move forward
          if (!onRamp) {
            m_rampTimer.reset();
            m_rampTimer.start();
            m_Left.set(0.2);
            m_Right.set(0.2);
          }
          //if onRamp is true, drive over the ramp and restart timer whenever the robot tilts
          else if (m_rampTimer.get() < 0.8) {
            if (Math.abs(yaw) > 8) {
              m_rampTimer.reset();
              m_rampTimer.start();
            }
            m_Left.set(0.2);
            m_Right.set(0.2);
          }
          //if the robot hasn't tilted for 0.8 seconds, set offRamp to true and start the rampOffTimer
          else if(!offRamp) {
            m_Left.set(0);
            m_Right.set(0);
            offRamp = true;
            m_rampOffTimer.reset();
            m_rampOffTimer.start();
          }
        }
      }
      //if the robot has left the ramp
      else if (offRamp) {
        pcm_armBreak.set(Value.kForward);
        //if it hasn't reentered the ramp. move backwards
        if (m_rampOffTimer.get() > 0.5 && !backOnRamp) {
          m_Left.set(-0.25);
          m_Right.set(-0.25);
          //If it gets onto the ramp, set the variable to true and start the timer
          if (Math.abs(yaw) > 10) {
            backOnRamp = true;
            m_onRampAgainTimer.reset();
            m_onRampAgainTimer.start();
          }
        }
        //when it gets back onto the ramp, balance quickly for 1.5 seconds
        else if (backOnRamp && m_onRampAgainTimer.get() < 1.5) {
          m_Left.set(yaw/75);
          m_Right.set(yaw/75);
        }
        //then switch to slow balancing
        else if (m_onRampAgainTimer.get() < 10) {
          m_Left.set(yaw/(62+(m_onRampAgainTimer.get()*22)));
          m_Right.set(yaw/(62+(m_onRampAgainTimer.get()*22)));
        }
        //stop moving once it's been on for over 10 seconds
        else {
          m_Left.set(0);
          m_Right.set(0);
          autoStop = true;
          levelArm = true;
        }
      }
      
      //if 3 seconds have gone by without the robot getting onto the ramp, sw
      if(t > 9 && !onRamp) {
        m_Left.set(0);
        m_Right.set(0);
        autolocation = "noMovement";
        m_sideTimer.reset();
        m_sideTimer.start();
      }
      if(levelArm) {
        pcm_armBreak.set(Value.kReverse);
        m_armBase.set((-armPos)/18);
        if(Math.abs(armPos) < 3) {
          pcm_armBreak.set(Value.kForward);
          m_armBase.set(0);
          levelArm = false;
        }
      }
    }
    //NOT IN MIDDLE
    else if(autolocation.equals("goGetConeNoRamp")) {
      if(m_sideTimer.get() < 3.38) {
        if (m_armBase.getEncoder().getPosition() < 20) {
          pcm_armBreak.set(Value.kReverse);
          m_armBase.set(0.2);
        }
        else {
          m_armBase.set(0);
          pcm_armBreak.set(Value.kForward);
        }
        pipeline.setNumber(0);
        m_Left.set(0.15+llx/158);
        m_Right.set(0.15-llx/158);
        m_Intake_Left.set(0.1);
        m_Intake_Right.set(0.1);
      }
      else if (m_sideTimer.get() < 3.9) {
        m_armBase.set(0);
        pcm_armBreak.set(Value.kForward);
        m_Intake_Left.set(0.3);
        m_Intake_Right.set(0.3);
      }
      else if(m_armBase.getEncoder().getPosition() > 3) {
        pcm_armBreak.set(Value.kReverse);
        m_Intake_Left.set(0);
        m_Intake_Right.set(0);
        m_armBase.set(-0.2);
      }
      else {
        m_armBase.set(0);
        pcm_armBreak.set(Value.kOff);
        m_Left.set(0);
        m_Right.set(0);
      }
    }
    else if(autolocation.equals("justPlaceCube")) {
      //intake cube a little
      if (t < 0.28) {
        pcm_armBreak.set(Value.kReverse);
        m_Left.set(0);
        m_Right.set(0);
        m_Intake_Left.set(0.28);
        m_Intake_Right.set(0.28);
      }
      //bring arm back
      else if (t < 1.0) {
        m_armBase.set(-0.1);
        m_Intake_Left.set(0);
        m_Intake_Right.set(0);
      }
      //outtake and move arm back up
      else if (t < 4 && m_armBase.getEncoder().getPosition() < -1 ) {
        m_armBase.set(0.2);
        m_Intake_Left.set(-0.9);
        m_Intake_Right.set(-0.9);
        m_Left.set(0);
        m_Right.set(0);
      }
      else {
        pcm_armBreak.set(Value.kForward);
        m_armBase.set(0);
        m_Intake_Left.set(0);
        m_Intake_Right.set(0);
      }
    };
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    m_armBase.getEncoder().setPosition(0);
    armTarget = 0;
    armSpeed = 0;
    intakeMoving = false;
    targetPosL = m_Intake_Left.getEncoder().getPosition();
    targetPosR = m_Intake_Right.getEncoder().getPosition();
    pcm_armBreak.set(Value.kReverse);
    autoStop = false;
    m_Left.set(0);
    m_Right.set(0);
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    //movement v

    getY = -m_Joystick_Drive.getY();
    getX = -m_Joystick_Drive.getZ();

    var jswitch = m_Joystick_Drive.getRawAxis(3);
    
    //This speed line makes the robot accelerate to the Joystick's position
    //Which improves handling of the vehicle greatly.
    speedY = speedY + (getY - speedY)/(45/((jswitch*-1+2)*2.8));

    //The farther forward the joystick is, the less sensitive the rotation will be.
    //This also improves handling.

    var divisionFunction = ((45+(Math.abs(getY)*10))/((jswitch*-1+2)*2.6));
    speedX = speedX + (getX - speedX)/divisionFunction;

    //Targeting system (targets cones and cubes)
    if (!(m_Joystick_Drive.getRawButton(1))) {
      m_Drive.arcadeDrive(speedY*1.1, speedX/(1.3+(Math.abs(getY)*0.8)));
      targetRotationSpeed = 0;
    }

    else {
      
      if(!llpos) {
        System.out.println(lltagPos);
      }
      else if(m_Joystick_Drive.getRawButton(5)) {
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

    double lowerLimit = 22;
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
  
    //limelight control
    if (m_Joystick_Drive.getRawButton(2) && !llpos && !llbtn) {
      pcm_llcontrol.set(Value.kForward);
      llpos = true;
      llbtn = true;
    }
    else if (m_Joystick_Drive.getRawButton(2) && llpos && !llbtn) {
      pcm_llcontrol.set(Value.kReverse);
      llpos = false;
      llbtn = true;
    }
    else if (!m_Joystick_Drive.getRawButton(2)) {
      pcm_llcontrol.set(Value.kOff);
      llbtn = false;
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
    gyro.zeroYaw();
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
    System.out.println(gyro.getYaw());
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
