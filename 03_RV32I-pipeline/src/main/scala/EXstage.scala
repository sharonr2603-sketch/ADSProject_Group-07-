// ADS I Class Project
// Pipelined RISC-V Core - EX Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Instruction Execute (EX) Stage: ALU operations and exception detection

Instantiated Modules:
    ALU: Integrate your module from Assignment02 for arithmetic/logical operations

ALU Interface:
    alu.io.operandA: first operand input
    alu.io.operandB: second operand input
    alu.io.operation: operation code controlling ALU function
    alu.io.aluResult: computation result output

Internal Signals:
    Map uopc codes to ALUOp values

Functionality:
    Map instruction uop to ALU operation code
    Pass operands to ALU
    Output results to pipeline

Outputs:
    aluResult: computation result from ALU
    exception: pass exception flag
*/

package core_tile

import chisel3._
import chisel3.util._
import Assignment02.{ALU, ALUOp}
import uopc._

class EX extends Module {
  val io = IO(new Bundle {
    // Inputs from ID/EX barrier
    val uop = Input(uopc())
    val operandA = Input(UInt(32.W))
    val operandB = Input(UInt(32.W))
    val XcptInvalid = Input(Bool())

    // Outputs to EX/MEM barrier
    val aluResult = Output(UInt(32.W))
    val exception = Output(Bool())
  })

  // Instantiate ALU
  val alu = Module(new ALU)

  // Connect operands to ALU
  alu.io.operandA := io.operandA
  alu.io.operandB := io.operandB

  // Default ALU operation
  alu.io.operation := ALUOp.ADD

  // Convert processor uop into ALU operation
  switch(io.uop) {
    is(ADD, ADDI)     { alu.io.operation := ALUOp.ADD }
    is(SUB)           { alu.io.operation := ALUOp.SUB }
    is(SLL, SLLI)     { alu.io.operation := ALUOp.SLL }
    is(SLT, SLTI)     { alu.io.operation := ALUOp.SLT }
    is(SLTU, SLTIU)   { alu.io.operation := ALUOp.SLTU }
    is(XOR, XORI)     { alu.io.operation := ALUOp.XOR }
    is(SRL, SRLI)     { alu.io.operation := ALUOp.SRL }
    is(SRA, SRAI)     { alu.io.operation := ALUOp.SRA }
    is(OR, ORI)       { alu.io.operation := ALUOp.OR }
    is(AND, ANDI)     { alu.io.operation := ALUOp.AND }
    is(NOP)           { alu.io.operation := ALUOp.ADD }
  }

  // Output ALU result
  io.aluResult := alu.io.aluResult

  // Pass exception flag forward
  io.exception := io.XcptInvalid
}