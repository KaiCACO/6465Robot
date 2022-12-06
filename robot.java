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

  private final XboxController m_Xbox_Drive = new XboxController(0);
  private final Joystick m_Joystick_Drive = new Joystick(1);

  private final CANSparkMax m_Front_Left = new CANSparkMax(1, MotorType.kBrushless);
  private final CANSparkMax m_Front_Right = new CANSparkMax(10, MotorType.kBrushless);
  private final CANSparkMax m_Back_Left = new CANSparkMax(2, MotorType.kBrushless);
  private final CANSparkMax m_Back_Right = new CANSparkMax(9, MotorType.kBrushless);

  private final DigitalInput bump = new DigitalInput(9);

  private final MotorControllerGroup m_Left = new MotorControllerGroup(m_Front_Left, m_Back_Left);
  private final MotorControllerGroup m_Right = new MotorControllerGroup(m_Front_Right, m_Back_Right);

  private final DifferentialDrive m_Drive = new DifferentialDrive(m_Left, m_Right);

  private final Timer m_timer = new Timer();
  public static boolean m_first = false;
  public static boolean m_second = false;
  public static boolean m_third = false;



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
    double time = Timer.getFPGATimestamp();

    if (time < 2) {
      m_Left.set(.20);
      m_Right.set(.20);
    }
    else {
      m_Left.set(0.0);
      m_Right.set(0.0);
    }
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {

  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    System.out.println("TeleopInit");
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    m_Drive.arcadeDrive(-m_Joystick_Drive.getY(), m_Joystick_Drive.getZ());

    if (m_Xbox_Drive.getRightBumper() && bump.get() == false) {
      //elevator
      System.out.println("elevator up");
    }
    else if (m_Xbox_Drive.getLeftBumper()) {
      System.out.println("elevator down");
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
