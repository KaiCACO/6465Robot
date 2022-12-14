// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
//import org.photonvision.PhotonCamera;


/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Shoot and Drive";
  private static final String kCustomAuto = "Shoot";
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  
  NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
  NetworkTableEntry tx = table.getEntry("tx");
  NetworkTableEntry ty = table.getEntry("ty");
  NetworkTableEntry ta = table.getEntry("ta");
  NetworkTableEntry tv = table.getEntry("tv");
  NetworkTableEntry pipeline = table.getEntry("pipeline");

  Double ang = null;
  Double dist = null;
  Double xd1;

  private final XboxController m_Xbox_Drive = new XboxController(1);
  private final Joystick m_Joystick_Drive = new Joystick(0);

  private final CANSparkMax m_Front_Left = new CANSparkMax(2, MotorType.kBrushless);
  private final CANSparkMax m_Front_Right = new CANSparkMax(3, MotorType.kBrushless);
  private final CANSparkMax m_Back_Left = new CANSparkMax(1, MotorType.kBrushless);
  private final CANSparkMax m_Back_Right = new CANSparkMax(9, MotorType.kBrushless);

  private final CANSparkMax m_elevator = new CANSparkMax(10, MotorType.kBrushless);
  private final CANSparkMax m_intake = new CANSparkMax(8, MotorType.kBrushless);

  private final DigitalInput elevator_top_switch = new DigitalInput(9);

  private final MotorControllerGroup m_Left = new MotorControllerGroup(m_Front_Left, m_Back_Left);
  private final MotorControllerGroup m_Right = new MotorControllerGroup(m_Front_Right, m_Back_Right);

  private final DifferentialDrive m_Drive = new DifferentialDrive(m_Left, m_Right);

  private final Timer m_timer = new Timer();
  private double getY;
  private double getZ;
  private boolean held = false;
  private final Timer intake_ease_timer = new Timer();

  private int whichAuto = 1;
  //PhotonCamera camera = new PhotonCamera("photonvision");



  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);


    m_Left.setInverted(false);
    m_Right.setInverted(true);


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
    double x = tx.getDouble(0.0);
    double y = ty.getDouble(0.0);
    double area = ta.getDouble(0.0);

    ang = (y * (Math.PI / 180)) + 0.4188;

    dist =  64 / Math.tan(ang);

    SmartDashboard.putNumber("Distance", dist);
    SmartDashboard.putNumber("LimelightX", x);
    SmartDashboard.putNumber("LimelightY", y);
    SmartDashboard.putNumber("LimelightArea", area);
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    System.out.println("starting autonomous");
    m_timer.reset();
    m_timer.start();
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    
    
    if (whichAuto == 1) {

      // goes 28 inches in 1.0 second at 0.20 power
      if (m_timer.get() < 1.0) {
        m_Left.set(.20);
        m_Right.set(.20);
      }
      // rotates ~90 degrees in 0.9 seconds at 0.20 power
      else if ((m_timer.get() > 1.2) && (m_timer.get() < 2.1)) {
        m_Left.set(0.20);
        m_Right.set(-0.20);
      }
      else if ((m_timer.get() > 2.3) && (m_timer.get() < 2.8)) {
        m_Left.set(.20);
        m_Right.set(.20);
      }
      else {
        m_Left.set(0);
        m_Right.set(0);
      }
    }

    else if (whichAuto == 2) {
      if (m_timer.get() < 1.0) {
        m_Left.set(.20);
        m_Right.set(.20);
      }
    }

  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    System.out.println("TeleopInit");
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    getY = m_Joystick_Drive.getY();
    getZ = m_Joystick_Drive.getZ();
    
    if (Math.abs(getZ) < 0.1) {
      getZ = 0;
    }
    if (Math.abs(getY) < 0.1) {
      getY = 0;
    }

    m_Drive.arcadeDrive(-getY/1.2, getZ/1.2);
    

    if (m_Xbox_Drive.getLeftBumper() || m_Xbox_Drive.getRightBumper()) {
      if (held == false) {
        held = true;
        intake_ease_timer.reset();
        intake_ease_timer.start();
      }
      if (m_Xbox_Drive.getLeftBumper()) {
        if (intake_ease_timer.get() > 0.5) {
          m_intake.set(0.6);
        }
        else {
          m_intake.set(0.6*(intake_ease_timer.get()*2));
        }
        m_Xbox_Drive.setRumble(RumbleType.kLeftRumble, 0.5);
        m_Xbox_Drive.setRumble(RumbleType.kRightRumble, 0);
      }
      else if (m_Xbox_Drive.getRightBumper()) {
        m_intake.set(-0.6);
        m_Xbox_Drive.setRumble(RumbleType.kLeftRumble, 0);
        m_Xbox_Drive.setRumble(RumbleType.kRightRumble, 0.5);
      }
    }
    else {
      m_intake.set(0);
      m_Xbox_Drive.setRumble(RumbleType.kLeftRumble, 0);
      m_Xbox_Drive.setRumble(RumbleType.kRightRumble, 0);
      held = false;
    }

    if (m_Xbox_Drive.getRightTriggerAxis() > 0.08 && elevator_top_switch.get()) {
      //elevatur go upsies
      m_elevator.set(0.7*(m_Xbox_Drive.getRightTriggerAxis()));
    }
    else if (m_Xbox_Drive.getLeftTriggerAxis() > 0.08) {
      //elevatur go downsies
      m_elevator.set(-0.7*(m_Xbox_Drive.getLeftTriggerAxis()));
    }
    else {m_elevator.set(0);}
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
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {

  }

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
