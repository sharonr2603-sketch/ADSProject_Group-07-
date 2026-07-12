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
    branchTarget: calculated branch target address for conditional branch instructions
    flush: control signal to flush pipeline on mispredicted branches
*/

package core_tile

import chisel3._
import chisel3.util._
import Assignment02.{ALU, ALUOp}
import uopc._

// -----------------------------------------
// Execute Stage
// -----------------------------------------

//ToDo: Add your implementation according to the specification above here 
class EX extends Module {

  val io = IO(new Bundle {

    val uop = Input(uopc())
    val pc = Input(UInt(32.W))
    val immediate = Input(UInt(32.W))

    val operandA = Input(UInt(32.W))
    val operandB = Input(UInt(32.W))

    val XcptInvalid = Input(Bool())

    val forwardA = Input(UInt(2.W))
    val forwardB = Input(UInt(2.W))

    val exMemAluResult = Input(UInt(32.W))
    val memWbAluResult = Input(UInt(32.W))

    val aluResult = Output(UInt(32.W))
    val exception = Output(Bool())

    val branchTarget = Output(UInt(32.W))
    val flush = Output(Bool())
  })

  val forwardedA = Wire(UInt(32.W))
  val forwardedB = Wire(UInt(32.W))

  forwardedA := io.operandA
  forwardedB := io.operandB

  switch(io.forwardA) {
    is("b10".U) { forwardedA := io.exMemAluResult }
    is("b01".U) { forwardedA := io.memWbAluResult }
  }

  switch(io.forwardB) {
    is("b10".U) { forwardedB := io.exMemAluResult }
    is("b01".U) { forwardedB := io.memWbAluResult }
  }

  val aluOperandB = Wire(UInt(32.W))
  aluOperandB := forwardedB

  switch(io.uop) {

    is(ADDI)  { aluOperandB := io.immediate }
    is(SLTI)  { aluOperandB := io.immediate }
    is(SLTUI) { aluOperandB := io.immediate }
    is(XORI)  { aluOperandB := io.immediate }
    is(ORI)   { aluOperandB := io.immediate }
    is(ANDI)  { aluOperandB := io.immediate }
    is(SLLI)  { aluOperandB := io.immediate }
    is(SRLI)  { aluOperandB := io.immediate }
    is(SRAI)  { aluOperandB := io.immediate }

  }

  val alu = Module(new ALU)

  alu.io.operandA := forwardedA
  alu.io.operandB := aluOperandB

  alu.io.operation := ALUOp.ADD

  switch(io.uop) {

    is(ADD, ADDI)   { alu.io.operation := ALUOp.ADD }
    is(SUB)         { alu.io.operation := ALUOp.SUB }
    is(SLL, SLLI)   { alu.io.operation := ALUOp.SLL }
    is(SLT, SLTI)   { alu.io.operation := ALUOp.SLT }
    is(SLTU, SLTUI) { alu.io.operation := ALUOp.SLTU }
    is(XOR, XORI)   { alu.io.operation := ALUOp.XOR }
    is(SRL, SRLI)   { alu.io.operation := ALUOp.SRL }
    is(SRA, SRAI)   { alu.io.operation := ALUOp.SRA }
    is(OR, ORI)     { alu.io.operation := ALUOp.OR }
    is(AND, ANDI)   { alu.io.operation := ALUOp.AND }

    is(JAL)  { alu.io.operation := ALUOp.ADD }
    is(JALR) { alu.io.operation := ALUOp.ADD }

    is(NOP) { alu.io.operation := ALUOp.ADD }
  }

  io.aluResult := alu.io.aluResult

  when(io.uop === JAL || io.uop === JALR) {
    io.aluResult := io.pc + 4.U
  }

  val branchTaken = WireDefault(false.B)

  switch(io.uop) {

    is(BEQ) {
      branchTaken := forwardedA === forwardedB
    }

    is(BNE) {
      branchTaken := forwardedA =/= forwardedB
    }

    is(BLT) {
      branchTaken := forwardedA.asSInt < forwardedB.asSInt
    }

    is(BGE) {
      branchTaken := forwardedA.asSInt >= forwardedB.asSInt
    }

    is(BLTU) {
      branchTaken := forwardedA < forwardedB
    }

    is(BGEU) {
      branchTaken := forwardedA >= forwardedB
    }

  }

  io.branchTarget := 0.U

  switch(io.uop) {

    is(BEQ, BNE, BLT, BGE, BLTU, BGEU) {
      io.branchTarget := io.pc + io.immediate
    }

    is(JAL) {
      io.branchTarget := io.pc + io.immediate
    }

    is(JALR) {
      io.branchTarget := (forwardedA + io.immediate) & "hFFFFFFFE".U
    }

  }

  io.flush := branchTaken || (io.uop === JAL) || (io.uop === JALR)

  io.exception := io.XcptInvalid

  printf(p"uop=${io.uop} A=${forwardedA} B=${forwardedB} imm=${io.immediate} aluB=${aluOperandB} result=${io.aluResult} flush=${io.flush} target=${io.branchTarget}\n")
}



    