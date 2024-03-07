// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.DriveSubsystem;

public class MoveForwardAndBackCommand extends CommandBase {

  private final double distance;
  private final double speed;
  private final DriveSubsystem swerve;
  private final CANSparkMax motor;
  private final Timer commandTimer;

  public MoveForwardAndBackCommand(double distance, double speed, DriveSubsystem swerve, CANSparkMax motor) {

    this.speed = speed;
    this.distance = distance;
    this.swerve = swerve;
    this.motor = motor;
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
    motor.set(0.7);
    double ct = commandTimer.get();
    if (ct < distance) {
      swerve.drive(speed, 0, 0, false, true);
    }
    else if (ct < distance + 1) {
      swerve.drive(0, 0, 0, false, true);
    }
    else if (ct < (distance * 2) + 1 + 0.1) {
      swerve.drive(-speed, 0, 0, false, true);
    }
    else {
      end(false);
    }
  }

  @Override
  public void end(boolean interrupted) {
    motor.set(0);
    System.out.println(commandTimer.get());
    swerve.drive(0, 0, 0, false, false);
  }

  @Override
  public boolean isFinished() {
    return false;
  }
}
