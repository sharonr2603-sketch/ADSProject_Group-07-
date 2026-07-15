// ADS I Class Project
// Pipelined RISC-V Core - Branch Target Buffer
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 05/12/2026 by Tobias Jauch (@tojauch)

/*
Branch Target Buffer (BTB): a hardware component that predicts the target address of conditional branch instructions to improve pipeline performance

Functionality (cf. slide 6-48 of the lecture slides):
    Stores target addresses and prediction information for conditional branch instructions
    On a branch instruction, checks if the instruction is in the BTB and retrieves the predicted target address and prediction state
    If the prediction is taken, the processor fetches the instruction from the predicted target address; if not taken, it continues sequentially
    Updates the BTB entry based on the actual outcome of the branch instruction (taken or not taken) and updates the prediction state accordingly

Inputs:
    PC: A 32-bit program counter representing the address of the branch instruction being fetched or executed.
    update: A 1-bit signal indicating whether the BTB should be updated with new information.
    updatePC: A 32-bit program counter associated with the branch instruction being updated.
    updateTarget: A 32-bit branch target address to be stored in the BTB.
    mispredicted: A 1-bit signal indicating whether the prediction turned out to be incorrect during execution (used to update the predictor).

Outputs:
    valid: A 1-bit signal indicating whether the BTB has a valid prediction for the provided program counter.
    target: A 32-bit signal representing the predicted branch target address when a valid prediction exists.
    predictTaken: A 1-bit signal indicating whether the branch is predicted to be taken or not.

*/

package core_tile

import chisel3._
import chisel3.util._

class BTB extends Module {

  val io = IO(new Bundle {

    val update = Input(Bool())
    val updatePC = Input(UInt(32.W))
    val updateTarget = Input(UInt(32.W))
    val actualTaken = Input(Bool())
    val PC = Input(UInt(32.W))

    val valid = Output(Bool())
    val target = Output(UInt(32.W))
    val predictTaken = Output(Bool())


  })

  val entries = 8

  val validBits = RegInit(VecInit(Seq.fill(entries)(false.B)))
  val tags = Reg(Vec(entries, UInt(32.W)))
  val targets = Reg(Vec(entries, UInt(32.W)))
  val prediction = RegInit(VecInit(Seq.fill(entries)("b01".U(2.W))))

  val lookupIndex = io.PC(4, 2)
  val updateIndex = io.updatePC(4, 2)

  io.valid := validBits(lookupIndex) &&
              (tags(lookupIndex) === io.PC)

  io.target := targets(lookupIndex)

  io.predictTaken := prediction(lookupIndex)(1)

  when(io.update) {
  validBits(updateIndex) := true.B
  tags(updateIndex) := io.updatePC
  targets(updateIndex) := io.updateTarget

  when(io.actualTaken) {
    when(prediction(updateIndex) =/= "b11".U) {
      prediction(updateIndex) := prediction(updateIndex) + 1.U
    }
  }.otherwise {
    when(prediction(updateIndex) =/= "b00".U) {
      prediction(updateIndex) := prediction(updateIndex) - 1.U
    }
  }
  printf(p"""
******** BTB UPDATE ********
Branch PC       : ${io.updatePC}
Actual Target   : ${io.updateTarget}
Actual Taken    : ${io.actualTaken}
Old State       : ${prediction(updateIndex)}
****************************
""")
}

printf(p"""
================ BTB LOOKUP ================
Current PC       : ${io.PC}
BTB Hit          : ${io.valid}
Predict Taken    : ${io.predictTaken}
Predicted Target : ${io.target}
============================================
""")
}