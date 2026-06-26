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

// -----------------------------------------
// Execute Stage
// -----------------------------------------

//ToDo: Add your implementation according to the specification above here 

class EX extends Module {
  val io = IO(new Bundle {
  
    val uop = Input(uopc())
    val operandA = Input(UInt(32.W))
    val operandB = Input(UInt(32.W))
    val XcptInvalid = Input(Bool())
    val forwardA = Input(UInt(2.W))
    val forwardB = Input(UInt(2.W))

    val exMemAluResult = Input(UInt(32.W))
    val memWbAluResult = Input(UInt(32.W))

    // Outputs to EX/MEM barrier
    val aluResult = Output(UInt(32.W))
    val exception = Output(Bool())
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

    val alu = Module(new ALU)

    alu.io.operandA := forwardedA
    alu.io.operandB := forwardedB
    
    alu.io.operation := ALUOp.ADD

    switch(io.uop) {
        is(ADD, ADDI)     { alu.io.operation := ALUOp.ADD }
        is(SUB)           { alu.io.operation := ALUOp.SUB }
        is(SLL, SLLI)     { alu.io.operation := ALUOp.SLL }
        is(SLT, SLTI)     { alu.io.operation := ALUOp.SLT }
        is(SLTU, SLTUI)   { alu.io.operation := ALUOp.SLTU }
        is(XOR, XORI)     { alu.io.operation := ALUOp.XOR }
        is(SRL, SRLI)     { alu.io.operation := ALUOp.SRL }
        is(SRA, SRAI)     { alu.io.operation := ALUOp.SRA }
        is(OR, ORI)       { alu.io.operation := ALUOp.OR }
        is(AND, ANDI)     { alu.io.operation := ALUOp.AND }
        is(NOP)           { alu.io.operation := ALUOp.ADD }
    }


    io.aluResult := alu.io.aluResult

    
    io.exception := io.XcptInvalid
    }