// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.DriveSubsystem;

public class MoveForwardAndBackCommand extends CommandBase {

  private final double distance;
  private final double speed;
  private final DriveSubsystem swerve;
  private final CANSparkMax motor;
  private final CANSparkMax shooterLeft;
  private final CANSparkMax shooterRight;
  private final Timer commandTimer;

  public MoveForwardAndBackCommand(double distance, double speed, DriveSubsystem swerve, CANSparkMax motor, CANSparkMax shooterLeft, CANSparkMax shooterRight) {

    this.speed = speed;
    this.distance = distance;
    this.swerve = swerve;
    this.motor = motor;
    this.shooterLeft = shooterLeft;
    this.shooterRight = shooterRight;
    this.commandTimer = new Timer();
    
    addRequirements(swerve);

  }

  @Override
  public void initialize() {
    commandTimer.reset();
    commandTimer.start();
  }

  @Override
  public void execute() {
    double ct = commandTimer.get();
    final double stopMoving = (distance * 2) + 1 + 0.3;
    if (ct < distance) {
      swerve.drive(speed, 0, 0, true, true);
    }
    else if (ct < distance + 1) {
      swerve.drive(0, 0, 0, true, true);
    }
    else if (ct < stopMoving) {
      swerve.drive(-speed, 0, 0, true, true);
    }
    else if (ct < stopMoving + 0.5) {
      shooterLeft.set(-0.6);
      shooterRight.set(-0.6);
      motor.set(-0.3);
      swerve.drive(0, 0, 0, true, false);
    }
    else if (ct < stopMoving + 2) {
      shooterLeft.set(0);
      shooterRight.set(0);
      motor.set(1);
    }
    else {
      end(false);
    }
  }

  @Override
  public void end(boolean interrupted) {
    motor.set(0);
    swerve.drive(0, 0, 0, false, false);
  }

  @Override
  public boolean isFinished() {
    return false;
  }
}
